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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.valgrind.core.LaunchConfigurationConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.osgi.framework.Version;

public class ValgrindOptionsTab extends AbstractLaunchConfigurationTab {
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	// General controls
	protected Button traceChildrenButton;
	protected Button childSilentButton;
	protected Button runFreeresButton;

	protected Button demangleButton;
	protected Spinner numCallersSpinner;
	protected Button errorLimitButton;
	protected Button showBelowMainButton;
	protected Spinner maxStackFrameSpinner;
	protected Button mainStackSizeButton;
	protected Spinner mainStackSizeSpinner;
	protected Text suppFileText;

	protected String tool;
	protected String[] tools;

	protected Composite top;
	protected ScrolledComposite scrollTop;
	protected Combo toolsCombo;
	protected TabFolder optionsFolder;
	protected TabItem toolTab;

	protected ILaunchConfigurationWorkingCopy launchConfigurationWorkingCopy;
	protected ILaunchConfiguration launchConfiguration;

	protected IValgrindToolPage dynamicTab;
	protected Composite dynamicTabHolder;

	protected boolean isInitializing = false;
	protected boolean initDefaults = false;
	
	protected IPath valgrindLocation;
	protected Version valgrindVersion;
	protected Exception ex;

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
		scrollTop = new ScrolledComposite(parent,	SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);
		
		setControl(scrollTop);
		
		top = new Composite(scrollTop, SWT.NONE);
		top.setLayout(new GridLayout());

		createVerticalSpacer(top, 1);

		createToolCombo(top);

		createVerticalSpacer(top, 1);

		optionsFolder = new TabFolder(top, SWT.BORDER);
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
		
		TabItem suppTab = new TabItem(optionsFolder, SWT.NONE);
		suppTab.setText(Messages.getString("ValgrindOptionsTab.Suppressions")); //$NON-NLS-1$
		
		Composite suppTop = new Composite(optionsFolder, SWT.NONE);
		suppTop.setLayout(new GridLayout());
		suppTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		createSuppressionsOption(suppTop);
		
		suppTab.setControl(suppTop);
		
		toolTab = new TabItem(optionsFolder, SWT.NONE);
		toolTab.setText(Messages.getString("ValgrindOptionsTab.Tool")); //$NON-NLS-1$

		dynamicTabHolder = new Composite(optionsFolder, SWT.NONE);
		dynamicTabHolder.setLayout(new GridLayout());
		dynamicTabHolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		toolTab.setControl(dynamicTabHolder);
		
