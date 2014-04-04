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

import static org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants.KEY;
import static org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS;
import static org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants.STAP_CMD_OPTION;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SystemTapScriptOptionsTab extends AbstractLaunchConfigurationTab {

	static final String MISC_COMMANDLINE_OPTIONS = "MiscComandLineOptions"; //$NON-NLS-1$

	private Button checkBox[] = new Button[IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length];
	private Text text[] = new Text[IDEPreferenceConstants.STAP_STRING_OPTIONS.length];
	private Text targetProgramText;

	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
	};
	private FileDialog fileDialog;
	private Text miscCommandsText;

	private Button dyninstCheckBox;

	private Text targetPidText;

	@Override
	public void createControl(Composite parent) {

		GridLayout singleColumnGridLayout = new GridLayout();
		singleColumnGridLayout.numColumns = 1;
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(singleColumnGridLayout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		this.fileDialog = new FileDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), SWT.OPEN);
		fileDialog.setText(Messages.SystemTapScriptOptionsTab_selectExec);
		fileDialog.setFilterPath(Platform.getLocation().toOSString());
		// Target Executable path
		Group targetExecutableGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		targetExecutableGroup.setText(Messages.SystemTapScriptOptionsTab_targetExec);
		targetExecutableGroup
				.setToolTipText(Messages.SystemTapScriptOptionsTab_targetToolTip);

		targetExecutableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false));
		GridLayout twoColumnGridLayout = new GridLayout();
		twoColumnGridLayout.numColumns = 2;
		targetExecutableGroup.setLayout(twoColumnGridLayout);
		this.targetProgramText = new Text(targetExecutableGroup, SWT.SINGLE
				| SWT.BORDER);
		targetProgramText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		targetProgramText.addModifyListener(modifyListener);
		Button selectTargetProgramButton = new Button(targetExecutableGroup, 0);
		GridData gridData = new GridData();

		selectTargetProgramButton.setLayoutData(gridData);
		selectTargetProgramButton
				.setText(Messages.SystemTapScriptLaunchConfigurationTab_browse);
		selectTargetProgramButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String fileName = fileDialog.open();
				if (fileName != null) {
					targetProgramText.setText(fileName);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Check boxes
		Composite cmpChkBoxes = new Composite(comp, SWT.NONE);
		cmpChkBoxes.setLayout(twoColumnGridLayout);
		cmpChkBoxes.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		for (int i = 0; i < IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
			checkBox[i] = new Button(cmpChkBoxes, SWT.CHECK);
			checkBox[i]
					.setText(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.LABEL]
							+ " (" + IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.FLAG] + ")"); //$NON-NLS-1$//$NON-NLS-2$
			checkBox[i].addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateLaunchConfigurationDialog();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					updateLaunchConfigurationDialog();
				}
			});
			checkBox[i]
					.setToolTipText(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.TOOLTIP]);

			if (IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.FLAG]
					.contains(Messages.SystemTapScriptOptionsTab_dyninst)) {
				this.dyninstCheckBox = checkBox[i];
			}
		}

		// Labels and Text fields
		Composite cmpTxtBoxes = new Composite(comp, SWT.NONE);
		cmpTxtBoxes.setLayout(twoColumnGridLayout);
		cmpTxtBoxes.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Label label;
		for (int i = 0; i < IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
			label = new Label(cmpTxtBoxes, SWT.NONE);
			label.setText(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.LABEL]
					+ " (" + IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.FLAG] + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			label.setToolTipText(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.TOOLTIP]);
			text[i] = new Text(cmpTxtBoxes, SWT.BORDER);
			text[i].setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
			text[i].addModifyListener(modifyListener);
			text[i].setToolTipText(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.TOOLTIP]);

			if (IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.FLAG]
					.contains("-x")) { //$NON-NLS-1$
				this.targetPidText = text[i];
			}
		}

		label = new Label(cmpTxtBoxes, SWT.NONE);
		label.setText(Messages.SystemTapScriptOptionsTab_otherOptions);
		miscCommandsText = new Text(cmpTxtBoxes, SWT.BORDER);
		miscCommandsText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, true));
		miscCommandsText.addModifyListener(modifyListener);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();

		configuration.setAttribute(STAP_CMD_OPTION[KEY],
				store.getString(STAP_CMD_OPTION[IDEPreferenceConstants.KEY]));

		for (int i = 0; i < IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
			configuration
					.setAttribute(
							IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY],
							store.getBoolean(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY]));
		}

		for (int i = 0; i < IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
			configuration
					.setAttribute(
							IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY],
							store.getString(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY]));
		}

		configuration.setAttribute(MISC_COMMANDLINE_OPTIONS,
				store.getString(MISC_COMMANDLINE_OPTIONS));
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			targetProgramText.setText(configuration.getAttribute(
					STAP_CMD_OPTION[KEY], "")); //$NON-NLS-1$

			for (int i = 0; i < IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
				checkBox[i]
						.setSelection(configuration
								.getAttribute(
										IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY],
										false));
			}

			for (int i = 0; i < IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
				text[i].setText(configuration
						.getAttribute(
								IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY],
								"")); //$NON-NLS-1$
			}

			miscCommandsText.setText(configuration.getAttribute(
					MISC_COMMANDLINE_OPTIONS, "")); //$NON-NLS-1$

		} catch (Exception e) {
			ExceptionErrorDialog
					.openError(
							Messages.SystemTapScriptOptionsTab_initializeConfigurationFailed,
							e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		configuration.setAttribute(STAP_CMD_OPTION[KEY],
				targetProgramText.getText());

		for (int i = 0; i < STAP_BOOLEAN_OPTIONS.length; i++) {
			configuration.setAttribute(STAP_BOOLEAN_OPTIONS[i][KEY],
					checkBox[i].getSelection());
		}

		for (int i = 0; i < IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
			configuration.setAttribute(
					IDEPreferenceConstants.STAP_STRING_OPTIONS[i][KEY],
					text[i].getText());
		}

		configuration.setAttribute(MISC_COMMANDLINE_OPTIONS,
				miscCommandsText.getText());
	}

	@Override
	public String getName() {
		return Messages.SystemTapScriptLaunchConfigurationTab_tabName;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		// If dyninst is being used a pid or a target executable must be
		// specified.
		if (this.dyninstCheckBox.getSelection()
				&& this.targetProgramText.getText().isEmpty()
				&& this.targetPidText.getText().isEmpty()) {
			setErrorMessage(Messages.SystemTapScriptOptionsTab_dyninstError);
			return false;
		}

		if (!this.targetPidText.getText().isEmpty() && !this.targetPidText.getText().matches("[0-9]*")) { //$NON-NLS-1$
			setErrorMessage(Messages.SystemTapScriptOptionsTab_pidError);
			return false;
		}

		return true;
	}

	@Override
	public Image getImage() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(IDEPlugin.PLUGIN_ID,
				"icons/smileytap_small.gif").createImage(); //$NON-NLS-1$
	}

}
