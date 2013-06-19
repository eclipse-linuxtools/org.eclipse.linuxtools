/*******************************************************************************
 * Copyright (c) 2012 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.LineParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetFactory;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.SelectGraphAndSeriesWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SystemTapScriptGraphOptionsTab extends
		AbstractLaunchConfigurationTab {

	static final String RUN_WITH_CHART = "runWithChart"; //$NON-NLS-1$
	static final String NUMBER_OF_COLUMNS = "numberOfColumns"; //$NON-NLS-1$
	static final String REGEX_BOX = "regexBox_"; //$NON-NLS-1$
	static final String REGULARE_EXPRESSION = "regularExpression"; //$NON-NLS-1$
	static final String SAMPLE_OUTPUT = "sampleOutput"; //$NON-NLS-1$

	private static final String NUMBER_OF_GRAPHS = "numberOfGraphs"; //$NON-NLS-1$
	private static final String GRAPH_TITLE = "graphTitle"; //$NON-NLS-1$
	private static final String GRAPH_KEY = "graphKey"; //$NON-NLS-1$
	private static final String GRAPH_X_SERIES = "graphXSeries"; //$NON-NLS-1$
	private static final String GRAPH_ID = "graphID"; //$NON-NLS-1$
	private static final String GRAPH_Y_SERIES_LENGTH = "graphYSeriesLength"; //$NON-NLS-1$
	private static final String GRAPH_Y_SERIES = "graphYSeries"; //$NON-NLS-1$
	protected Pattern pattern;
	protected Matcher matcher;

	private ModifyListener regExListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			refreshRegexRows();
			updateLaunchConfigurationDialog();
		}
	};

	private ModifyListener columnNameListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			updateLaunchConfigurationDialog();
		}
	};

	private Text regularExpressionText;
	private Composite textFieldsComposite;

	private ScrolledComposite regexTextScrolledComposite;
	private Group outputParsingGroup;
	private Button runWithChartCheckButton;

	private Table graphsTable;
	private Button addGraphButton, duplicateGraphButton, editGraphButton, removeGraphButton;
	private TableItem selectedTableItem;
	private Group graphsGroup;
	private Text sampleOutputText;
	private int numberOfVisibleColumns = 0;
	private boolean graphingEnabled = true;
	private String regexErrorMessage;
	private Stack<String> cachedNames = new Stack<String>();

	public static IDataSetParser createDatasetParser(ILaunchConfiguration configuration) {
		try {
			return new LineParser(configuration.getAttribute(REGULARE_EXPRESSION, "").concat("\\n")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_0, e);
		}
		return null;
	}

	public static IDataSet createDataset(ILaunchConfiguration configuration) {
		int n;
		try {
			n = configuration.getAttribute(NUMBER_OF_COLUMNS, 0);
			ArrayList<String> labels = new ArrayList<String>(n);

			for (int i = 0; i < n; i++) {
				String text = configuration.getAttribute(REGEX_BOX + i, (String) null);
				labels.add(text);
			}

			return DataSetFactory.createDataSet(RowDataSet.ID, labels.toArray(new String[] {}));
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_1, e);
		}
		return null;
	}

	public static LinkedList<GraphData> createGraphsFromConfiguration (ILaunchConfiguration configuration) throws CoreException {
		LinkedList<GraphData> graphs = new LinkedList<GraphData>();
		int n = configuration.getAttribute(NUMBER_OF_GRAPHS, 0);
		for (int i = 0; i < n; i++) {
			GraphData graphData = new GraphData();
			graphData.title = configuration.getAttribute (GRAPH_TITLE + i, ""); //$NON-NLS-1$

			graphData.key = configuration.getAttribute(GRAPH_KEY + i, ""); //$NON-NLS-1$
			graphData.xSeries = configuration.getAttribute(GRAPH_X_SERIES + i, 0);
			graphData.graphID = configuration.getAttribute(GRAPH_ID + i, ""); //$NON-NLS-1$

			int ySeriesLength = configuration.getAttribute(GRAPH_Y_SERIES_LENGTH + i, 0);
			int[] ySeries = new int[ySeriesLength];
			for (int j = 0; j < ySeriesLength; j++) {
				ySeries[j] = configuration.getAttribute(GRAPH_Y_SERIES + i + "_" + j, 0); //$NON-NLS-1$
			}
			graphData.ySeries = ySeries;

			graphs.add(graphData);
		}

		return graphs;
	}

	@Override
	public void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(layout);
		top.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true));

		this.runWithChartCheckButton = new Button(top, SWT.CHECK);
		runWithChartCheckButton.setText(Messages.SystemTapScriptGraphOptionsTab_2);
		runWithChartCheckButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setGraphingEnabled(runWithChartCheckButton.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setGraphingEnabled(runWithChartCheckButton.getSelection());
			}
		});

		runWithChartCheckButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_3);

		this.outputParsingGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		outputParsingGroup.setText(Messages.SystemTapScriptGraphOptionsTab_4);
		outputParsingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		this.createColumnSelector(outputParsingGroup);

		this.graphsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		graphsGroup.setText(Messages.SystemTapScriptGraphOptionsTab_graphsTitle);
		graphsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createGraphCreateArea(graphsGroup);

		setGraphingEnabled(false);
		runWithChartCheckButton.setSelection(false);
	}

	protected void createColumnSelector(Composite parent) {

		GridLayout layout = new GridLayout();
		parent.setLayout(layout);

		GridLayout twoColumns = new GridLayout();
		twoColumns.numColumns = 2;

		Composite regexSummaryComposite = new Composite(parent, SWT.NONE);
		regexSummaryComposite.setLayout(twoColumns);
		regexSummaryComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label regularExpressionLabel = new Label(regexSummaryComposite, SWT.NONE);
		regularExpressionLabel.setText(Messages.ParsingWizardPage_RegularExpression + ":"); //$NON-NLS-1$
		regularExpressionLabel.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_regexTooltip);
		regularExpressionText = new Text(regexSummaryComposite, SWT.BORDER);
		regularExpressionText.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_regexTooltip);
		regularExpressionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		regularExpressionText.addModifyListener(regExListener);

		Label sampleOutputLabel = new Label(regexSummaryComposite, SWT.NONE);
		sampleOutputLabel.setText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
		sampleOutputLabel.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputTooltip);
		this.sampleOutputText = new Text(regexSummaryComposite, SWT.BORDER);
		this.sampleOutputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.sampleOutputText.addModifyListener(regExListener);
		sampleOutputText.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputTooltip);

		GridLayout threeColumnLayout = new GridLayout();
		threeColumnLayout.numColumns = 3;
		threeColumnLayout.makeColumnsEqualWidth = true;
		Composite expressionTableLabels = new Composite(parent, SWT.NONE);
		expressionTableLabels.setLayout(threeColumnLayout);
		expressionTableLabels.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label label = new Label(expressionTableLabels, SWT.NONE);
		label.setText(Messages.ParsingWizardPage_Title);
		label.setAlignment(SWT.CENTER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 200;

		label.setLayoutData(data);

		label = new Label(expressionTableLabels, SWT.NONE);
		label.setText(Messages.SystemTapScriptGraphOptionsTab_extractedValueLabel);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.regexTextScrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint = 200;
		regexTextScrolledComposite.setLayoutData(data);

		textFieldsComposite = new Composite(regexTextScrolledComposite, SWT.NONE);
		textFieldsComposite.setLayout(twoColumns);
		textFieldsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		regexTextScrolledComposite.setContent(textFieldsComposite);
		regexTextScrolledComposite.setExpandHorizontal(true);
		regexTextScrolledComposite.setExpandVertical(false);

		refreshRegexRows();
	}

	private IDataSet getDataset() {
		Control[] textBoxes = this.textFieldsComposite.getChildren();
		int numberOfColumns = textBoxes.length/2;
		ArrayList<String> labels = new ArrayList<String>(numberOfColumns);

		for (int i = 0; i < numberOfColumns; i++) {
			String text = ((Text)textBoxes[i*2]).getText();
			labels.add(text);
		}
		return DataSetFactory.createDataSet(RowDataSet.ID, labels.toArray(new String[] {}));
	}

	private void createGraphCreateArea(Composite comp){
		GridLayout twoColumnsLayout = new GridLayout();
		comp.setLayout(twoColumnsLayout);
		twoColumnsLayout.numColumns = 2;

		graphsTable = new Table(comp, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		graphsTable.setLayoutData(layoutData);

		// Button to add another graph
		Composite buttonComposite = new Composite(comp, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		buttonComposite.setLayout(gridLayout);
		// Button to add a new graph
		addGraphButton = new Button(buttonComposite, SWT.PUSH);
		addGraphButton.setText(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton);
		addGraphButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_AddGraphButtonToolTip);
		addGraphButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Button to copy an existing graph
		duplicateGraphButton = new Button(buttonComposite, SWT.PUSH);
		duplicateGraphButton.setText(Messages.SystemTapScriptGraphOptionsTab_DuplicateGraphButton);
		duplicateGraphButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_DuplicateGraphButtonToolTip);
		duplicateGraphButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Button to edit an existing graph
		editGraphButton = new Button(buttonComposite, SWT.PUSH);
		editGraphButton.setText(Messages.SystemTapScriptGraphOptionsTab_EditGraphButton);
		editGraphButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_EditGraphButtonToolTip);
		editGraphButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Button to remove the selected graph/filter
		removeGraphButton = new Button(buttonComposite, SWT.PUSH);
		removeGraphButton.setText(Messages.SystemTapScriptGraphOptionsTab_RemoveGraphButton);
		removeGraphButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_RemoveGraphButtonToolTip);
		removeGraphButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Action to notify the buttons when to enable/disable themselves based
		// on list selection
		graphsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedTableItem = (TableItem) e.item;
				setSelectionControlsEnabled(true);
			}
		});

		// Brings up a new dialog box when user clicks the add button. Allows
		// selecting a new graph to display.
		addGraphButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectGraphAndSeriesWizard wizard = new SelectGraphAndSeriesWizard(getDataset(), null);
				IWorkbench workbench = PlatformUI.getWorkbench();
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench
						.getActiveWorkbenchWindow().getShell(), wizard);
				dialog.create();
				dialog.open();

				GraphData gd = wizard.getGraphData();

				if (null != gd) {
					TableItem item = new TableItem(graphsTable, SWT.NONE);
					item.setText(GraphFactory.getGraphName(gd.graphID) + ":" //$NON-NLS-1$
							+ gd.title);
					item.setData(gd);
					updateLaunchConfigurationDialog();
				}
			}
		});

		// Adds a new entry to the list of graphs that is a copy of the one selected.
		duplicateGraphButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphData gd = (GraphData) selectedTableItem.getData();

				TableItem item = new TableItem(graphsTable, SWT.NONE);
				item.setText(GraphFactory.getGraphName(gd.graphID) + ":" //$NON-NLS-1$
						+ gd.title);
				item.setData(gd);
				updateLaunchConfigurationDialog();
			}
		});

		// When button is clicked, brings up same wizard as the one for adding
		// a graph. Data in the wizard is filled out to match the properties
		// of the selected graph.
		editGraphButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectGraphAndSeriesWizard wizard = new SelectGraphAndSeriesWizard(getDataset(),
						(GraphData) selectedTableItem.getData());
				IWorkbench workbench = PlatformUI.getWorkbench();
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench
						.getActiveWorkbenchWindow().getShell(), wizard);
				dialog.create();
				dialog.open();

				GraphData gd = wizard.getGraphData();

				if (null != gd) {
					selectedTableItem.setText(GraphFactory.getGraphName(gd.graphID) + ":" //$NON-NLS-1$
							+ gd.title);
					selectedTableItem.setData(gd);
					updateLaunchConfigurationDialog();
				}
			}
		});

		// Removes the selected graph/filter from the table
		removeGraphButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedTableItem.dispose();
				setSelectionControlsEnabled(false);
				updateLaunchConfigurationDialog();
			}
		});
	}

	private void refreshRegexRows() {

		try{
			pattern = Pattern.compile(regularExpressionText.getText());
			matcher = pattern.matcher(sampleOutputText.getText());
			this.regexErrorMessage = ""; //$NON-NLS-1$
		}catch (PatternSyntaxException e){
			this.regexErrorMessage = e.getMessage();
			return;
		}
		if (regularExpressionText.getText().contains("()")){ //$NON-NLS-1$
			this.regexErrorMessage = Messages.SystemTapScriptGraphOptionsTab_6;
			return;
		}

		int desiredNumberOfColumns =  matcher.groupCount();

		while (numberOfVisibleColumns < desiredNumberOfColumns){
			addColumn();
		}

		while (numberOfVisibleColumns > desiredNumberOfColumns){
			removeColumn();
		}

		// Set values
		Control[] children = textFieldsComposite.getChildren();
		for (int i = 0; i < numberOfVisibleColumns; i++) {
			if (!matcher.matches()){
				((Label)children[i*2+1]).setText(""); //$NON-NLS-1$
			} else {
				((Label)children[i*2+1]).setText(" " +matcher.group(i+1)); //$NON-NLS-1$
			}
		}

	}

	private void addColumn(){
		Text text = new Text(textFieldsComposite, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.minimumWidth = 200;
		data.widthHint = 200;
		text.setLayoutData(data);
		if (cachedNames.size() > 0) {
			text.setText(cachedNames.pop());
		}
		text.addModifyListener(columnNameListener);

		Label label = new Label(textFieldsComposite, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.numberOfVisibleColumns++;

		textFieldsComposite.layout();
		textFieldsComposite.pack();
	}

	private void removeColumn(){
		Control[] children = textFieldsComposite.getChildren();
		int i = this.numberOfVisibleColumns*2 -1;
		cachedNames.push(((Text)children[i-1]).getText());
		children[i].dispose();
		children[i-1].dispose();

		this.numberOfVisibleColumns--;

		textFieldsComposite.layout();
		textFieldsComposite.pack();
	}

	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RUN_WITH_CHART, false);
		configuration.setAttribute(NUMBER_OF_COLUMNS, 0);
		configuration.setAttribute(NUMBER_OF_GRAPHS, 0);
		configuration.setAttribute(NUMBER_OF_GRAPHS, 0);
		configuration.setAttribute(REGULARE_EXPRESSION, ""); //$NON-NLS-1$
		configuration.setAttribute(SAMPLE_OUTPUT, ""); //$NON-NLS-1$
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			boolean chart = configuration.getAttribute(RUN_WITH_CHART, false);
			setGraphingEnabled(chart);
			this.runWithChartCheckButton.setSelection(chart);

			regularExpressionText.setText(configuration.getAttribute(REGULARE_EXPRESSION, "")); //$NON-NLS-1$
			sampleOutputText.setText(configuration.getAttribute(SAMPLE_OUTPUT, "")); //$NON-NLS-1$

			int n = configuration.getAttribute(NUMBER_OF_COLUMNS, 0);
			Control[] textBoxes = this.textFieldsComposite.getChildren();

			for (int i = 0; i < n && i*2 < textBoxes.length; i++) {
				String text = configuration.getAttribute(REGEX_BOX+i, (String)null);
				if (text != null) {
					((Text)textBoxes[i*2]).setText(text);
				}
			}

			// Add graphs
			graphsTable.removeAll();
			LinkedList<GraphData> graphs = createGraphsFromConfiguration(configuration);
			for (GraphData graphData : graphs) {
				TableItem item = new TableItem(graphsTable, SWT.NONE);
				item.setText(GraphFactory.getGraphName(graphData.graphID) + ":" //$NON-NLS-1$
						+ graphData.title);
				item.setData(graphData);
			}

		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_5, e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RUN_WITH_CHART, this.runWithChartCheckButton.getSelection());

		configuration.setAttribute(REGULARE_EXPRESSION, regularExpressionText.getText());
		configuration.setAttribute(SAMPLE_OUTPUT, sampleOutputText.getText());

		Control[] textBoxes = this.textFieldsComposite.getChildren();
		int numberOfColumns = textBoxes.length/2;
		configuration.setAttribute(NUMBER_OF_COLUMNS, numberOfColumns);

		for (int i = 0; i < numberOfColumns; i++) {
			String text = ((Text)textBoxes[i*2]).getText();
			configuration.setAttribute(REGEX_BOX+i, text);
		}

		// Save graphs.
		TableItem[] list = this.graphsTable.getItems();
		configuration.setAttribute(NUMBER_OF_GRAPHS, list.length);
		for (int i = 0; i < list.length; i++) {
			GraphData graphData = (GraphData)list[i].getData();
			configuration.setAttribute(GRAPH_TITLE + i, graphData.title);

			configuration.setAttribute(GRAPH_KEY + i, graphData.key);
			configuration.setAttribute(GRAPH_X_SERIES + i, graphData.xSeries);
			configuration.setAttribute(GRAPH_ID + i, graphData.graphID);

			configuration.setAttribute(GRAPH_Y_SERIES_LENGTH + i, graphData.ySeries.length);
			for (int j = 0; j < graphData.ySeries.length; j++) {
				configuration.setAttribute(GRAPH_Y_SERIES + i + "_" + j, graphData.ySeries[j]); //$NON-NLS-1$
			}
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		// If graphic is disabled then everything is valid.
		if (!this.graphingEnabled){
			return true;
		}

		if (!this.regexErrorMessage.equals("")){ //$NON-NLS-1$
			setErrorMessage(regexErrorMessage);
			return false;
		}

		return true;
	}

	@Override
	public String getName() {
		return Messages.SystemTapScriptGraphOptionsTab_7;
	}

	@Override
	public Image getImage() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(IDEPlugin.PLUGIN_ID,
				"icons/graphing_tab.gif").createImage(); //$NON-NLS-1$
	}

	private void setGraphingEnabled(boolean enabled){
		if (this.graphingEnabled == enabled){
			return;
		}
		this.graphingEnabled = enabled;
		this.setControlEnabled(outputParsingGroup, enabled);
		this.setControlEnabled(graphsGroup, enabled);
		// Disable buttons that rely on a selected graph if no graph is selected.
		this.setSelectionControlsEnabled(selectedTableItem != null);
		updateLaunchConfigurationDialog();
	}

	private void setControlEnabled(Composite composite, boolean enabled){
		composite.setEnabled(enabled);
		for (Control child : composite.getChildren()) {
				child.setEnabled(enabled);
			if(child instanceof Composite){
				setControlEnabled((Composite)child, enabled);
			}
		}
	}

	/**
	 * Call this to enable/disable all buttons whose actions depend on a selected graph.
	 * @param enabled Set to true to enable the buttons; set to false to disable them.
	 */
	private void setSelectionControlsEnabled(boolean enabled) {
		duplicateGraphButton.setEnabled(enabled);
		editGraphButton.setEnabled(enabled);
		removeGraphButton.setEnabled(enabled);
	}
}
