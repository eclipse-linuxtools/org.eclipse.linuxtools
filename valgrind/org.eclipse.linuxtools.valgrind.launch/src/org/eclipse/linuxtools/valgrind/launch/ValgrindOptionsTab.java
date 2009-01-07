/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.launch;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.valgrind.core.utils.LaunchConfigurationConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class ValgrindOptionsTab extends AbstractLaunchConfigurationTab {
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	// General controls
	protected Button traceChildrenButton;
	protected Button childSilentButton;
	//	protected Button trackFdsButton;
	//	protected Button timeStampButton;
	protected Button runFreeresButton;

	protected Button demangleButton;
	protected Spinner numCallersSpinner;
	protected Button errorLimitButton;
	protected Button showBelowMainButton;
	protected Spinner maxStackFrameSpinner;
	protected Text suppFileText;

	protected String tool;
	protected String[] tools;
	protected Combo toolsCombo;

	protected ILaunchConfigurationWorkingCopy launchConfigurationWorkingCopy;
	protected ILaunchConfiguration launchConfiguration;

	protected ILaunchConfigurationTab dynamicTab;
	protected Composite dynamicTabHolder;

	protected boolean isInitializing = false;
	protected boolean initDefaults = false;

	protected SelectionListener selectListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}
	};
	protected ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();	
		}			
	};

	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout());

		createVerticalSpacer(top, 1);

		createToolCombo(top);

		createVerticalSpacer(top, 1);

		TabFolder optionsFolder = new TabFolder(top, SWT.BORDER);
		optionsFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		// "general" tab
		TabItem generalTab = new TabItem(optionsFolder, SWT.NONE);
		generalTab.setText(Messages.getString("ValgrindOptionsTab.General")); //$NON-NLS-1$

		Composite generalTop = new Composite(optionsFolder, SWT.NONE);
		generalTop.setLayout(new GridLayout());
		generalTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		createBasicOptions(generalTop);

		createVerticalSpacer(generalTop, 1);

		createErrorOptions(generalTop);

		generalTab.setControl(generalTop);

		// "tool" tab
		TabItem toolTab = new TabItem(optionsFolder, SWT.NONE);
		toolTab.setText(Messages.getString("ValgrindOptionsTab.Tool")); //$NON-NLS-1$

		dynamicTabHolder = new Composite(optionsFolder, SWT.NONE);
		dynamicTabHolder.setLayout(new GridLayout());
		dynamicTabHolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		toolTab.setControl(dynamicTabHolder);
	}

	private void createToolCombo(Composite top) {
		Composite comboTop = new Composite(top, SWT.NONE);
		comboTop.setLayout(new GridLayout(2, false));
		Label toolLabel = new Label(comboTop, SWT.NONE);
		toolLabel.setText(Messages.getString("ValgrindOptionsTab.Tool_to_run")); //$NON-NLS-1$
		toolsCombo = new Combo(comboTop, SWT.READ_ONLY);
		tools = ValgrindLaunchPlugin.getDefault().getRegisteredToolIDs();

		String[] names = new String[tools.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = ValgrindLaunchPlugin.getDefault().getToolName(tools[i]);
		}
		toolsCombo.setItems(names);

		toolsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				// user selected change, set defaults in new tool
				if (!isInitializing) {
					initDefaults = true;
					int ix = toolsCombo.getSelectionIndex();
					tool = tools[ix];
					handleToolChanged();
					updateLaunchConfigurationDialog();
				}
			}
		});
	}

	protected void createBasicOptions(Composite top) {
		Group basicGroup = new Group(top, SWT.NONE);
		basicGroup.setLayout(new GridLayout());
		basicGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		basicGroup.setText(Messages.getString("ValgrindOptionsTab.Basic_Options")); //$NON-NLS-1$

		Composite basicTop = new Composite(basicGroup, SWT.NONE);
		basicTop.setLayout(new GridLayout(8, false));
		basicTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label traceChildrenLabel = new Label(basicTop, SWT.NONE);
		traceChildrenLabel.setText(Messages.getString("ValgrindOptionsTab.trace_children")); //$NON-NLS-1$
		traceChildrenButton = new Button(basicTop, SWT.CHECK);
		traceChildrenButton.addSelectionListener(selectListener);

		createHorizontalSpacer(basicTop, 1);

		// Must be on to prevent mangled XML output
		Label childSilentLabel = new Label(basicTop, SWT.NONE);
		childSilentLabel.setText(Messages.getString("ValgrindOptionsTab.child_silent")); //$NON-NLS-1$
		childSilentButton = new Button(basicTop, SWT.CHECK);
		childSilentButton.setSelection(true);
		childSilentButton.setEnabled(false);
		//childSilentButton.addSelectionListener(selectListener);

		createHorizontalSpacer(basicTop, 1);

		//		Label trackFdsLabel = new Label(basicTop, SWT.NONE);
		//		trackFdsLabel.setText("track open fds:");
		//		trackFdsButton = new Button(basicTop, SWT.CHECK);
		//		trackFdsButton.addSelectionListener(selectListener);

		//		createVerticalSpacer(basicTop, 1);

		//		Label timeStampLabel = new Label(basicTop, SWT.NONE);
		//		timeStampLabel.setText("time stamp messages:");
		//		timeStampButton = new Button(basicTop, SWT.CHECK);
		//		timeStampButton.addSelectionListener(selectListener);

		//		createHorizontalSpacer(basicTop, 1);

		Label runFreeresLabel = new Label(basicTop, SWT.NONE);
		runFreeresLabel.setText(Messages.getString("ValgrindOptionsTab.run_freeres")); //$NON-NLS-1$
		runFreeresButton = new Button(basicTop, SWT.CHECK);
		runFreeresButton.addSelectionListener(selectListener);
	}

	protected void createErrorOptions(Composite top) {
		Group errorGroup = new Group(top, SWT.NONE);
		errorGroup.setLayout(new GridLayout());
		errorGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		errorGroup.setText(Messages.getString("ValgrindOptionsTab.Error_Options")); //$NON-NLS-1$

		Composite errorTop = new Composite(errorGroup, SWT.NONE);
		errorTop.setLayout(new GridLayout(8, false));
		errorTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label demangleLabel = new Label(errorTop, SWT.NONE);
		demangleLabel.setText(Messages.getString("ValgrindOptionsTab.demangle")); //$NON-NLS-1$
		demangleButton = new Button(errorTop, SWT.CHECK);
		demangleButton.addSelectionListener(selectListener);

		createHorizontalSpacer(errorTop, 1);

		Label numCallersLabel = new Label(errorTop, SWT.NONE);
		numCallersLabel.setText(Messages.getString("ValgrindOptionsTab.num_callers")); //$NON-NLS-1$
		numCallersSpinner = new Spinner(errorTop, SWT.BORDER);
		numCallersSpinner.setMaximum(50);
		numCallersSpinner.addModifyListener(modifyListener);

		createHorizontalSpacer(errorTop, 1);

		Label errorLimitLabel = new Label(errorTop, SWT.NONE);
		errorLimitLabel.setText(Messages.getString("ValgrindOptionsTab.limit_errors")); //$NON-NLS-1$
		errorLimitButton = new Button(errorTop, SWT.CHECK);
		errorLimitButton.addSelectionListener(selectListener);

		createVerticalSpacer(errorTop, 1);

		Label showBelowMainLabel = new Label(errorTop, SWT.NONE);
		showBelowMainLabel.setText(Messages.getString("ValgrindOptionsTab.show_errors_below_main")); //$NON-NLS-1$
		showBelowMainButton = new Button(errorTop, SWT.CHECK);
		showBelowMainButton.addSelectionListener(selectListener);

		createHorizontalSpacer(errorTop, 1);

		Label maxStackFrameLabel = new Label(errorTop, SWT.NONE);
		maxStackFrameLabel.setText(Messages.getString("ValgrindOptionsTab.max_size_of_stack_frame")); //$NON-NLS-1$
		maxStackFrameSpinner = new Spinner(errorTop, SWT.BORDER);
		maxStackFrameSpinner.setMaximum(Integer.MAX_VALUE);
		maxStackFrameSpinner.addModifyListener(modifyListener);

		createVerticalSpacer(errorTop, 1);

		createSuppressionsOption(errorTop);		
	}

	protected void createSuppressionsOption(Composite top) {
		Label suppFileLabel = new Label(top, SWT.NONE);
		suppFileLabel.setText(Messages.getString("ValgrindOptionsTab.suppressions_file")); //$NON-NLS-1$

		Composite browseTop = new Composite(top, SWT.NONE);
		GridLayout browseLayout = new GridLayout(3, false);
		browseLayout.marginHeight = 0;
		browseLayout.marginWidth = 0;
		browseTop.setLayout(browseLayout);
		GridData browseData = new GridData(GridData.FILL_HORIZONTAL);
		browseData.horizontalSpan = 7;
		browseTop.setLayoutData(browseData);

		suppFileText = new Text(browseTop, SWT.BORDER);
		suppFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		suppFileText.addModifyListener(modifyListener);

		Button workspaceBrowseButton = createPushButton(browseTop, Messages.getString("ValgrindOptionsTab.Workspace"), null);  //$NON-NLS-1$
		workspaceBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
				dialog.setTitle(Messages.getString("ValgrindOptionsTab.Select_a_Resource"));  //$NON-NLS-1$
				dialog.setMessage(Messages.getString("ValgrindOptionsTab.Select_a_Suppressions_File"));  //$NON-NLS-1$
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot()); 
				dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
				if (dialog.open() == IDialogConstants.OK_ID) {
					IResource resource = (IResource) dialog.getFirstResult();
					String arg = resource.getFullPath().toString();
					String fileLoc = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
					suppFileText.setText(fileLoc);
				}
			}
		});
		Button fileBrowseButton = createPushButton(browseTop, Messages.getString("ValgrindOptionsTab.File_System"), null); //$NON-NLS-1$
		fileBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String filePath = suppFileText.getText();
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				filePath = dialog.open();
				if (filePath != null) {
					suppFileText.setText(filePath);
				}
			}
		});
	}

	protected void handleToolChanged() {
		try {
			// create dynamicTab
			loadDynamicArea();

			if (launchConfigurationWorkingCopy == null) {
				if (launchConfiguration.isWorkingCopy()) {
					launchConfigurationWorkingCopy = (ILaunchConfigurationWorkingCopy) launchConfiguration;
				} else {
					launchConfigurationWorkingCopy = launchConfiguration.getWorkingCopy();
				}
			}

			// setDefaults called on this tab so call on dynamicTab OR
			// user changed tool, not just restoring state
			if (initDefaults) {
				dynamicTab.setDefaults(launchConfigurationWorkingCopy);
			}						
			initDefaults = false;
			dynamicTab.initializeFrom(launchConfigurationWorkingCopy);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void loadDynamicArea() throws CoreException {
		for (Control child : dynamicTabHolder.getChildren()) {
			child.dispose();
		}

		dynamicTab = ValgrindLaunchPlugin.getDefault().getToolPage(tool);
		if (dynamicTab == null) {
			throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, Messages.getString("ValgrindOptionsTab.No_options_tab_found") + tool)); //$NON-NLS-1$
		}
		dynamicTab.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		dynamicTab.createControl(dynamicTabHolder);

		dynamicTabHolder.layout(true);
	}

	public String getName() {
		return Messages.getString("ValgrindOptionsTab.Valgrind_Options"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return ValgrindLaunchPlugin.imageDescriptorFromPlugin(ValgrindLaunchPlugin.PLUGIN_ID, "icons/valgrind-icon.png").createImage(); //$NON-NLS-1$
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		isInitializing = true;
		getControl().setRedraw(false);
		launchConfiguration = configuration;
		launchConfigurationWorkingCopy = null;

		try {
			tool = configuration.getAttribute(LaunchConfigurationConstants.ATTR_TOOL, ValgrindLaunchPlugin.TOOL_EXT_DEFAULT);
			int select = -1;
			for (int i = 0; i < tools.length && select < 0; i++) {
				if (tool.equals(tools[i])) {
					select = i;
				}
			}

			if (select != -1) {
				toolsCombo.select(select);
			}
			handleToolChanged();
			initializeGeneral(configuration);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		getControl().setRedraw(true);
		isInitializing = false;
	}

	protected void initializeGeneral(ILaunchConfiguration configuration) throws CoreException {
		traceChildrenButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, false));
		//		childSilentButton.setSelection(configuration.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_CHILDSILENT, false));
		//		trackFdsButton.setSelection(configuration.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TRACKFDS, false));
		//		timeStampButton.setSelection(configuration.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TIMESTAMP, false));
		runFreeresButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, true));

		demangleButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, true));
		numCallersSpinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, 12));
		errorLimitButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, true));
		showBelowMainButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, false));
		maxStackFrameSpinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, 2000000));
		suppFileText.setText(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, EMPTY_STRING));
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		boolean result = false;
		if (result = isGeneralValid() && dynamicTab != null) {
			result = dynamicTab.isValid(launchConfig);
		}
		return result;
	}

	protected boolean isGeneralValid() {
		String strpath = suppFileText.getText();
		boolean result = false;
		if (strpath.equals(EMPTY_STRING)) {
			result = true;
		}
		else {
			try {
				File suppfile = ValgrindLaunchPlugin.getDefault().parseWSPath(strpath);
				if (suppfile != null && suppfile.exists()) {
					result = true;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		if (!result) {
			setErrorMessage(NLS.bind(Messages.getString("ValgrindOptionsTab.suppressions_file_doesnt_exist"), strpath)); //$NON-NLS-1$
		}
		return result;
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, tool);
		applyGeneralAttributes(configuration);
		if (dynamicTab != null) {
			dynamicTab.performApply(configuration);
		}
	}

	protected void applyGeneralAttributes(
			ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, traceChildrenButton.getSelection());
		//		configuration.setAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_CHILDSILENT, childSilentButton.getSelection());
		//		configuration.setAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TRACKFDS, trackFdsButton.getSelection());
		//		configuration.setAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TIMESTAMP, timeStampButton.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, runFreeresButton.getSelection());

		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, demangleButton.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, numCallersSpinner.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, errorLimitButton.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, showBelowMainButton.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, maxStackFrameSpinner.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, suppFileText.getText());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		launchConfigurationWorkingCopy = configuration;
		
		setDefaultGeneralAttributes(configuration);
		if (dynamicTab != null) {
			dynamicTab.setDefaults(configuration);
			initDefaults = false;
		}
	}

	public static void setDefaultGeneralAttributes(
			ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, ValgrindLaunchPlugin.TOOL_EXT_DEFAULT);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, false);
		//		configuration.setAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_CHILDSILENT, false);
		//		configuration.setAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TRACKFDS, false);
		//		configuration.setAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TIMESTAMP, false);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, true);

		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, true);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, 12);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, true);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, false);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, 2000000);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, EMPTY_STRING);
	}

	@Override
	public void dispose() {
		if (dynamicTab != null) {
			dynamicTab.dispose();
		}
		super.dispose();
	}

	protected void createHorizontalSpacer(Composite comp, int numlines) {
		Label lbl = new Label(comp, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numlines;
		lbl.setLayoutData(gd);
	}

	@Override
	protected void updateLaunchConfigurationDialog() {
		if (!isInitializing) {
			super.updateLaunchConfigurationDialog();
		}		
	}

}
