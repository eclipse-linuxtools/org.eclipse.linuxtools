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
package org.eclipse.linuxtools.internal.dataviewers.charts.dialogs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField;
import org.eclipse.linuxtools.dataviewers.charts.provider.ChartFactory;
import org.eclipse.linuxtools.dataviewers.charts.provider.IChartField;
import org.eclipse.linuxtools.internal.dataviewers.charts.Activator;
import org.eclipse.linuxtools.internal.dataviewers.charts.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;

/**
 * The dialog used to customize the chart before creating it.
 */
public class ChartDialog extends Dialog {

    /** The section name of the viewer's dialog settings where the chart dialog save its state */
    private static final String TAG_SECTION_CHARTS_STATE = "charts_section"; //$NON-NLS-1$
    /**
     * The key used by the column buttons to save their state. For example the button i will use the key
     * <code>TAG_COLUMN_BUTTON_+i</code>
     */
    private static final String TAG_COLUMN_BUTTON_ = "COLUMN_BUTTON_"; //$NON-NLS-1$
    /** The key used by the bar graph button to save its state */
    private static final String TAG_BAR_GRAPH_BUTTON = "BAR_GRAPH_BUTTON"; //$NON-NLS-1$
    /** The key used by the vertical bars button to save its state */
    private static final String TAG_VERTICAL_BARS_BUTTON = "VERTICAL_BARS_BUTTON"; //$NON-NLS-1$

    /** The default value of the column buttons */
    private static final boolean DEFAULT_COLUMN_BUTTON = true;
    /** The default value of the bar graph button */
    private static final boolean DEFAULT_BAR_GRAPH_BUTTON = true;
    /** The default value of the vertical bars button */
    private static final boolean DEFAULT_VERTICAL_BARS_BUTTON = false;

    private final AbstractSTViewer stViewer;
    private Chart chart;

    private Text errorMessageText;
    private Button verticalBarsButton;
    private Button pieChartButton;
    private Button barGraphButton;
    private Button okButton;
    private List<Button> columnButtons;

    /**
     * The constructor
     *
     * @param shell
     * @param stViewer
     */
    public ChartDialog(Shell shell, AbstractSTViewer stViewer) {
        super(shell);
        this.stViewer = stViewer;
    }

    /**
     * Restores the state of this dialog
     */
    public void restoreState() {
            IDialogSettings settings = stViewer.getViewerSettings().getSection(TAG_SECTION_CHARTS_STATE);
            if (settings == null) {
                settings = stViewer.getViewerSettings().addNewSection(TAG_SECTION_CHARTS_STATE);
                return;
            }

            for (int i = 0; i < columnButtons.size(); i++) {
                boolean selected = Boolean.parseBoolean(settings.get(TAG_COLUMN_BUTTON_ + i));
                columnButtons.get(i).setSelection(selected);
            }

            boolean barGraph = Boolean.parseBoolean(settings.get(TAG_BAR_GRAPH_BUTTON));
            barGraphButton.setSelection(barGraph);
            pieChartButton.setSelection(!barGraph);

            boolean vBars = Boolean.parseBoolean(settings.get(TAG_VERTICAL_BARS_BUTTON));
            verticalBarsButton.setSelection(vBars);
            verticalBarsButton.setEnabled(barGraph);
    }

