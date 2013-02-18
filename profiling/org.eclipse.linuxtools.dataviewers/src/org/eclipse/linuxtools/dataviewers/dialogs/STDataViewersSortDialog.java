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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersComparator;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This dialog is used to choose how the data is sorted in the viewer
 *
 */
public class STDataViewersSortDialog extends TrayDialog {

    private final STDataViewersComparator sorter;

    private Combo[] priorityCombos;

    private Button[] ascendingButtons;

    private Button[] descendingButtons;

    private boolean dirty;

    /**
     * Create a new instance of the receiver.
     *
     * @param parentShell
     * @param sorter
     */
    public STDataViewersSortDialog(Shell parentShell, STDataViewersComparator sorter) {
        super(parentShell);
        this.sorter = sorter;
        this.dirty = false;
    }

    /*
     * (non-Javadoc) Method declared on Window.
     */
    @Override
	protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(STDataViewersMessages.sortDialog_title);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    @Override
	protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        if (sorter == null) {
            return composite;
        }

        initializeDialogUnits(composite);

        createPrioritiesArea(composite);
        createRestoreDefaultsButton(composite);
        createSeparatorLine(composite);

        Dialog.applyDialogFont(composite);

        return composite;
    }

    /**
     * Create the proirities area.
     *
     * @param parent
     */
    private void createPrioritiesArea(Composite parent) {
        Composite prioritiesArea = new Composite(parent, SWT.NULL);
        prioritiesArea.setLayout(new GridLayout(3, false));

        int[] priorities = sorter.getPriorities();

        ascendingButtons = new Button[priorities.length];
        descendingButtons = new Button[priorities.length];
        priorityCombos = new Combo[Math.min(priorities.length, STDataViewersComparator.MAX_DEPTH)];

        Label sortByLabel = new Label(prioritiesArea, SWT.NULL);
        sortByLabel.setText(STDataViewersMessages.sortDialog_label);
        GridData data = new GridData();
        data.horizontalSpan = 3;
        sortByLabel.setLayoutData(data);

        for (int i = 0; i < priorityCombos.length; i++) {
            final int index = i;
            Label numberLabel = new Label(prioritiesArea, SWT.NULL);
            numberLabel.setText(NLS.bind(STDataViewersMessages.sortDialog_columnLabel, i + 1));

            priorityCombos[i] = new Combo(prioritiesArea, SWT.READ_ONLY);
            priorityCombos[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Composite directionGroup = new Composite(prioritiesArea, SWT.NONE);
            directionGroup.setLayout(new GridLayout(2, false));

            ascendingButtons[i] = new Button(directionGroup, SWT.RADIO);
            ascendingButtons[i].setText(getAscendingText(i));
            ascendingButtons[i].addSelectionListener(new SelectionAdapter() {
                @Override
				public void widgetSelected(SelectionEvent e) {
                    markDirty();
                }
            });
            descendingButtons[i] = new Button(directionGroup, SWT.RADIO);
            descendingButtons[i].setText(getDescendingText(i));
            descendingButtons[i].addSelectionListener(new SelectionAdapter() {
                @Override
				public void widgetSelected(SelectionEvent e) {
                    markDirty();
                }
            });

            if (i < priorityCombos.length - 1) {
                priorityCombos[i].addSelectionListener(new SelectionAdapter() {
                    @Override
					public void widgetSelected(SelectionEvent e) {
                        List<String> allItems = new ArrayList<String>(Arrays.asList(priorityCombos[index].getItems()));
                        computeSelectionItems(index, allItems);
                        markDirty();
                    }

                    private void computeSelectionItems(int index, List<String> allItems) {
                        // if not after the last
                        if (index < priorityCombos.length) {
                            // target combo
                            Combo priorityCombo = priorityCombos[index];
                            // target combo's "old selection" (current selection)
                            String oldSelection = priorityCombo.getItem(priorityCombo.getSelectionIndex());
                            // setting new items list
                            priorityCombo.setItems(allItems.toArray(new String[allItems.size()]));

                            if (allItems.contains(oldSelection)) {
                                // old selection can be kept.
                                String newSelection = oldSelection;
                                priorityCombo.select(allItems.indexOf(oldSelection));
                                allItems.remove(newSelection);
                            } else {
                                // old selection has been removed by another combo.
                                // selecting a new element (the first) in the items list.
                                String newSelection = allItems.get(0);
                                priorityCombo.select(0);
                                allItems.remove(newSelection);
                            }
                            computeSelectionItems(index + 1, allItems);
                        }
                    }
                });
            } else {
                priorityCombos[i].addSelectionListener(new SelectionAdapter() {
                    @Override
					public void widgetSelected(SelectionEvent e) {
                        markDirty();
                    }
                });
            }
        }

        // set widget's values from sorter data
        // (combos and radio buttons)
        updateUIFromSorter();
    }

    /**
     * Get the descending label for the Descending field at i. Use the index to determine the mnemonic.
     *
     * @param index
     * @return String
     */
    private String getDescendingText(int index) {
        switch (index) {
        case 1:
            return STDataViewersMessages.sortDirectionDescending_text2;
        case 2:
            return STDataViewersMessages.sortDirectionDescending_text3;
        case 3:
            return STDataViewersMessages.sortDirectionDescending_text4;
        default:
            return STDataViewersMessages.sortDirectionDescending_text;
        }

    }

    /**
     * Get the ascending label for the Ascending field at i. Use the index to determine the mnemonic.
     *
     * @param index
     * @return String
     */
    private String getAscendingText(int index) {
        switch (index) {
        case 1:
            return STDataViewersMessages.sortDirectionAscending_text2;
        case 2:
            return STDataViewersMessages.sortDirectionAscending_text3;
        case 3:
            return STDataViewersMessages.sortDirectionAscending_text4;
        default:
            return STDataViewersMessages.sortDirectionAscending_text;
        }

    }

    /**
     * Create the restore defaults button.
     *
     * @param parent
     */
    private void createRestoreDefaultsButton(Composite parent) {
        Button defaultsButton = new Button(parent, SWT.PUSH);
        defaultsButton.setText(STDataViewersMessages.restoreDefaults_text);
        setButtonSize(defaultsButton, new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        defaultsButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                restoreDefaults();
                markDirty();
            }
        });
    }

    private void createSeparatorLine(Composite parent) {
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
    }

    private void restoreDefaults() {
        updateUI(null, null);
    }

    private void updateUIFromSorter() {
        updateUI(sorter.getPriorities(), sorter.getDirections());
    }

    private void updateUI(int[] priorities, int[] directions) {
        Item[] columns = sorter.getColumns();
        List<String> allItems = new ArrayList<String>();
        List<Integer> allDirections = new ArrayList<Integer>();
        for (int i = 0; i < columns.length; i++) {
            if (priorities == null || directions == null) {
                allItems.add(columns[i].getText());
                ISTDataViewersField field = (ISTDataViewersField) columns[i].getData();
                allDirections.add(field.getDefaultDirection());
            } else {
                allItems.add(columns[priorities[i]].getText());
                allDirections.add(directions[priorities[i]]);
            }
        }

        for (int i = 0; i < priorityCombos.length; i++) {
            priorityCombos[i].removeAll();
            priorityCombos[i].setItems(allItems.toArray(new String[allItems.size()]));
            priorityCombos[i].select(0);
            allItems.remove(0);
            ascendingButtons[i].setSelection(allDirections.get(0) == STDataViewersComparator.ASCENDING);
            descendingButtons[i].setSelection(allDirections.get(0) == STDataViewersComparator.DESCENDING);
            allDirections.remove(0);
        }
    }

    @Override
    public int open() {
        dirty = false;
        return super.open();
    }

    @Override
	protected void okPressed() {
        if (isDirty()) {
            outerfor: for (int i = priorityCombos.length - 1; i >= 0; i--) {
                Combo combo = priorityCombos[i];
                int index = combo.getSelectionIndex();
                String item = combo.getItem(index);
                Item[] columns = sorter.getColumns();
                for (Item column : columns) {
                    if (item.equals(column.getText())) {
                        ISTDataViewersField field = (ISTDataViewersField) column.getData();
                        sorter.setTopPriority(column, field);
                        int direction = STDataViewersComparator.ASCENDING;
                        if (descendingButtons[i].getSelection()) {
                            direction = STDataViewersComparator.DESCENDING;
                        }
                        sorter.setTopPriorityDirection(direction);
                        continue outerfor;
                    }
                }
                sorter.resetState();
                return;
            }
        }
        super.okPressed();
    }

    /**
     * @return boolean
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the dirty flag to true.
     */
    public void markDirty() {
        dirty = true;
    }

    /**
     * Set the layout data of the button to a GridData with appropriate heights and widths.
     *
     * @param button
     */
    private void setButtonSize(Button button, GridData buttonData) {
        button.setFont(button.getParent().getFont());
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        buttonData.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        button.setLayoutData(buttonData);
    }

    /**
     * Return the sorter for the receiver.
     *
     * @return TableSorter
     */
    public STDataViewersComparator getSorter() {
        return new STDataViewersComparator(sorter);
    }

}
