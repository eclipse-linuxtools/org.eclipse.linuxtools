/*******************************************************************************
 * Copyright (c) 2012 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowParser;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SystemTapScriptGraphOptionsTab extends
		AbstractLaunchConfigurationTab {

	static final String RUN_WITH_CHART = "runWithChart"; //$NON-NLS-1$
	static final String NUMBER_OF_COLUMNS = "numberOfColumns"; //$NON-NLS-1$
	static final String REGEX_BOX = "regexBox_"; //$NON-NLS-1$

	private ModifyListener regExListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
			refreshRegEx();
		}
	};

	private static final int COLUMNS = 3;
	private static final int MAX_SERIES = 24;

	private Label lblRegEx;
	private Composite textFieldsComposite;

	private Spinner numberOfColumnsSpinner;

	private ScrolledComposite regexTextScrolledComposite;
	private Group outputParsingGroup;
	private Button runWithChartCheckButton;

	public static IDataSetParser createDatasetParser(ILaunchConfiguration configuration) {
		int n;
		try {
			n = configuration.getAttribute(NUMBER_OF_COLUMNS, 0);
			ArrayList<String> regEx = new ArrayList<String>(n * (COLUMNS - 1));

			for (int i = 0; i < (n * COLUMNS); i++) {
				if (i % COLUMNS != 0) {
					String text = configuration.getAttribute(REGEX_BOX + i,
							(String) null);
					regEx.add(text);
				}
			}

			return new RowParser(regEx.toArray(new String[] {}));
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

			for (int i = 0; i < (n * COLUMNS); i++) {
				if (i % COLUMNS == 0) {
					String text = configuration.getAttribute(REGEX_BOX + i,
							(String) null);
					labels.add(text);
				}
			}

			return DataSetFactory.createDataSet(RowDataSet.ID, labels.toArray(new String[] {}));
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_1, e);
		}
		return null;
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

		setGraphingEnabled(false);
		runWithChartCheckButton.setSelection(false);
	}

	protected void createColumnSelector(Composite parent) {

		GridLayout layout = new GridLayout();
		parent.setLayout(layout);

		Composite numberOfColumnsComposite = new Composite(parent, SWT.NONE);
		GridLayout twoColumns = new GridLayout();
		twoColumns.numColumns = 2;
		numberOfColumnsComposite.setLayout(twoColumns);

		Label lblSeries = new Label(numberOfColumnsComposite, SWT.NONE);
		lblSeries.setText(Messages.ParsingWizardPage_NumberOfColumns);

		this.numberOfColumnsSpinner = new Spinner (numberOfColumnsComposite, SWT.BORDER);
		numberOfColumnsSpinner.setMinimum(1);
		numberOfColumnsSpinner.setMaximum(MAX_SERIES);
		numberOfColumnsSpinner.setSelection(3);
		numberOfColumnsSpinner.setIncrement(1);
		numberOfColumnsSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				refreshRegexRows();
				refreshRegEx();
			}
		});

		GridLayout threeColumnLayout = new GridLayout();
		threeColumnLayout.numColumns = 3;
		threeColumnLayout.makeColumnsEqualWidth = true;
		Composite expressionTableLabels = new Composite(parent, SWT.NONE);
		expressionTableLabels.setLayout(threeColumnLayout);
		expressionTableLabels.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label label = new Label(expressionTableLabels, SWT.NONE);
		label.setText(Messages.ParsingWizardPage_Title);
		label.setAlignment(SWT.CENTER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		label = new Label(expressionTableLabels, SWT.NONE);
		label.setText(Messages.ParsingWizardPage_RegularExpression);
		label.setAlignment(SWT.CENTER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		label = new Label(expressionTableLabels, SWT.NONE);
		label.setText(Messages.ParsingWizardPage_Delimiter);
		label.setAlignment(SWT.CENTER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.regexTextScrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		regexTextScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		textFieldsComposite = new Composite(regexTextScrolledComposite, SWT.NONE);
		textFieldsComposite.setLayout(threeColumnLayout);
		textFieldsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		regexTextScrolledComposite.setContent(textFieldsComposite);
		regexTextScrolledComposite.setExpandHorizontal(true);
		regexTextScrolledComposite.setExpandVertical(true);

		Composite regexSummaryComposite = new Composite(parent, SWT.NONE);
		regexSummaryComposite.setLayout(twoColumns);

		Label lblRegExTitle = new Label(regexSummaryComposite, SWT.NONE);
		lblRegExTitle.setText(Messages.ParsingWizardPage_RegularExpression + ":"); //$NON-NLS-1$
		lblRegEx = new Label(regexSummaryComposite, SWT.NONE);
		lblRegEx.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		refreshRegexRows();
	}

	private void refreshRegexRows() {
		int numberOfColumns = numberOfColumnsSpinner.getSelection();
		int currentNumberOfColumns = textFieldsComposite.getChildren().length/3;

		while (currentNumberOfColumns < numberOfColumns){
			addColumn();
			currentNumberOfColumns++;
		}

		while (currentNumberOfColumns > numberOfColumns){
			removeColumn();
			currentNumberOfColumns--;
		}

		refreshRegEx();
	}

	private void addColumn(){
		Text text = new Text(textFieldsComposite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.addModifyListener(regExListener);

		text = new Text(textFieldsComposite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.setText("\\d+"); //$NON-NLS-1$
		text.addModifyListener(regExListener);

		text = new Text(textFieldsComposite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.setText("\\D+"); //$NON-NLS-1$
		text.addModifyListener(regExListener);

		regexTextScrolledComposite.setMinSize(textFieldsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		textFieldsComposite.layout();
	}

	private void removeColumn(){
		Control[] children = textFieldsComposite.getChildren();
		int i = children.length - 1;
		children[i--].dispose();
		children[i--].dispose();
		children[i--].dispose();

		regexTextScrolledComposite.setMinSize(textFieldsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		textFieldsComposite.layout();
	}

	private void refreshRegEx() {
		int series = numberOfColumnsSpinner.getSelection();
		series *= COLUMNS;
		StringBuilder s = new StringBuilder();
		for(int i=0; i<series; i++) {
			if(0 != i%COLUMNS) {
				s.append(((Text)textFieldsComposite.getChildren()[i]).getText());
			}
		}
		lblRegEx.setText(s.toString());
		lblRegEx.getParent().pack();
	}

	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RUN_WITH_CHART, false);
		configuration.setAttribute(NUMBER_OF_COLUMNS, 3);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			boolean chart = configuration.getAttribute(RUN_WITH_CHART, false);
			setGraphingEnabled(chart);
			this.runWithChartCheckButton.setSelection(chart);

			int n = configuration.getAttribute(NUMBER_OF_COLUMNS, 0);
			numberOfColumnsSpinner.setSelection(n);
			Control[] textBoxes = this.textFieldsComposite.getChildren();
			for (int i = 0; i < textBoxes.length; i++) {
				String text = configuration.getAttribute(REGEX_BOX+i, (String)null);
				if (text != null) {
					((Text)textBoxes[i]).setText(text);
				}
			}
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_5, e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RUN_WITH_CHART, this.runWithChartCheckButton.getSelection());

		int n = numberOfColumnsSpinner.getSelection();
		configuration.setAttribute(NUMBER_OF_COLUMNS, n);
		Control[] textBoxes = this.textFieldsComposite.getChildren();
		for (int i = 0; i < textBoxes.length; i++) {
			String text = ((Text)textBoxes[i]).getText();
			configuration.setAttribute(REGEX_BOX+i, text);
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		// If graphic is disabled then everything is valid.
		if (this.runWithChartCheckButton.getSelection() == false){
			return true;
		}

		// Check that all regex boxes are filled in
		int series = numberOfColumnsSpinner.getSelection();

		for(int i=0; i<(series*COLUMNS); i++) {
			if(((Text)textFieldsComposite.getChildren()[i]).getText().isEmpty()) {
				setErrorMessage(Messages.SystemTapScriptGraphOptionsTab_6);
				return false;
			}
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
		this.setControlEnabled(outputParsingGroup, enabled);
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
}
