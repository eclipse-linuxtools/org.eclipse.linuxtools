/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersHideShowManager;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This dialog allows the user to show/hide some columns of the viewer The status of shown/hidden columns is saved in
 * the dialog settings of the view.
 *
 */
public class STDataViewersHideShowColumnsDialog extends Dialog {

    private boolean dirty;

    private STDataViewersHideShowManager manager;

    private CheckboxTableViewer checkButtonsTable;

    private AbstractSTViewer stViewer;

    /**
     * Creates the hide/show columns dialog for the given AbstractSTViewer.
     *
     * @param stViewer The  AbstractSTViewer to control.
     */
    public STDataViewersHideShowColumnsDialog(AbstractSTViewer stViewer) {
        super(stViewer.getViewer().getControl().getShell());
        this.dirty = false;
        this.stViewer = stViewer;
        this.manager = stViewer.getHideShowManager();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(STDataViewersMessages.hideshowDialog_title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
        composite.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(2, true);
        composite.setLayout(layout);

        initializeDialogUnits(composite);

        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        checkButtonsTable = createCheckboxTable(composite, layoutData);
        checkButtonsTable.setInput(stViewer.getColumns());
        checkButtonsTable.addCheckStateListener(checkStateListener);

        Button selectAllButton = new Button(composite, SWT.NONE);
        selectAllButton.setText(STDataViewersMessages.selectAll_text);
        selectAllButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                dirty = true;
                checkButtonsTable.setAllChecked(true);
            }
        });
        layoutData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
        selectAllButton.setLayoutData(layoutData);

        Button deselectAllButton = new Button(composite, SWT.NONE);
        deselectAllButton.setText(STDataViewersMessages.deselectAll_text);
        deselectAllButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                dirty = true;
                checkButtonsTable.setAllChecked(false);
            }
        });
        layoutData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
        deselectAllButton.setLayoutData(layoutData);

        createSeparatorLine(composite);
        Dialog.applyDialogFont(composite);

        setDefaultWidgetsValues();

        return composite;
    }

    private void setDefaultWidgetsValues() {
        Item[] columns = stViewer.getColumns();
        for (int i = columns.length; i-- > 0;) {
            boolean state = (manager.getState(i) == STDataViewersHideShowManager.STATE_SHOWN);
            checkButtonsTable.setChecked(columns[i], state);
        }
    }

    private ICheckStateListener checkStateListener = new ICheckStateListener() {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            dirty = true;
        }
    };

    /**
     * Creates a separator line above the OK/Cancel buttons bar.
     *
     * @param parent The parent composite.
     */
    protected void createSeparatorLine(Composite parent) {
        GridLayout parentLayout = (GridLayout) parent.getLayout();

        // Build the separator line
        Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, parentLayout.numColumns, 1);
        separator.setLayoutData(layoutData);
    }

    private CheckboxTableViewer createCheckboxTable(Composite parent, GridData layoutData) {
        CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE | SWT.HIDE_SELECTION
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        tableViewer.getControl().setLayoutData(layoutData);

        tableViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof Item[]) {
                    Item[] columns = (Item[]) inputElement;
                    int[] order = stViewer.getColumnOrder();
                    Item[] elements = new Item[columns.length];

                    // sort the columns according to their actual display order
                    for (int i = columns.length; i-- > 0;) {
                        elements[i] = columns[order[i]];
                    }

                    return elements;
                }
                return new Object[] {};
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });

        tableViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof Item) {
                    Item column = (Item) element;

                    if (manager.getWidth(stViewer.getColumnIndex(column)) == 0) {
                        return column.getText() + " (width = 0)";
                    }
                    return column.getText();
                }
                return element.toString();
            }
        });

        return tableViewer;
    }

    @Override
    public int open() {
        dirty = false;
        return super.open();
    }

    @Override
    protected void okPressed() {
        if (dirty) {
            saveManagerSettings();
        }
        super.okPressed();
    }

    private void saveManagerSettings() {
        Item[] columns = stViewer.getColumns();
        for (int i = columns.length; i-- > 0;) {
            int state = checkButtonsTable.getChecked(columns[i]) ? STDataViewersHideShowManager.STATE_SHOWN
                    : STDataViewersHideShowManager.STATE_HIDDEN;
            manager.setState(i, state);
        }
    }

    /**
     * @return The hideShowManager.
     */
    public STDataViewersHideShowManager getManager() {
        return manager;
    }

    /**
     * Returns whether the dialog contains changes.
     *
     * @return boolean True if the dialog has changed, false otherwise.
     */
    public boolean isDirty() {
        return dirty;
    }


}