		scrollTop.setContent(top);
		recomputeSize();
	}

	protected void recomputeSize() {
		Point point = top.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		top.setSize(point);
		scrollTop.setMinSize(point);
	}

	private void createToolCombo(Composite top) {
		Composite comboTop = new Composite(top, SWT.NONE);
		comboTop.setLayout(new GridLayout(2, false));
		Label toolLabel = new Label(comboTop, SWT.NONE);
		toolLabel.setText(Messages.getString("ValgrindOptionsTab.Tool_to_run")); //$NON-NLS-1$
		toolsCombo = new Combo(comboTop, SWT.READ_ONLY);
		tools = getPlugin().getRegisteredToolIDs();

		String[] names = new String[tools.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = capitalize(getPlugin().getToolName(tools[i]));
		}
		toolsCombo.setItems(names);

		toolsCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
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

	private String capitalize(String str) {
		if (str.length() > 0) {
			char[] buf = str.toCharArray();
			buf[0] = Character.toUpperCase(buf[0]);
			
			str = String.valueOf(buf);
		}
		return str;
	}

	protected void createBasicOptions(Composite top) {
		Group basicGroup = new Group(top, SWT.NONE);
		basicGroup.setLayout(new GridLayout());
		basicGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		basicGroup.setText(Messages.getString("ValgrindOptionsTab.Basic_Options")); //$NON-NLS-1$

		Composite basicTop = new Composite(basicGroup, SWT.NONE);
		basicTop.setLayout(new GridLayout(2, true));
		basicTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		traceChildrenButton = new Button(basicTop, SWT.CHECK);
		traceChildrenButton.setText(Messages.getString("ValgrindOptionsTab.trace_children")); //$NON-NLS-1$
		traceChildrenButton.addSelectionListener(selectListener);
		traceChildrenButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Must be on to prevent mangled XML output
		childSilentButton = new Button(basicTop, SWT.CHECK);
		childSilentButton.setText(Messages.getString("ValgrindOptionsTab.child_silent")); //$NON-NLS-1$
		childSilentButton.setSelection(true);
		childSilentButton.setEnabled(false);
		childSilentButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		runFreeresButton = new Button(basicTop, SWT.CHECK);
		runFreeresButton.setText(Messages.getString("ValgrindOptionsTab.run_freeres")); //$NON-NLS-1$
		runFreeresButton.addSelectionListener(selectListener);
		runFreeresButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void createErrorOptions(Composite top) {
		Group errorGroup = new Group(top, SWT.NONE);
		errorGroup.setLayout(new GridLayout());
		errorGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		errorGroup.setText(Messages.getString("ValgrindOptionsTab.Error_Options")); //$NON-NLS-1$

		Composite errorTop = new Composite(errorGroup, SWT.NONE);
		errorTop.setLayout(new GridLayout(2, true));
		errorTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		demangleButton = new Button(errorTop, SWT.CHECK);
		demangleButton.setText(Messages.getString("ValgrindOptionsTab.demangle")); //$NON-NLS-1$
		demangleButton.addSelectionListener(selectListener);
		demangleButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite numCallersTop = new Composite(errorTop, SWT.NONE);
		numCallersTop.setLayout(new GridLayout(2, false));
		Label numCallersLabel = new Label(numCallersTop, SWT.NONE);
		numCallersLabel.setText(Messages.getString("ValgrindOptionsTab.num_callers")); //$NON-NLS-1$
		numCallersSpinner = new Spinner(numCallersTop, SWT.BORDER);
		numCallersSpinner.setMaximum(50);
		numCallersSpinner.addModifyListener(modifyListener);
		numCallersSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		errorLimitButton = new Button(errorTop, SWT.CHECK);
		errorLimitButton.setText(Messages.getString("ValgrindOptionsTab.limit_errors")); //$NON-NLS-1$
		errorLimitButton.addSelectionListener(selectListener);
		errorLimitButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		showBelowMainButton = new Button(errorTop, SWT.CHECK);
		showBelowMainButton.setText(Messages.getString("ValgrindOptionsTab.show_errors_below_main")); //$NON-NLS-1$
		showBelowMainButton.addSelectionListener(selectListener);
		showBelowMainButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite maxStackFrameTop = new Composite(errorTop, SWT.NONE);
		maxStackFrameTop.setLayout(new GridLayout(2, false));
		Label maxStackFrameLabel = new Label(maxStackFrameTop, SWT.NONE);
		maxStackFrameLabel.setText(Messages.getString("ValgrindOptionsTab.max_size_of_stack_frame")); //$NON-NLS-1$
		maxStackFrameSpinner = new Spinner(maxStackFrameTop, SWT.BORDER);
		maxStackFrameSpinner.setMaximum(Integer.MAX_VALUE);
		maxStackFrameSpinner.addModifyListener(modifyListener);	
		maxStackFrameSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// 3.4.0 specific
		try {
			Version ver = getPlugin().getValgrindVersion();
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				Composite mainStackSizeTop = new Composite(errorTop, SWT.NONE);
				GridLayout mainStackSizeLayout = new GridLayout(2, false);
				mainStackSizeLayout.marginHeight = mainStackSizeLayout.marginWidth = 0;
				mainStackSizeTop.setLayout(mainStackSizeLayout);
				mainStackSizeButton = new Button(mainStackSizeTop, SWT.CHECK);
				mainStackSizeButton.setText(Messages.getString("ValgrindOptionsTab.Main_stack_size")); //$NON-NLS-1$
				mainStackSizeButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						checkMainStackEnablement();
						updateLaunchConfigurationDialog();
					}
				});
				mainStackSizeSpinner = new Spinner(mainStackSizeTop, SWT.BORDER);
				mainStackSizeSpinner.setMaximum(Integer.MAX_VALUE);
				mainStackSizeSpinner.addModifyListener(modifyListener);
				mainStackSizeSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
		} catch (CoreException e) {
			e.printStackTrace();
			ex = e;
		}
	}

	protected void createSuppressionsOption(Composite top) {
		Composite browseTop = new Composite(top, SWT.NONE);		
		browseTop.setLayout(new GridLayout(4, false));
		GridData browseData = new GridData(GridData.FILL_HORIZONTAL);
		browseTop.setLayoutData(browseData);

		Label suppFileLabel = new Label(browseTop, SWT.NONE);
		suppFileLabel.setText(Messages.getString("ValgrindOptionsTab.suppressions_file")); //$NON-NLS-1$
		
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
			
			// change name of tool TabItem
			toolTab.setText(dynamicTab.getName());
			optionsFolder.layout(true);
			
			// adjust minimum size for ScrolledComposite
			recomputeSize();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void loadDynamicArea() throws CoreException {
		for (Control child : dynamicTabHolder.getChildren()) {
			child.dispose();
		}
		
		loadDynamicTab();
		if (dynamicTab == null) {
			throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, Messages.getString("ValgrindOptionsTab.No_options_tab_found") + tool)); //$NON-NLS-1$
		}
		dynamicTab.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		dynamicTab.createControl(dynamicTabHolder);

		dynamicTabHolder.layout(true);		
	}

	private void loadDynamicTab() throws CoreException {
		 dynamicTab = getPlugin().getToolPage(tool);		
	}
	
	public IValgrindToolPage getDynamicTab() {
		return dynamicTab;
	}

	protected ValgrindLaunchPlugin getPlugin() {
		return ValgrindLaunchPlugin.getDefault();
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
			tool = configuration.getAttribute(LaunchConfigurationConstants.ATTR_TOOL, LaunchConfigurationConstants.DEFAULT_TOOL);
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
			
			traceChildrenButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, LaunchConfigurationConstants.DEFAULT_GENERAL_TRACECHILD));
			runFreeresButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, LaunchConfigurationConstants.DEFAULT_GENERAL_FREERES));
			demangleButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, LaunchConfigurationConstants.DEFAULT_GENERAL_DEMANGLE));
			numCallersSpinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, LaunchConfigurationConstants.DEFAULT_GENERAL_NUMCALLERS));
			errorLimitButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, LaunchConfigurationConstants.DEFAULT_GENERAL_ERRLIMIT));
			showBelowMainButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, LaunchConfigurationConstants.DEFAULT_GENERAL_BELOWMAIN));
			maxStackFrameSpinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, LaunchConfigurationConstants.DEFAULT_GENERAL_MAXFRAME));
			suppFileText.setText(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, LaunchConfigurationConstants.DEFAULT_GENERAL_SUPPFILE));
			
			// 3.4.0 specific
			Version ver = getPlugin().getValgrindVersion();
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				mainStackSizeButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK_BOOL, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK_BOOL));
				mainStackSizeSpinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK));
				checkMainStackEnablement();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		getControl().setRedraw(true);
		isInitializing = false;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		boolean result = false;
		if (ex != null) {
			setErrorMessage(ex.getLocalizedMessage());
		}
		else if (result = isGeneralValid() && dynamicTab != null) {
			result = dynamicTab.isValid(launchConfig);
			setErrorMessage(dynamicTab.getErrorMessage());
		}
		return result;
	}

	private boolean isGeneralValid() {
		String strpath = suppFileText.getText();
		boolean result = false;
		if (strpath.equals(EMPTY_STRING)) {
			result = true;
		}
		else {
			try {
				IPath suppfile = getPlugin().parseWSPath(strpath);
				if (suppfile.toFile().exists()) {
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
		
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, traceChildrenButton.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, runFreeresButton.getSelection());

		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, demangleButton.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, numCallersSpinner.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, errorLimitButton.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, showBelowMainButton.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, maxStackFrameSpinner.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, suppFileText.getText());
		
		// 3.4.0 specific
		try {
			Version ver = getPlugin().getValgrindVersion();
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK_BOOL, mainStackSizeButton.getSelection());
				configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK, mainStackSizeSpinner.getSelection());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (dynamicTab != null) {
			dynamicTab.performApply(configuration);
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		launchConfigurationWorkingCopy = configuration;
		
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, LaunchConfigurationConstants.DEFAULT_TOOL);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, LaunchConfigurationConstants.DEFAULT_GENERAL_TRACECHILD);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, LaunchConfigurationConstants.DEFAULT_GENERAL_FREERES);

		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, LaunchConfigurationConstants.DEFAULT_GENERAL_DEMANGLE);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, LaunchConfigurationConstants.DEFAULT_GENERAL_NUMCALLERS);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, LaunchConfigurationConstants.DEFAULT_GENERAL_ERRLIMIT);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, LaunchConfigurationConstants.DEFAULT_GENERAL_BELOWMAIN);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, LaunchConfigurationConstants.DEFAULT_GENERAL_MAXFRAME);
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, LaunchConfigurationConstants.DEFAULT_GENERAL_SUPPFILE);
		
		// 3.4.0 specific
		try {
			Version ver = getPlugin().getValgrindVersion();
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK_BOOL, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK_BOOL);
				configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		if (dynamicTab != null) {
			dynamicTab.setDefaults(configuration);
			initDefaults = false;
		}
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

	private void checkMainStackEnablement() {
		mainStackSizeSpinner.setEnabled(mainStackSizeButton.getSelection());
	}

	public Button getTraceChildrenButton() {
		return traceChildrenButton;
	}

	public Button getChildSilentButton() {
		return childSilentButton;
	}

	public Button getRunFreeresButton() {
		return runFreeresButton;
	}

	public Button getDemangleButton() {
		return demangleButton;
	}

	public Spinner getNumCallersSpinner() {
		return numCallersSpinner;
	}

	public Button getErrorLimitButton() {
		return errorLimitButton;
	}

	public Button getShowBelowMainButton() {
		return showBelowMainButton;
	}

	public Spinner getMaxStackFrameSpinner() {
		return maxStackFrameSpinner;
	}

	public Button getMainStackSizeButton() {
		return mainStackSizeButton;
	}

	public Spinner getMainStackSizeSpinner() {
		return mainStackSizeSpinner;
	}

	public Text getSuppFileText() {
		return suppFileText;
	}

	public Combo getToolsCombo() {
		return toolsCombo;
	}

	public String[] getTools() {
		return tools;
	}
}