    /**
     * Saves the state of this dialog
     */
    public void saveState() {
            IDialogSettings settings = stViewer.getViewerSettings().getSection(TAG_SECTION_CHARTS_STATE);
            if (settings == null) {
                settings = stViewer.getViewerSettings().addNewSection(TAG_SECTION_CHARTS_STATE);
            }

            for (int i = 0; i < columnButtons.size(); i++) {
                boolean selected = columnButtons.get(i).getSelection();
                settings.put(TAG_COLUMN_BUTTON_ + i, selected);
            }

            boolean barGraph = barGraphButton.getSelection();
            settings.put(TAG_BAR_GRAPH_BUTTON, barGraph);

            boolean vBars = verticalBarsButton.getSelection();
            settings.put(TAG_VERTICAL_BARS_BUTTON, vBars);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.ChartConstants_CREATE_NEW_CHART_FROM_SELECTION);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            chart = produceChart();
            saveState();
        } else {
            chart = null;
        }
        super.buttonPressed(buttonId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Control c = super.createContents(parent);
        this.validateInput();
        return c;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite titleComp = new Composite(composite, SWT.NONE);
        titleComp.setLayout(new RowLayout(SWT.HORIZONTAL));

        Label icon = new Label(titleComp, SWT.NONE);
        icon.setImage(Activator.getImage("icons/chart_icon.png")); //$NON-NLS-1$

        Label label = new Label(titleComp, SWT.WRAP);
        label.setText(Messages.ChartConstants_CHART_BUILDER);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        titleComp.setLayoutData(data);

        Group chartTypeGroup = new Group(composite, SWT.NONE);
        data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        chartTypeGroup.setLayoutData(data);
        chartTypeGroup.setLayout(new GridLayout(2, false));
        chartTypeGroup.setText(Messages.ChartConstants_SELECT_YOUR_CHART_TYPE);

        ValidateSelectionListener listener = new ValidateSelectionListener();

        barGraphButton = new Button(chartTypeGroup, SWT.RADIO);
        barGraphButton.setText(Messages.ChartConstants_BAR_GRAPH);
        barGraphButton.addSelectionListener(listener);
        barGraphButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                verticalBarsButton.setEnabled(barGraphButton.getSelection());
            }
        });
        data = new GridData();
        barGraphButton.setLayoutData(data);

        verticalBarsButton = new Button(chartTypeGroup, SWT.CHECK);
        verticalBarsButton.setText(Messages.ChartConstants_VERTICAL_BARS);
        data = new GridData();
        verticalBarsButton.setLayoutData(data);

        pieChartButton = new Button(chartTypeGroup, SWT.RADIO);
        pieChartButton.setText(Messages.ChartConstants_PIE_CHART);
        pieChartButton.addSelectionListener(listener);
        data = new GridData();
        data.horizontalSpan = 2;
        pieChartButton.setLayoutData(data);

        Group chartColumnGroup = new Group(composite, SWT.NONE);
        chartColumnGroup.setLayout(new GridLayout(1, true));
        data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        chartColumnGroup.setLayoutData(data);
        chartColumnGroup.setText(Messages.ChartConstants_SELECT_COLUMNS_TO_SHOW);

        addColumnButtons(chartColumnGroup, listener);

        errorMessageText = new Text(composite, SWT.READ_ONLY);
        errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        applyDialogFont(composite);

        setWidgetsValues();

        return composite;
    }

    /**
     * Sets the widgets values
     */
    private void setWidgetsValues() {
        // set default values
        barGraphButton.setSelection(DEFAULT_BAR_GRAPH_BUTTON);
        verticalBarsButton.setEnabled(barGraphButton.getSelection());
        verticalBarsButton.setSelection(DEFAULT_VERTICAL_BARS_BUTTON);
        for (Button button : columnButtons) {
            button.setSelection(DEFAULT_COLUMN_BUTTON);
        }

        // restore state if there is one saved
        restoreState();
    }

    /**
     * Adds one check button for each column implementing the IChartField interface.
     *
     * @see IChartField
     * @param comp
     * @param listener
     */
    private void addColumnButtons(Composite comp, SelectionListener listener) {
        columnButtons = new LinkedList<Button>();
        for (ISTDataViewersField field : stViewer.getAllFields()) {
            if (field instanceof IChartField) {
                IChartField cField = (IChartField) field;
                Button b = new Button(comp, SWT.CHECK);
                b.setText(cField.getColumnHeaderText());
                b.setData(cField);
                b.addSelectionListener(listener);
                GridData dt = new GridData();
                b.setLayoutData(dt);
                columnButtons.add(b);
            }
        }

        Label sep = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        sep.setLayoutData(data);
        Composite buttonComposite = new Composite(comp, SWT.NONE);
        data = new GridData();
        buttonComposite.setLayoutData(data);
        FillLayout l = new FillLayout();
        l.spacing = 5;
        buttonComposite.setLayout(l);

        final Button b1 = new Button(buttonComposite, SWT.PUSH);
        b1.setText(Messages.ChartConstants_SELECT_ALL);
        final Button b2 = new Button(buttonComposite, SWT.PUSH);
        b2.setText(Messages.ChartConstants_DESELECT_ALL);
        SelectionListener sl = new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean b = (e.getSource() == b1);
                for (Button button : columnButtons) {
                    button.setSelection(b);
                }
                validateInput();
            }
        };
        b1.addSelectionListener(sl);
        b2.addSelectionListener(sl);
    }

    /**
     * Returns the Chart built by this dialog
     *
     * @return the chart
     */
    public Chart getValue() {
        return chart;
    }

    /**
     * Validates the input.
     * <p>
     * The default implementation of this framework method delegates the request to the supplied input validator object;
     * if it finds the input invalid, the error message is displayed in the dialog's message line. This hook method is
     * called whenever the text changes in the input field.
     * </p>
     */
    protected void validateInput() {
        String errorMessage = null;

        int selectedNum = 0;
        for (Button button : columnButtons) {
            if (button.getSelection())
                selectedNum++;
        }

        if (selectedNum == 0) {
            errorMessage = Messages.ChartConstants_NO_COLUMN_SELECTED;
        }
        /*
         * else if (pieChartButton.getSelection() && selectedNum != 1) { errorMessage =
         * "PieChart: Please select only one column"; }
         */

        // Bug 16256: important not to treat "" (blank error) the same as null
        // (no error)
        setErrorMessage(errorMessage);
    }

    /**
     * Sets or clears the error message. If not <code>null</code>, the OK button is disabled.
     *
     * @param errorMessage
     *            the error message, or <code>null</code> to clear
     * @since 3.0
     */
    public void setErrorMessage(String errorMessage) {
        errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
        okButton.setEnabled(errorMessage == null);
        errorMessageText.getParent().update();
    }

    /**
     * Build the chart from configuration
     *
     * @return a new chart
     */
    private Chart produceChart() {
        IStructuredSelection selection = (IStructuredSelection) stViewer.getViewer().getSelection();
        if (selection == StructuredSelection.EMPTY)
            return null;
        Object[] objects = selection.toArray();

        ISTDataViewersField labelField = getLabelField(stViewer);

        List<IChartField> selectedFields = new ArrayList<IChartField>();
        for (Button button : columnButtons) {
            if (button.getSelection()) {
                selectedFields.add((IChartField) button.getData());
            }
        }
        boolean barChartType = barGraphButton.getSelection();
        boolean horizontalBars = !verticalBarsButton.getSelection();

        if (barChartType) {
            return ChartFactory
                    .produceBarChart(objects, labelField, selectedFields, Messages.ChartConstants_BAR_GRAPH, horizontalBars);
        } else {
            return ChartFactory.producePieChart(objects, labelField, selectedFields, Messages.ChartConstants_PIE_CHART);
        }
    }

    private class ValidateSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            validateInput();
        }
    }

    /**
     * @param viewer
     * @return the field used to provide the labels to the series
     */
    protected ISTDataViewersField getLabelField(AbstractSTViewer viewer) {
        return viewer.getAllFields()[0];
    }
}