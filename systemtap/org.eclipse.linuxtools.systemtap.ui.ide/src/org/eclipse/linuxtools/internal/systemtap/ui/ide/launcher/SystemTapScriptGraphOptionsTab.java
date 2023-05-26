/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.LineParser;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.graphing.ui.wizards.dataset.DataSetFactory;
import org.eclipse.linuxtools.systemtap.graphing.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.graphing.ui.wizards.graph.SelectGraphAndSeriesWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class SystemTapScriptGraphOptionsTab extends
            AbstractLaunchConfigurationTab {

    /**
     * The maximum number of regular expressions that can be stored in a configuration.
     */
    static final int MAX_NUMBER_OF_REGEXS = 20;

    /**
     * The maximum length of an output-parsing regular expression.
     */
    static final int MAX_REGEX_LENGTH = 200;

    // Note: any non-private String key with a trailing underscore is to be appended with an integer when looking up values.
    static final String RUN_WITH_CHART = "runWithChart"; //$NON-NLS-1$
    static final String NUMBER_OF_REGEXS = "numberOfRegexs"; //$NON-NLS-1$
    static final String NUMBER_OF_COLUMNS = "numberOfColumns_"; //$NON-NLS-1$
    static final String REGEX_BOX = "regexBox_"; //$NON-NLS-1$
    static final String NUMBER_OF_EXTRAS = "numberOfExtras_"; //$NON-NLS-1$
    static final String EXTRA_BOX = "extraBox_"; //$NON-NLS-1$
    static final String REGULAR_EXPRESSION = "regularExpression_"; //$NON-NLS-1$
    static final String SAMPLE_OUTPUT = "sampleOutput_"; //$NON-NLS-1$

    // Note: all graph-related keys point to 2D lists (regular expression & graph number),
    // except for GRAPH_Y_SERIES (which is a 3D list).
    private static final String NUMBER_OF_GRAPHS = "numberOfGraphs"; //$NON-NLS-1$
    private static final String GRAPH_TITLE = "graphTitle"; //$NON-NLS-1$
    private static final String GRAPH_KEY = "graphKey"; //$NON-NLS-1$
    private static final String GRAPH_X_SERIES = "graphXSeries"; //$NON-NLS-1$
    private static final String GRAPH_ID = "graphID"; //$NON-NLS-1$
    private static final String GRAPH_Y_SERIES_LENGTH = "graphYSeriesLength"; //$NON-NLS-1$
    private static final String GRAPH_Y_SERIES = "graphYSeries"; //$NON-NLS-1$
    protected Pattern pattern;
    protected Matcher matcher;

    private Combo regularExpressionCombo;
    private Button removeRegexButton;

    private Text sampleOutputText;
    private Composite textFieldsComposite;

    /**
     * This value controls whether or not the ModifyListeners associated with
     * the Texts will perform when dispatched. Sometimes the listeners should
     * be disabled to prevent needless/unsafe operations.
     */
    private boolean textListenersEnabled = true;

    private Group outputParsingGroup;
    private Button runWithChartCheckButton;

    private Table graphsTable;
    private Button addGraphButton, duplicateGraphButton, editGraphButton, removeGraphButton;
    private TableItem selectedTableItem;
    private Group graphsGroup;
    private int numberOfVisibleColumns = 0;
    private boolean graphingEnabled = true;

    /**
     * A list of error messages, each entry corresponding to an entered regular expression.
     */
    private List<String> regexErrorMessages = new ArrayList<>();

    /**
     * The index of the selected regular expression.
     */
    private int selectedRegex = -1;

    /**
     * A list containing the user-defined sample outputs associated with the regex of every index.
     */
    private List<String> outputList = new ArrayList<>();

    /**
     * A name is given to each group captured by a regular expression. This stack contains
     * the names of all of a regex's groups that have been deleted, so each name may be
     * restored (without having to retype it) when a group is added again.
     */
    private Stack<String> cachedNames = new Stack<>();

    /**
     * A list of cachedNames stacks, containing one entry for each regular expression stored.
     */
    private List<Stack<String>> cachedNamesList = new ArrayList<>();

    /**
     * A two-dimensional list that holds references to the names given to each regular expression's captured groups.
     */
    private List<List<String>> columnNamesList = new ArrayList<>();

    /**
     * A list holding the data of every graph for the selected regular expression.
     */
    private List<GraphData> graphsData = new LinkedList<>();

    /**
     * A list of graphsData lists. This is needed because each regular expression has its own set of graphs.
     */
    private List<LinkedList<GraphData>> graphsDataList = new ArrayList<>();

    /**
     * A list of GraphDatas that rely on series information that has been deleted from their relying regex.
     */
    private List<GraphData> badGraphs = new LinkedList<>();

    private ModifyListener regexListener = event -> {
	    if (!textListenersEnabled || regularExpressionCombo.getSelectionIndex() != -1) {
	        return;
	    }
	    regularExpressionCombo.setItem(selectedRegex, regularExpressionCombo.getText());
	    regularExpressionCombo.select(selectedRegex);
	    refreshRegexRows();
	    updateLaunchConfigurationDialog();
	};

    private ModifyListener sampleOutputListener = event -> {
	    if (!textListenersEnabled) {
	        return;
	    }
	    outputList.set(selectedRegex, sampleOutputText.getText());
	    refreshRegexRows();
	    updateLaunchConfigurationDialog();
	};

    private ModifyListener columnNameListener = event -> {
	    if (!textListenersEnabled) {
	        return;
	    }

	    ArrayList<String> columnNames = new ArrayList<>();
	    Control[] children = textFieldsComposite.getChildren();
	    for (int i = 0; i < numberOfVisibleColumns; i++) {
	        columnNames.add(((Text)children[i*4 + 2]).getText());
	    }
	    columnNamesList.set(selectedRegex, columnNames);
	    updateLaunchConfigurationDialog();
	};

	private SelectionListener regexGenerator = SelectionListener.widgetSelectedAdapter(e -> {
		MessageDialog dialog;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IPath scriptPath = null;
		for (ILaunchConfigurationTab tab : getLaunchConfigurationDialog().getTabs()) {
			if (tab instanceof SystemTapScriptLaunchConfigurationTab) {
				scriptPath = ((SystemTapScriptLaunchConfigurationTab) tab).getScriptPath();
				break;
			}
		}
		if (scriptPath == null) {
			dialog = new MessageDialog(workbench.getActiveWorkbenchWindow().getShell(),
					Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsErrorTitle, null,
					Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsError, MessageDialog.ERROR,
					new String[] { "OK" }, 0); //$NON-NLS-1$
			dialog.open();
			return;
		}

		dialog = new MessageDialog(workbench.getActiveWorkbenchWindow().getShell(),
				Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsTitle, null,
				Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsMessage, MessageDialog.QUESTION,
				new String[] { "Yes", "Cancel" }, 0); //$NON-NLS-1$ //$NON-NLS-2$
		int result = dialog.open();
		if (result != 0) { // Cancel
			return;
		}

		List<Entry<String, Integer>> regexs = SystemTapRegexGenerator.generateFromPrintf(scriptPath,
				MAX_NUMBER_OF_REGEXS);

		if (regexs.size() == 0) {
			dialog = new MessageDialog(workbench.getActiveWorkbenchWindow().getShell(),
					Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsErrorTitle, null,
					Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsEmpty, MessageDialog.ERROR,
					new String[] { "OK" }, 0); //$NON-NLS-1$
			dialog.open();
		} else {
			// Since script output has been found, reset the configuration's regexs.
			textListenersEnabled = false;
			regularExpressionCombo.removeAll();
			outputList.clear();
			regexErrorMessages.clear();
			columnNamesList.clear();
			cachedNamesList.clear();
			graphsTable.removeAll();
			graphsDataList.clear();
			badGraphs.clear();
			for (int i = 0, n = regexs.size(); i < n; i++) {
				List<String> columnNames = new ArrayList<>();
				for (int c = 0, numColumns = regexs.get(i).getValue(); c < numColumns; c++) {
					columnNames.add(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase,
							c + 1));
				}
				regularExpressionCombo.add(regexs.get(i).getKey());
				outputList.add(""); //$NON-NLS-1$ //For empty "sample output" entry.
				regexErrorMessages.add(null);
				columnNamesList.add(columnNames);
				cachedNamesList.add(new Stack<>());
				graphsDataList.add(new LinkedList<>());
			}
			if (getNumberOfRegexs() < MAX_NUMBER_OF_REGEXS) {
				regularExpressionCombo.add(Messages.SystemTapScriptGraphOptionsTab_regexAddNew);
			}
			textListenersEnabled = true;

			removeRegexButton.setEnabled(getNumberOfRegexs() > 1);
			regularExpressionCombo.select(0);
			updateRegexSelection(0, true);
			checkAllOtherErrors(); // Check for errors in case there was a problem with regex generation
			updateLaunchConfigurationDialog();
		}
	});

    /**
     * Returns the list of the names given to reach regular expression.
     * @param configuration
     * @return
     */
    public static List<String> createDatasetNames(ILaunchConfiguration configuration) {
        try {
            int numberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);
            ArrayList<String> names = new ArrayList<>(numberOfRegexs);
            for (int r = 0; r < numberOfRegexs; r++) {
                names.add(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphSetTitleBase, r + 1));
            }
            return names;
        } catch (CoreException e) {
            ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_cantInitializeTab, e);
        }
        return null;
    }

    /**
     * Creates a list of parsers, one for each regular expression created, that will be used
     * to parse the output of a running script.
     * @param configuration The desired run configuration.
     * @return A list of parsers.
     */
    public static List<IDataSetParser> createDatasetParsers(ILaunchConfiguration configuration) {
        try {
            int numberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);
            ArrayList<IDataSetParser> parsers = new ArrayList<>(numberOfRegexs);
            for (int r = 0; r < numberOfRegexs; r++) {
                parsers.add(new LineParser("^" + configuration.getAttribute(REGULAR_EXPRESSION + r, "") + "$")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            return parsers;
        } catch (CoreException e) {
            ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_cantInitializeTab, e);
        }
        return null;
    }

    /**
     * Creates a data set corresponding to the titles given to each output column
     * from each of a run configuration's regular expressions.
     * @param configuration
     * @return
     */
    public static List<IFilteredDataSet> createDataset(ILaunchConfiguration configuration) {
        try {
            int numberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);
            ArrayList<IFilteredDataSet> datasets = new ArrayList<>(numberOfRegexs);

            for (int r = 0; r < numberOfRegexs; r++) {
                int numberOfColumns = configuration.getAttribute(NUMBER_OF_COLUMNS + r, 0);
                ArrayList<String> labels = new ArrayList<>(numberOfColumns);

                for (int c = 0; c < numberOfColumns; c++) {
                    labels.add(configuration.getAttribute(get2DConfigData(REGEX_BOX, r, c), "")); //$NON-NLS-1$
                }
                datasets.add(DataSetFactory.createFilteredDataSet(RowDataSet.ID, labels.toArray(new String[] {})));
            }

            return datasets;
        } catch (CoreException e) {
            ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_cantInitializeTab, e);
        }
        return null;
    }

    /**
     * Creates graph data corresponding to the graphs that will plot a script's parsed output data.
     * @param configuration The desired run configuration.
     * @return A data set.
     */
    public static List<LinkedList<GraphData>> createGraphsFromConfiguration (ILaunchConfiguration configuration)
            throws CoreException {
        // Restrict number of regexs to at least one, so at least
        // one inner list will exist in the return value.
        int numberOfRegexs = Math.max(configuration.getAttribute(NUMBER_OF_REGEXS, 1), 1);
        ArrayList<LinkedList<GraphData>> graphsList = new ArrayList<>(numberOfRegexs);

        for (int r = 0; r < numberOfRegexs; r++) {
            int numberOfGraphs = configuration.getAttribute(NUMBER_OF_GRAPHS + r, 0);
            LinkedList<GraphData> graphs = new LinkedList<>();
            for (int i = 0; i < numberOfGraphs; i++) {
                GraphData graphData = new GraphData();
                graphData.title = configuration.getAttribute(get2DConfigData(GRAPH_TITLE, r, i), (String) null);

                graphData.key = configuration.getAttribute(get2DConfigData(GRAPH_KEY, r, i), (String) null);
                graphData.xSeries = configuration.getAttribute(get2DConfigData(GRAPH_X_SERIES, r, i), 0);
                graphData.graphID = configuration.getAttribute(get2DConfigData(GRAPH_ID, r, i), (String) null);

                int ySeriesLength = configuration.getAttribute(get2DConfigData(GRAPH_Y_SERIES_LENGTH, r, i), 0);
                if (ySeriesLength == 0) {
                    graphData.ySeries = null;
                } else {
                    int[] ySeries = new int[ySeriesLength];
                    for (int j = 0; j < ySeriesLength; j++) {
                        ySeries[j] = configuration.getAttribute(get2DConfigData(GRAPH_Y_SERIES, r, i + "_" + j), 0); //$NON-NLS-1$
                    }
                    graphData.ySeries = ySeries;
                }

                graphs.add(graphData);
            }
            graphsList.add(graphs);
        }

        return graphsList;
    }

    /**
     * Returns the key associated with the i'th data item of the r'th regular expression.
     * @param configDataName The type of data to access from the configuration.
     * @param r The index of the regular expression.
     * @param i The index of the data item to access.
     */
    private static String get2DConfigData(String configDataName, int r, int i) {
        return configDataName + r + "_" + i; //$NON-NLS-1$
    }

    /**
     * Returns the key associated with the data item of the r'th regular expression, tagged by string s.
     * @param configDataName The type of data to access from the configuration.
     * @param r The index of the regular expression.
     * @param s The string to put at the end of the key.
     */
    private static String get2DConfigData(String configDataName, int r, String s) {
        return configDataName + r + "_" + s; //$NON-NLS-1$
    }

    /**
     * Returns the total number of regular expressions of the current configuration.
     */
    private int getNumberOfRegexs() {
        return outputList.size();
    }

    @Override
    public void createControl(Composite parent) {
        GridLayout layout = new GridLayout();
        Composite top = new Composite(parent, SWT.NONE);
        setControl(top);
        top.setLayout(layout);
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.runWithChartCheckButton = new Button(top, SWT.CHECK);
        runWithChartCheckButton.setText(Messages.SystemTapScriptGraphOptionsTab_graphOutputRun);
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

        runWithChartCheckButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_graphOutput);

        this.outputParsingGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
        outputParsingGroup.setText(Messages.SystemTapScriptGraphOptionsTab_outputLabel);
        outputParsingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        this.createColumnSelector(outputParsingGroup);

        this.graphsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
        // Set the text here just to allow proper sizing.
        graphsGroup.setText(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphSetTitleBase, 1));
        graphsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createGraphCreateArea(graphsGroup);

        setGraphingEnabled(false);
        runWithChartCheckButton.setSelection(false);
    }

    private void createColumnSelector(Composite parent) {

        GridLayout layout = new GridLayout();
        parent.setLayout(layout);

        Composite topLayout = new Composite(parent, SWT.NONE);
        topLayout.setLayout(new GridLayout(1, false));
        topLayout.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        Button generateExpsButton = new Button(topLayout, SWT.PUSH);
        generateExpsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        generateExpsButton.setText(Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsButton);
        generateExpsButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_generateFromPrintsTooltip);
        generateExpsButton.addSelectionListener(regexGenerator);

        Composite regexButtonLayout = new Composite(parent, SWT.NONE);
        regexButtonLayout.setLayout(new GridLayout(3, false));
        regexButtonLayout.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label selectedRegexLabel = new Label(regexButtonLayout, SWT.NONE);
        selectedRegexLabel.setText(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
        selectedRegexLabel.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_regexTooltip);
        regularExpressionCombo = new Combo(regexButtonLayout, SWT.DROP_DOWN);
        regularExpressionCombo.setTextLimit(MAX_REGEX_LENGTH);
        regularExpressionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		regularExpressionCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			int selected = regularExpressionCombo.getSelectionIndex();
			if (selected == selectedRegex) {
				return;
			}

			// If deselecting an empty regular expression, delete it automatically.
			if (regularExpressionCombo.getItem(selectedRegex).isEmpty() && graphsDataList.get(selectedRegex).size() == 0
					&& outputList.get(selectedRegex).isEmpty()) {

				// If the deselected regex is the last one in the combo, just quit.
				// Otherwise, the deleted blank entry would be replaced by another blank entry.
				if (selected == regularExpressionCombo.getItemCount() - 1) {
					regularExpressionCombo.select(selectedRegex); // To keep the text blank.
					return;
				}
				removeRegex(false);
				if (selected > selectedRegex) {
					selected--;
				}
			}

			// When selecting the "Add New Regex" item in the combo (which is always the
			// last item),
			// update all appropriate values to make room for a new regular expression.
			if (selected == regularExpressionCombo.getItemCount() - 1 && getNumberOfRegexs() < MAX_NUMBER_OF_REGEXS) {
				outputList.add(""); //$NON-NLS-1$
				regexErrorMessages.add(null);
				columnNamesList.add(new ArrayList<>());
				cachedNamesList.add(new Stack<>());
				graphsDataList.add(new LinkedList<>());

				// Remove "Add New Regex" from the selected combo item; make it blank.
				regularExpressionCombo.setItem(selected, ""); //$NON-NLS-1$
				regularExpressionCombo.select(selected);
				updateRegexSelection(selected, false);
				updateLaunchConfigurationDialog();

				// Enable the "remove" button if only one item was present before.
				// (Don't do this _every_ time something is added.)
				if (getNumberOfRegexs() == 2) {
					removeRegexButton.setEnabled(true);
				}
				if (getNumberOfRegexs() < MAX_NUMBER_OF_REGEXS) {
					regularExpressionCombo.add(Messages.SystemTapScriptGraphOptionsTab_regexAddNew);
				}
			} else {
				updateRegexSelection(selected, false);
			}
		}));
        regularExpressionCombo.addModifyListener(regexListener);

        removeRegexButton = new Button(regexButtonLayout, SWT.PUSH);
        removeRegexButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        removeRegexButton.setText(Messages.SystemTapScriptGraphOptionsTab_regexRemove);
		removeRegexButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			IWorkbench workbench = PlatformUI.getWorkbench();
			MessageDialog dialog = new MessageDialog(workbench.getActiveWorkbenchWindow().getShell(),
					Messages.SystemTapScriptGraphOptionsTab_removeRegexTitle, null,
					MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_removeRegexAsk,
							regularExpressionCombo.getItem(selectedRegex)),
					MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0); //$NON-NLS-1$ //$NON-NLS-2$
			int result = dialog.open();
			if (result == 0) { // Yes
				removeRegex(true);
			}
		}));

        GridLayout twoColumns = new GridLayout(2, false);

        Composite regexSummaryComposite = new Composite(parent, SWT.NONE);
        regexSummaryComposite.setLayout(twoColumns);
        regexSummaryComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label sampleOutputLabel = new Label(regexSummaryComposite, SWT.NONE);
        sampleOutputLabel.setText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
        sampleOutputLabel.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputTooltip);
        this.sampleOutputText = new Text(regexSummaryComposite, SWT.BORDER);
        this.sampleOutputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.sampleOutputText.addModifyListener(sampleOutputListener);
        sampleOutputText.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputTooltip);


        Composite expressionTableLabels = new Composite(parent, SWT.NONE);
        expressionTableLabels.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        expressionTableLabels.setLayout(twoColumns);

        Label label = new Label(expressionTableLabels, SWT.NONE);
        label.setText(Messages.SystemTapScriptGraphOptionsTab_columnTitle);
        label.setAlignment(SWT.LEFT);

        Label label2 = new Label(expressionTableLabels, SWT.NONE);
        label2.setAlignment(SWT.LEFT);
        label2.setText(Messages.SystemTapScriptGraphOptionsTab_extractedValueLabel);

        ScrolledComposite regexTextScrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.heightHint = 200;
        regexTextScrolledComposite.setLayoutData(data);

        textFieldsComposite = new Composite(regexTextScrolledComposite, SWT.NONE);
        textFieldsComposite.setLayout(new GridLayout(4, false));
        regexTextScrolledComposite.setContent(textFieldsComposite);
        regexTextScrolledComposite.setExpandHorizontal(true);

        // To position the column labels properly, add a dummy column and use its children's sizes for reference.
        // This is necessary since expressionTableLabels can't share a layout with textFieldsComposite.
        textListenersEnabled = false;
        addColumn(null);
        data = new GridData(SWT.FILL, SWT.FILL, false, false);
        data.horizontalIndent = textFieldsComposite.getChildren()[2].getLocation().x;
        data.widthHint = textFieldsComposite.getChildren()[2].getSize().x;
        label.setLayoutData(data);
        label2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        removeColumn(false);
        textListenersEnabled = true;
    }

    private IDataSet getCurrentDataset() {
        return DataSetFactory.createDataSet(RowDataSet.ID, columnNamesList.get(selectedRegex).toArray(new String[] {}));
    }

    private void createGraphCreateArea(Composite comp) {
        comp.setLayout(new GridLayout(2, false));

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
		graphsTable.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			selectedTableItem = (TableItem) e.item;
			setSelectionControlsEnabled(true);
		}));

        // Brings up a new dialog box when user clicks the add button. Allows
        // selecting a new graph to display.
		addGraphButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			SelectGraphAndSeriesWizard wizard = new SelectGraphAndSeriesWizard(getCurrentDataset(), null);
			IWorkbench workbench = PlatformUI.getWorkbench();
			wizard.init(workbench, null);
			WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
			dialog.create();
			dialog.open();

			GraphData gd = wizard.getGraphData();

			if (gd != null) {
				TableItem item = new TableItem(graphsTable, SWT.NONE);
				graphsData.add(gd);
				setUpGraphTableItem(item, gd, false);
				updateLaunchConfigurationDialog();
			}
		}));

        // Adds a new entry to the list of graphs that is a copy of the one selected.
		duplicateGraphButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			GraphData gd = ((GraphData) selectedTableItem.getData()).getCopy();

			TableItem item = new TableItem(graphsTable, SWT.NONE);
			graphsData.add(gd);
			if (badGraphs.contains(selectedTableItem.getData())) {
				badGraphs.add(gd);
				setUpGraphTableItem(item, gd, true);
			} else {
				setUpGraphTableItem(item, gd, false);
			}
			updateLaunchConfigurationDialog();
		}));

        // When button is clicked, brings up same wizard as the one for adding
        // a graph. Data in the wizard is filled out to match the properties
        // of the selected graph.
		editGraphButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			SelectGraphAndSeriesWizard wizard = new SelectGraphAndSeriesWizard(getCurrentDataset(),
					(GraphData) selectedTableItem.getData());
			IWorkbench workbench = PlatformUI.getWorkbench();
			wizard.init(workbench, null);
			WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
			dialog.create();
			dialog.open();

			GraphData gd = wizard.getGraphData();
			if (gd == null) {
				return;
			}
			GraphData old_gd = (GraphData) selectedTableItem.getData();
			if (!gd.isCopyOf(old_gd)) {
				badGraphs.remove(old_gd);
				setUpGraphTableItem(selectedTableItem, gd, false);
				graphsData.set(graphsTable.indexOf(selectedTableItem), gd);
				checkErrors(selectedRegex);
				updateLaunchConfigurationDialog();
			}
		}));

        // Removes the selected graph/filter from the table
		removeGraphButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			GraphData gd = (GraphData) selectedTableItem.getData();
			graphsData.remove(gd);
			badGraphs.remove(gd);
			selectedTableItem.dispose();
			setSelectionControlsEnabled(false);
			checkErrors(selectedRegex);
			updateLaunchConfigurationDialog();
		}));
    }

    private void removeRegex(boolean autoSelect) {
        int removedRegex = selectedRegex;
        if (autoSelect) {
            // The current selection is to be removed, so select something else that will be available.
            regularExpressionCombo.select(selectedRegex != 0 ? selectedRegex - 1 : 1);
            updateRegexSelection(regularExpressionCombo.getSelectionIndex(), false);
        }

        regularExpressionCombo.remove(removedRegex);
        outputList.remove(removedRegex);
        regexErrorMessages.remove(removedRegex);
        columnNamesList.remove(removedRegex);
        cachedNamesList.remove(removedRegex);
        graphsDataList.remove(removedRegex);

        if (autoSelect) {
            // Make sure the index of the selection is accurate.
            selectedRegex = regularExpressionCombo.getSelectionIndex();
        }

        // Re-add the "Add New Regex" entry if it is missing.
        if (getNumberOfRegexs() == MAX_NUMBER_OF_REGEXS - 1) {
            regularExpressionCombo.add(Messages.SystemTapScriptGraphOptionsTab_regexAddNew);
        }

        // Disable the "remove" button if only one selection is left; never want zero items.
        if (getNumberOfRegexs() == 1) {
            removeRegexButton.setEnabled(false);
        }
        updateLaunchConfigurationDialog();
    }

    /**
     * This handles UI & list updating whenever a different regular expression is selected.
     * @param newSelection The index of the regex to be selected.
     * @param force If true, the UI will update even if the index of the selected regex did not change.
     */
    private void updateRegexSelection(int newSelection, boolean force) {
        // Quit if the selection didn't change anything, or if the selection is invalid (-1).
        if (newSelection == -1 || (!force && selectedRegex == newSelection)) {
            return;
        }
        selectedRegex = newSelection;

        boolean textListenersDisabled = !textListenersEnabled;
        if (!textListenersDisabled) {
            textListenersEnabled = false;
        }

        sampleOutputText.setText(outputList.get(selectedRegex));
        cachedNames = cachedNamesList.get(selectedRegex);

        // Update the number of columns and their titles here, and not in refreshRegexRows,
        // using the list of saved active names instead of a cachedNames stack.
        List<String> columnNames = columnNamesList.get(selectedRegex);
        int desiredNumberOfColumns = columnNames.size();
        // Remove all columns to easily update them all immediately afterwards.
        while (numberOfVisibleColumns > 0) {
            removeColumn(false);
        }
        while (numberOfVisibleColumns < desiredNumberOfColumns) {
            addColumn(columnNames.get(numberOfVisibleColumns));
        }

        refreshRegexRows();

        // Now, only display graphs that are associated with the selected regex.
        graphsData = graphsDataList.get(selectedRegex);
        graphsTable.removeAll();
        selectedTableItem = null;
        setSelectionControlsEnabled(false);

        for (GraphData gd : graphsData) {
            TableItem item = new TableItem(graphsTable, SWT.NONE);
            setUpGraphTableItem(item, gd, badGraphs.contains(gd));
        }
        graphsGroup.setText(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphSetTitleBase,
                selectedRegex + 1));

        if (!textListenersDisabled) {
            textListenersEnabled = true;
        }
    }

    private void refreshRegexRows() {
        try {
            pattern = Pattern.compile(regularExpressionCombo.getText());
            matcher = pattern.matcher(sampleOutputText.getText());
            regexErrorMessages.set(selectedRegex, null);
        } catch (PatternSyntaxException e) {
            regexErrorMessages.set(selectedRegex, e.getMessage());
            return;
        }
        regexErrorMessages.set(selectedRegex, checkRegex(regularExpressionCombo.getText()));
        if (regexErrorMessages.get(selectedRegex) != null) {
            return;
        }

        int desiredNumberOfColumns = matcher.groupCount();

        while (numberOfVisibleColumns < desiredNumberOfColumns) {
            addColumn(null);
        }

        while (numberOfVisibleColumns > desiredNumberOfColumns) {
            removeColumn(true);
        }

        // Set values
        Control[] children = textFieldsComposite.getChildren();
        for (int i = 0; i < numberOfVisibleColumns; i++) {
            String sampleOutputResults;
            if (matcher.matches()) {
                sampleOutputResults = matcher.group(i+1);
            }
            else if (sampleOutputText.getText().length() == 0) {
                sampleOutputResults = Messages.SystemTapScriptGraphOptionsTab_sampleOutputIsEmpty;
            } else {
                sampleOutputResults = Messages.SystemTapScriptGraphOptionsTab_sampleOutputNoMatch;
            }
            ((Label)children[i*4+3]).setText(" " + sampleOutputResults); //$NON-NLS-1$
        }

        // May only add/edit graphs if there is output data being captured.
        addGraphButton.setEnabled(numberOfVisibleColumns > 0);
        if (selectedTableItem != null) {
            editGraphButton.setEnabled(numberOfVisibleColumns > 0);
        }

        regexErrorMessages.set(selectedRegex, findBadGraphs(selectedRegex));
    }

    /**
     * Checks if a provided regular expression is valid.
     * @param regex The regular expression to check for validity.
     * @return <code>null</code> if the regular expression is valid, or an error message.
     */
    private static String checkRegex(String regex) {
        //TODO may add more invalid regexs here, each with its own error message.
        if (regex.contains("()")) { //$NON-NLS-1$
            return Messages.SystemTapScriptGraphOptionsTab_emptyGroup;
        }
        return null;
    }

    /**
     * Adds one column to the list of the currently-selected regex's columns.
     * This creates an extra Text in which the name of the column may be entered,
     * and a corresponding Label containing sample expected output.
     * @param nameToAdd If non-null, the name of the newly-created column will
     * match this String. If null, the column will be given a name recovered from
     * the active stack of cached names, or a default name if one doesn't exist.
     */
    private void addColumn(String nameToAdd) {
        // Show the "shift" buttons of the previous column, if they exist.
        if (this.numberOfVisibleColumns > 0) {
            textFieldsComposite.getChildren()[(this.numberOfVisibleColumns - 1) * 4].setVisible(true);
            textFieldsComposite.getChildren()[(this.numberOfVisibleColumns - 1) * 4 + 1].setVisible(true);
        }

        // Add buttons for shifting column names up/down in the list.
        Button buttonUp = new Button(textFieldsComposite, SWT.PUSH);
        buttonUp.setText(Messages.SystemTapScriptGraphOptionsTab_columnShiftUp);
        buttonUp.setVisible(false);
        Button buttonDown = new Button(textFieldsComposite, SWT.PUSH);
        buttonDown.setText(Messages.SystemTapScriptGraphOptionsTab_columnShiftDown);
        buttonDown.setVisible(false);

        Text text = new Text(textFieldsComposite, SWT.BORDER);
        GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
        data.minimumWidth = 200;
        data.widthHint = 200;
        text.setLayoutData(data);

        numberOfVisibleColumns++;
        text.addModifyListener(columnNameListener);
        if (nameToAdd == null) {
            // Restore a deleted name by popping from the stack.
            if (cachedNames.size() > 0) {
                text.setText(cachedNames.pop());
            } else {
                text.setText(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase,
                        numberOfVisibleColumns));
            }
        } else {
            text.setText(nameToAdd);
        }

        Label label = new Label(textFieldsComposite, SWT.BORDER);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        textFieldsComposite.layout();
        textFieldsComposite.pack();

        // Add button listeners for shifting column names.
		buttonUp.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			Control clickedButton = (Control) e.widget;
			Control[] children = textFieldsComposite.getChildren();
			int currentColumn = 0;
			for (; currentColumn < numberOfVisibleColumns - 1; currentColumn++) {
				if (children[currentColumn * 4].equals(clickedButton)) {
					break;
				}
			}
			String edgeName = ((Text) children[currentColumn * 4 + 2]).getText();
			for (int i = currentColumn; i < numberOfVisibleColumns - 1; i++) {
				((Text) children[i * 4 + 2]).setText(((Text) children[(i + 1) * 4 + 2]).getText());
			}
			((Text) children[(numberOfVisibleColumns - 1) * 4 + 2]).setText(edgeName);
		}));

		buttonDown.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			Control clickedButton = (Control) e.widget;
			Control[] children = textFieldsComposite.getChildren();
			int currentColumn = 0;
			for (; currentColumn < numberOfVisibleColumns - 1; currentColumn++) {
				if (children[currentColumn * 4 + 1].equals(clickedButton)) {
					break;
				}
			}
			String edgeName = ((Text) children[(numberOfVisibleColumns - 1) * 4 + 2]).getText();
			for (int i = numberOfVisibleColumns - 1; i > currentColumn; i--) {
				((Text) children[i * 4 + 2]).setText(((Text) children[(i - 1) * 4 + 2]).getText());
			}
			((Text) children[currentColumn * 4 + 2]).setText(edgeName);
		}));
    }

    /**
     * Removes a column from the currently-selected regex, and removes its
     * corresponding Text & Label from the UI.
     * @param saveNames Set to <code>true</code> if the contents of removed
     * columns are to be saved in a stack for later use.
     */
    private void removeColumn(boolean saveNames) {
        Control[] children = textFieldsComposite.getChildren();
        int i = this.numberOfVisibleColumns*4 - 1;

        if (saveNames) {
            // Push the removed name on a stack.
            String name = ((Text)children[i-1]).getText();
            if (name != null && name != "") { //$NON-NLS-1$
                cachedNames.push(name);
            }
            columnNamesList.get(selectedRegex).remove(numberOfVisibleColumns - 1);
        }

        children[i].dispose();
        children[i-1].dispose();
        children[i-2].dispose();
        children[i-3].dispose();

        // Hide the previous column's "shift" buttons, if it exists.
        if (this.numberOfVisibleColumns > 2) {
            children[i - 6].setVisible(false);
            children[i - 7].setVisible(false);
        }

        this.numberOfVisibleColumns--;

        textFieldsComposite.layout();
        textFieldsComposite.pack();
    }

    /**
     * Marks all graphs belonging to the indicated regular expression that have an
     * error (missing column data, invalid graphID), or unmarks graphs that don't.
     * @param    regex The index of the regular expression to check for invalid graphs.
     * @return    An appropriate error message if an invalid graph is found, or if the
     * selected regular expression parses nothing.
     */
    private String findBadGraphs(int regex) {
        boolean foundBadID = false;
        boolean foundRemoved = false;
        int numberOfColumns = columnNamesList.get(regex).size();

        for (GraphData gd : graphsDataList.get(regex)) {
            boolean singleBadID = false;
            boolean singleRemoved = false;

            if (GraphFactory.getGraphName(gd.graphID) == null) {
                singleBadID = true;
            } else {
                if (gd.xSeries >= numberOfColumns) {
                    singleRemoved = true;
                }
                for (int s = 0; s < gd.ySeries.length && !singleRemoved; s++) {
                    if (gd.ySeries[s] >= numberOfColumns) {
                        singleRemoved = true;
                    }
                }
            }
            if (singleRemoved || singleBadID) {
                if (!badGraphs.contains(gd)) {
                    badGraphs.add(gd);
                    setUpGraphTableItem(findGraphTableItem(gd), null, true);
                }
            } else if (badGraphs.contains(gd)) {
                badGraphs.remove(gd);
                setUpGraphTableItem(findGraphTableItem(gd), null, false);
            }

            foundBadID |= singleBadID;
            foundRemoved |= singleRemoved;
        }

        if (numberOfColumns == 0) {
            return Messages.SystemTapScriptGraphOptionsTab_noGroups;
        }
        if (foundBadID) {
            return Messages.SystemTapScriptGraphOptionsTab_badGraphID;
        }
        if (foundRemoved) {
            return Messages.SystemTapScriptGraphOptionsTab_deletedGraphData;
        }
        return null;
    }

    private TableItem findGraphTableItem(GraphData gd) {
        for (TableItem item : graphsTable.getItems()) {
            if (item.getData().equals(gd)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Sets up a given {@link TableItem} with the proper title & appearance based on
     * its graph data & (in)valid status.
     * @param item The {@link TableItem} to set up.
     * @param gd The {@link GraphData} that the item will hold. Set to <code>null</code>
     * to preserve the item's existing data.
     * @param bad <code>true</code> if the item should appear as invalid, <code>false</code> otherwise.
     */
    private void setUpGraphTableItem(TableItem item, GraphData gd, boolean bad) {
        // Include a null check to avoid accidentally marking non-visible items.
        if (item == null) {
            return;
        }
        if (gd != null) {
            item.setData(gd);
        } else {
            gd = (GraphData) item.getData();
        }
        item.setForeground(item.getDisplay().getSystemColor(bad ? SWT.COLOR_RED : SWT.COLOR_BLACK));
        String graphName = GraphFactory.getGraphName(gd.graphID);
        if (graphName == null) {
            graphName = Messages.SystemTapScriptGraphOptionsTab_invalidGraphID;
        }
        item.setText(graphName + ":" + gd.title //$NON-NLS-1$
                + (bad ? " " + Messages.SystemTapScriptGraphOptionsTab_invalidGraph : "")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(RUN_WITH_CHART, false);
        configuration.setAttribute(NUMBER_OF_REGEXS, 1);
        configuration.setAttribute(NUMBER_OF_COLUMNS + 0, 0);
        configuration.setAttribute(NUMBER_OF_EXTRAS + 0, 0);
        configuration.setAttribute(REGULAR_EXPRESSION + 0, ""); //$NON-NLS-1$
        configuration.setAttribute(SAMPLE_OUTPUT + 0, ""); //$NON-NLS-1$
        configuration.setAttribute(NUMBER_OF_GRAPHS + 0, 0);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            textListenersEnabled = false;

            // Reset lists & settings to keep things idempotent.
            regularExpressionCombo.removeAll();
            outputList.clear();
            regexErrorMessages.clear();
            columnNamesList.clear();
            cachedNamesList.clear();
            graphsTable.removeAll();
            badGraphs.clear();

            // There should always be at least one regular expression (a blank one still counts).
            // If configuration's number of regexs is zero, it is outdated.
            int numberOfRegexs = Math.max(configuration.getAttribute(NUMBER_OF_REGEXS, 1), 1);

            // Only allow removing regexs if there are more than one.
            removeRegexButton.setEnabled(numberOfRegexs > 1);

            for (int r = 0; r < numberOfRegexs; r++) {
                // Save all of the configuration's regular expressions & sample outputs in a list.
                regularExpressionCombo.add(configuration.getAttribute(REGULAR_EXPRESSION + r, "")); //$NON-NLS-1$
                outputList.add(configuration.getAttribute(SAMPLE_OUTPUT + r, "")); //$NON-NLS-1$

                // Save each regex's list of group names.
                int numberOfColumns = configuration.getAttribute(NUMBER_OF_COLUMNS + r, 0);
                ArrayList<String> namelist = new ArrayList<>(numberOfColumns);
                for (int i = 0; i < numberOfColumns; i++) {
                    namelist.add(configuration.getAttribute(get2DConfigData(REGEX_BOX, r, i), (String)null));
                }
                columnNamesList.add(namelist);

                //Reclaim missing column data that was required for existing graphs at the time of the previous "apply".
                int numberOfExtras = configuration.getAttribute(NUMBER_OF_EXTRAS + r, 0);
                Stack<String> oldnames = new Stack<>();
                for (int i = 0; i < numberOfExtras; i++) {
                    oldnames.push(configuration.getAttribute(get2DConfigData(EXTRA_BOX, r, i), (String)null));
                }
                cachedNamesList.add(oldnames);

                regexErrorMessages.add(null);
            }
            if (getNumberOfRegexs() < MAX_NUMBER_OF_REGEXS) {
                regularExpressionCombo.add(Messages.SystemTapScriptGraphOptionsTab_regexAddNew);
            }

            // When possible, preserve the selection on subsequent initializations, for user convenience.
            int defaultSelectedRegex = 0 <= selectedRegex && selectedRegex < numberOfRegexs ? selectedRegex : 0;
            regularExpressionCombo.select(defaultSelectedRegex);

            // Add graphs
            graphsDataList = createGraphsFromConfiguration(configuration);
            graphsData = graphsDataList.get(defaultSelectedRegex);
            for (GraphData graphData : graphsData) {
                TableItem item = new TableItem(graphsTable, SWT.NONE);
                setUpGraphTableItem(item, graphData, true);
            }

            updateRegexSelection(defaultSelectedRegex, true); // Handles all remaining updates.
            checkAllOtherErrors();

            boolean chart = configuration.getAttribute(RUN_WITH_CHART, false);
            setGraphingEnabled(chart);
            this.runWithChartCheckButton.setSelection(chart);

        } catch (CoreException e) {
            ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_cantInitializeTab, e);
        } finally {
            textListenersEnabled = true;
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(RUN_WITH_CHART, this.runWithChartCheckButton.getSelection());

        int numberOfRegexs = getNumberOfRegexs();
        for (int r = 0; r < numberOfRegexs; r++) {
            // Save data sets, and clear removed ones.
            configuration.setAttribute(REGULAR_EXPRESSION + r, regularExpressionCombo.getItem(r));
            configuration.setAttribute(SAMPLE_OUTPUT + r, outputList.get(r));

            List<String> columnNames = columnNamesList.get(r);
            int numberOfColumns = columnNames.size();
            for (int i = 0; i < numberOfColumns; i++) {
                configuration.setAttribute(get2DConfigData(REGEX_BOX, r, i), columnNames.get(i));
            }
            cleanUpConfigurationItem(configuration, NUMBER_OF_COLUMNS, REGEX_BOX, r, numberOfColumns);
            configuration.setAttribute(NUMBER_OF_COLUMNS + r, numberOfColumns);

            // If the current regex has graphs with missing data, store all cached names
            // in the configuration so that they will be easily restorable for next time.
            Stack<String> extranames = cachedNamesList.get(r);
            int numberOfExtras = findBadGraphs(r) == null ? 0 : extranames.size();
            for (int i = 0; i < numberOfExtras; i++) {
                configuration.setAttribute(get2DConfigData(EXTRA_BOX, r, i), extranames.get(i));
            }
            cleanUpConfigurationItem(configuration, NUMBER_OF_EXTRAS, EXTRA_BOX, r, numberOfExtras);
            configuration.setAttribute(NUMBER_OF_EXTRAS + r, numberOfExtras);

            // Save new graphs, and clear removed ones.
            LinkedList<GraphData> list = graphsDataList.get(r);
            int numberOfGraphs = list.size();
            for (int i = 0; i < numberOfGraphs; i++) {
                GraphData graphData = list.get(i);
                configuration.setAttribute(get2DConfigData(GRAPH_TITLE, r, i), graphData.title);
                configuration.setAttribute(get2DConfigData(GRAPH_KEY, r, i), graphData.key);
                configuration.setAttribute(get2DConfigData(GRAPH_X_SERIES, r, i), graphData.xSeries);
                configuration.setAttribute(get2DConfigData(GRAPH_ID, r, i), graphData.graphID);

                int ySeriesLength = graphData.ySeries.length;
                for (int j = 0; j < ySeriesLength; j++) {
                    configuration.setAttribute(get2DConfigData(GRAPH_Y_SERIES, r, i + "_" + j), //$NON-NLS-1$
                            graphData.ySeries[j]);
                }
                cleanUpConfigurationGraphYSeries(configuration, r, i, ySeriesLength);
                configuration.setAttribute(get2DConfigData(GRAPH_Y_SERIES_LENGTH, r, i), ySeriesLength);
            }
            cleanUpConfigurationGraphs(configuration, r, numberOfGraphs);
            configuration.setAttribute(NUMBER_OF_GRAPHS + r, numberOfGraphs);
        }
        cleanUpConfiguration(configuration, numberOfRegexs);
        configuration.setAttribute(NUMBER_OF_REGEXS, numberOfRegexs);
    }

    /**
     * Removes all configuration attributes associated with deleted regular expressions.
     * @param configuration The configuration to remove attributes from.
     * @param numberOfRegexs The number of regex-related properties to exist in the
     * configuration after cleanup.
     */
    private void cleanUpConfiguration(ILaunchConfigurationWorkingCopy configuration, int numberOfRegexs) {
        int oldNumberOfRegexs = 0;
        try {
            oldNumberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);
        } catch (CoreException e) {}
        for (int r = numberOfRegexs; r < oldNumberOfRegexs; r++) {
            configuration.removeAttribute(REGULAR_EXPRESSION + r);
            configuration.removeAttribute(SAMPLE_OUTPUT + r);

            cleanUpConfigurationItem(configuration, NUMBER_OF_COLUMNS, REGEX_BOX, r, 0);
            configuration.removeAttribute(NUMBER_OF_COLUMNS + r);

            cleanUpConfigurationItem(configuration, NUMBER_OF_COLUMNS, EXTRA_BOX, r, 0);
            configuration.removeAttribute(NUMBER_OF_EXTRAS + r);

            cleanUpConfigurationGraphs(configuration, r, 0);
            configuration.removeAttribute(NUMBER_OF_GRAPHS + r);
        }
    }

    private void cleanUpConfigurationGraphs(ILaunchConfigurationWorkingCopy configuration, int regex, int newNumberOfGraphs) {
        int oldNumberOfGraphs = 0;
        try {
            oldNumberOfGraphs = configuration.getAttribute(NUMBER_OF_GRAPHS + regex, 0);
        } catch (CoreException e) {}
        for (int i = newNumberOfGraphs; i < oldNumberOfGraphs; i++) {
            configuration.removeAttribute(get2DConfigData(GRAPH_TITLE, regex, i));
            configuration.removeAttribute(get2DConfigData(GRAPH_KEY, regex, i));
            configuration.removeAttribute(get2DConfigData(GRAPH_X_SERIES, regex, i));
            configuration.removeAttribute(get2DConfigData(GRAPH_ID, regex, i));

            cleanUpConfigurationGraphYSeries(configuration, regex, i, 0);
            configuration.removeAttribute(get2DConfigData(GRAPH_Y_SERIES_LENGTH, regex, i));
        }
    }

    private void cleanUpConfigurationItem(ILaunchConfigurationWorkingCopy configuration, String counter, String property, int regex, int newNumberOfItems) {
        int oldNumberOfItems = 0;
        try {
            oldNumberOfItems = configuration.getAttribute(counter + regex, 0);
        } catch (CoreException e) {}
        for (int i = newNumberOfItems; i < oldNumberOfItems; i++) {
            configuration.removeAttribute(get2DConfigData(property, regex, i));
        }
    }

    private void cleanUpConfigurationGraphYSeries(ILaunchConfigurationWorkingCopy configuration, int regex, int graph, int newLength) {
        int oldYSeriesLength = 0;
        try {
            oldYSeriesLength = configuration.getAttribute(get2DConfigData(GRAPH_Y_SERIES_LENGTH, regex, graph), 0);
        } catch (CoreException e) {}
        for (int i = newLength; i < oldYSeriesLength; i++) {
            configuration.removeAttribute(get2DConfigData(GRAPH_Y_SERIES, regex, graph + "_" + i)); //$NON-NLS-1$
        }
    }

    /**
     * Checks all regular expressions for errors, except for the currently-selected
     * expression (as it should be checked by {@link #refreshRegexRows}).
     */
    private void checkAllOtherErrors() {
        for (int i = 0, n = getNumberOfRegexs(); i < n; i++) {
            if (i == selectedRegex) {
                continue;
            }
            checkErrors(i);
        }
    }

    /**
     * Checks the regular expression of the provided index for errors.
     * Sets the associated error message to contain relevant error information.
     * @param i The index of the regular expression to check for errors.
     */
    private void checkErrors(int i) {
        String regex = regularExpressionCombo.getItem(i);
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            regexErrorMessages.set(i, e.getMessage());
            return;
        }

        String error = findBadGraphs(i);
        if (error == null) {
            error = checkRegex(regex);
        }

        regexErrorMessages.set(i, error);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        setErrorMessage(null);

        // If graphic is disabled then everything is valid.
        if (!this.graphingEnabled) {
            return true;
        }

        for (int r = 0, n = getNumberOfRegexs(); r < n; r++) {
            String regexErrorMessage = regexErrorMessages.get(r);
            if (regexErrorMessage != null) {
                setErrorMessage(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_regexErrorMsgFormat,
                        regularExpressionCombo.getItems()[r], regexErrorMessage));
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a launch configuration's Systemtap Graphing settings are valid.
     * @param launchConfig The launch configuration to check for graph validity.
     * @return <code>true</code> if the launch settings are valid, or <code>false</code> if
     * its graph settings are invalid in some way.
     * @since 2.2
     */
    public static boolean isValidLaunch(ILaunchConfiguration launchConfig) throws CoreException {
        // If graphic is disabled then everything is valid.
        if (!launchConfig.getAttribute(RUN_WITH_CHART, false)) {
            return true;
        }

        for (int r = 0, n = launchConfig.getAttribute(NUMBER_OF_REGEXS, 1); r < n; r++) {
            // Check for any invalid regexs.
            String regex = launchConfig.getAttribute(REGULAR_EXPRESSION + r, (String) null);
            if (regex == null || checkRegex(regex) != null) {
                return false;
            }
            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                return false;
            }

            // If graphs are plotted but no data is captured by one of them, report this as a problem.
            int numberOfColumns = launchConfig.getAttribute(NUMBER_OF_COLUMNS + r, 0);
            if (numberOfColumns == 0) {
                return false;
            }

            // Check for graphs that are missing required data.
            for (int i = 0, g = launchConfig.getAttribute(NUMBER_OF_GRAPHS + r, 0); i < g; i++) {
                if (GraphFactory.getGraphName(launchConfig.getAttribute(get2DConfigData(GRAPH_ID, r, i), (String) null)) == null) {
                    return false;
                }
                if (launchConfig.getAttribute(get2DConfigData(GRAPH_X_SERIES, r, i), 0) >= numberOfColumns) {
                    return false;
                }
                for (int j = 0, y = launchConfig.getAttribute(get2DConfigData(GRAPH_Y_SERIES_LENGTH, r, i), 0); j < y; j++) {
                    if (launchConfig.getAttribute(get2DConfigData(GRAPH_Y_SERIES, r, i + "_" + j), 0) >= numberOfColumns) { //$NON-NLS-1$
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return Messages.SystemTapScriptGraphOptionsTab_graphingTitle;
    }

    @Override
    public Image getImage() {
		return ResourceLocator.imageDescriptorFromBundle(IDEPlugin.PLUGIN_ID, "icons/graphing_tab.gif").get() //$NON-NLS-1$
				.createImage();
    }

    private void setGraphingEnabled(boolean enabled) {
        this.graphingEnabled = enabled;
        this.setControlEnabled(outputParsingGroup, enabled);
        this.setControlEnabled(graphsGroup, enabled);
        // Disable buttons that rely on a selected graph if no graph is selected.
        this.setSelectionControlsEnabled(selectedTableItem != null);
        this.addGraphButton.setEnabled(enabled && numberOfVisibleColumns > 0);
        this.removeRegexButton.setEnabled(enabled && getNumberOfRegexs() > 1);
        updateLaunchConfigurationDialog();
    }

    private void setControlEnabled(Composite composite, boolean enabled) {
        composite.setEnabled(enabled);
        for (Control child : composite.getChildren()) {
            child.setEnabled(enabled);
            if (child instanceof Composite) {
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
        editGraphButton.setEnabled(enabled && numberOfVisibleColumns > 0);
        removeGraphButton.setEnabled(enabled);
    }
}
