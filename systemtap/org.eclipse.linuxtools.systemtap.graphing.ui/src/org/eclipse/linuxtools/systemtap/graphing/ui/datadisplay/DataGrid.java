/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *     Red Hat Inc. - ongoing maintenance
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.ui.datadisplay;

import java.text.MessageFormat;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.GraphingUIPlugin;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.Localization;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.preferences.GraphingPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.wizards.filter.AvailableFilterTypes;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.wizards.filter.SelectFilterWizard;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.structures.IFormattingStyles;
import org.eclipse.linuxtools.systemtap.structures.StringFormatter;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class DataGrid implements IUpdateListener {

    public static final int NONE = 0;
    public static final int FULL_UPDATE = 1;

    private IFilteredDataSet filteredDataSet;
    private IFormattingStyles columnFormat[];
    private Table table;
    private Point clickLocation;
    private IPreferenceStore prefs;
    private Menu filterMenu;

    private MenuItem autoResizeMenuItem;
    private MenuItem jumpToEntryMenuItem;

    private MenuItem removeFiltersMenuItem;
    private MenuItem formatMenuItem;
    private IPropertyChangeListener propertyChangeListener;

    /**
     * @param composite The {@link Composite} in which the grid will reside.
     * @param set The data set this grid is associated with.
     * @since 3.0 set must be a IFilteredDataSet.
     */
    public DataGrid(Composite composite, IFilteredDataSet set) {
        prefs = GraphingUIPlugin.getDefault().getPreferenceStore();

        filteredDataSet = set;
        clickLocation = new Point(-1, -1);
        createPartControl(composite);

        propertyChangeListener = event -> {
		    if (event.getProperty().equals(GraphingPreferenceConstants.P_MAX_DATA_ITEMS)) {
		        handleUpdateEvent();
		    }
		};
        prefs.addPropertyChangeListener(propertyChangeListener);
    }

    public Control getControl() { return table; }

    private void createPartControl(Composite parent) {
        table = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.getVerticalBar().setVisible(true);

        String[] names = filteredDataSet.getTitles();
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(Localization.getString("DataGrid.Row")); //$NON-NLS-1$
        column.pack();
        column.setMoveable(false);
        column.setResizable(false);

        columnFormat = new IFormattingStyles[names.length];
        for (int i = 0; i < names.length; i++) {
            column = new TableColumn(table, SWT.LEFT);
            column.setText(names[i]);
            column.pack();
            column.setMoveable(true);

            columnFormat[i] = new StringFormatter();
        }

        table.setMenu(this.initMenus());

        table.addListener(SWT.MouseDown, event -> {
		    clickLocation.x = event.x;
		    clickLocation.y = event.y;
		});
        handleUpdateEvent();
    }

    private Menu initMenus() {
        Menu menu = new Menu(table.getShell(), SWT.POP_UP);

        Menu formatMenu = new Menu(menu);
        formatMenuItem = new MenuItem(menu, SWT.CASCADE);
        formatMenuItem.setText(Localization.getString("DataGrid.FormatAs")); //$NON-NLS-1$
        formatMenuItem.setMenu(formatMenu);

        filterMenu = new Menu(menu);
        MenuItem item = new MenuItem(menu, SWT.CASCADE);
        item.setText(Localization.getString("DataGrid.AddFilter")); //$NON-NLS-1$
        item.addSelectionListener(new AddFilterSelection());

        removeFiltersMenuItem = new MenuItem(menu, SWT.CASCADE);
        removeFiltersMenuItem.setText(Localization.getString("DataGrid.RemoveFilter")); //$NON-NLS-1$
        removeFiltersMenuItem.setMenu(filterMenu);

        IDataSetFilter[] filters = filteredDataSet.getFilters();
        if (filters != null && filters.length > 0) {
            for (IDataSetFilter filter : filters) {
                item = new MenuItem(filterMenu, SWT.CASCADE);
                item.setText(AvailableFilterTypes.getFilterName(filter.getID()));
                item.setData(filter);
                item.addSelectionListener(new RemoveFilterSelection());
            }
        } else {
            removeFiltersMenuItem.setEnabled(false);
        }

        autoResizeMenuItem = new MenuItem(menu, SWT.CHECK);
        autoResizeMenuItem.setText(Localization.getString("DataGrid.AutoResize")); //$NON-NLS-1$
        autoResizeMenuItem.setSelection(prefs.getBoolean(GraphingPreferenceConstants.P_AUTO_RESIZE));
        jumpToEntryMenuItem = new MenuItem(menu, SWT.CHECK);
        jumpToEntryMenuItem.setText(Localization.getString("DataGrid.JumpToEntry")); //$NON-NLS-1$
        jumpToEntryMenuItem.setSelection(prefs.getBoolean(GraphingPreferenceConstants.P_JUMP_NEW_TABLE_ENTRY));

        for (String element : IFormattingStyles.FORMAT_TITLES) {
            item = new MenuItem(formatMenu, SWT.RADIO);
            item.setText(element);
            item.addSelectionListener(new MenuFormatSelection());
        }

        formatMenuItem.setEnabled(filteredDataSet.getRowCount() > 0);
        formatMenu.addMenuListener(new FormatMenuListener());
        return menu;
    }

    private int getSelectedColumn() {
        TableColumn[] cols = table.getColumns();
        int location = 0;
        for (int i = 0; i < cols.length; i++) {
            if (clickLocation.x > location && clickLocation.x < (location += cols[i].getWidth())) {
                return i;
            }
        }

        return cols.length-1;
    }

    private class AddFilterSelection extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            SelectFilterWizard wizard = new SelectFilterWizard(filteredDataSet.getTitles());
            IWorkbench workbench = PlatformUI.getWorkbench();
            wizard.init(workbench, null);
            WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
            dialog.create();
            int result = dialog.open();

            if (result != Window.CANCEL) {
                IDataSetFilter filter = wizard.getFilter();
                removeFiltersMenuItem.setEnabled(true);
                filteredDataSet.addFilter(filter);
                table.removeAll();
                handleUpdateEvent();

                MenuItem item = new MenuItem(filterMenu, SWT.CASCADE);
                item.setText(MessageFormat.format(Localization.getString("DataGrid.FilterLabel"), //$NON-NLS-1$
                        AvailableFilterTypes.getFilterName(filter.getID()), filteredDataSet.getTitles()[filter.getColumn()], filter.getInfo()));
                item.setData(filter);
                item.addSelectionListener(new RemoveFilterSelection());
            }
            wizard.dispose();
        }
    }

    private class RemoveFilterSelection implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
			IDataSetFilter idsf = (IDataSetFilter) e.widget.getData();
            e.widget.dispose();
            if (filterMenu.getItemCount() == 0) {
                removeFiltersMenuItem.setEnabled(false);
            }

            if (filteredDataSet.removeFilter(idsf)) {
                table.removeAll();
                handleUpdateEvent();
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {}
    }

    private class FormatMenuListener extends MenuAdapter {
        @Override
        public void menuShown(MenuEvent e) {
            MenuItem[] items = ((Menu)e.widget).getItems();
            boolean doubleValid = false, longValid = false;
            String itemText;

            int selectedCol = Math.max(1, getSelectedColumn());

            for (MenuItem item : items) {
                item.setSelection(false);
            }
            items[columnFormat[selectedCol-1].getFormat()].setSelection(true);

            items[IFormattingStyles.UNFORMATED].setEnabled(true);
            items[IFormattingStyles.STRING].setEnabled(true);

            itemText = filteredDataSet.getRow(0)[selectedCol-1].toString();
            try {
                Double.parseDouble(itemText);
                doubleValid = true;
            } catch (NumberFormatException nfe) {}
            try {
                Long.parseLong(itemText);
                longValid = true;
            } catch (NumberFormatException nfe) {}

            items[IFormattingStyles.DOUBLE].setEnabled(doubleValid);
            items[IFormattingStyles.HEX].setEnabled(longValid);
            items[IFormattingStyles.OCTAL].setEnabled(longValid);
            items[IFormattingStyles.BINARY].setEnabled(longValid);
            items[IFormattingStyles.DATE].setEnabled(longValid);
        }
    }

    private class MenuFormatSelection extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            int format = IFormattingStyles.UNFORMATED;
            int column = Math.max(1, getSelectedColumn());
            int i;
            for (i = 0; i < IFormattingStyles.FORMAT_TITLES.length; i++) {
                if (IFormattingStyles.FORMAT_TITLES[i].equals(((MenuItem)e.getSource()).getText())) {
                    format = i;
                }
            }

            Object[] data = filteredDataSet.getColumn(column-1);
            columnFormat[column-1].setFormat(format);
            for (i = 0; i < table.getItemCount(); i++) {
                table.getItem(i).setText(column, columnFormat[column-1].format(data[i].toString()));
            }
            table.redraw();
        }
    }

    @Override
    public void handleUpdateEvent() {
        if (table.isDisposed()) {
            return;
        }

        table.getDisplay().asyncExec(() -> {
		    if (table.isDisposed()) {
		        return;
		    }
		    TableItem item;
		    int startLocation;
		    int endLocation = filteredDataSet.getRowCount();
		    int maxItems = prefs.getInt(GraphingPreferenceConstants.P_MAX_DATA_ITEMS);
		    int oldSelection = table.getSelectionIndex();

		    //Remove old items to refresh table, and only read in as many items as will fit.
		    //Note that a full refresh is necessary in order for filtered data to appear correctly.
		    table.removeAll();
		    startLocation = Math.max(endLocation-maxItems, 0);

		    //Add all the new items to the table
		    Object[] os;
		    for (int j, i1 = startLocation; i1 < endLocation; i1++) {
		        item = new TableItem(table, SWT.NONE);
		        os = filteredDataSet.getRow(i1);

		        //Add 1 to the index/row num since graphs start counting rows at 1, not 0.
		        item.setText(0, Integer.toString(i1 + 1));
		        for (j = 0; j < os.length; j++) {
		            //Ignore null items
		            if (os[j] != null) {
		                item.setText(j+1, columnFormat[j].format(os[j].toString()));
		            }
		        }
		    }
		    //Re-select the old table selection, if there was one
		    if (oldSelection != -1) {
		        table.select(oldSelection);
		    }

		    //Resize the columns
		    TableColumn col = table.getColumn(0);
		    col.pack();
		    if (autoResizeMenuItem.getSelection()) {
		        TableColumn[] cols = table.getColumns();
		        for (int i2 = 1; i2 < cols.length; i2++) {
		            cols[i2].pack();
		        }
		    }

		    //Use if we want to set focus to newly added item.
		    //Run async so the table can be fully constructed before jumping to an entry.
		    if (jumpToEntryMenuItem.getSelection() && table.getItemCount() > 0) {
		        table.getDisplay().asyncExec(() -> table.showItem(table.getItem(table.getItemCount()-1)));
		    }
		    formatMenuItem.setEnabled(table.getItemCount() > 0);
		});
    }

    public void dispose() {
        filteredDataSet = null;
        table.dispose();
        table = null;
        clickLocation = null;
        columnFormat = null;
        prefs.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = null;
    }
}
