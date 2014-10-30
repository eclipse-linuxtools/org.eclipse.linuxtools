/*******************************************************************************
 * Copyright (c) 2008, 2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 * Martin Oberhuber (Wind River) - [354342] make valgrind version changeable
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
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
import org.eclipse.linuxtools.profiling.launch.ConfigUtils;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;
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
    protected Button dSymUtilButton;
    protected List suppFileList;

    protected String tool;
    protected String[] tools;

    protected Composite top;
    protected Composite mainStackSizeTop;
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

    protected Exception ex;

    /**
     * @since 1.2
     */
    protected boolean noToolCombo;

    private Version valgrindVersion;
    private boolean checkVersion;

    public ValgrindOptionsTab() {
        this(true);
    }

    public ValgrindOptionsTab(boolean checkVersion) {
        this.checkVersion = checkVersion;
    }

    private SelectionListener selectListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            updateLaunchConfigurationDialog();
        }
    };
    private ModifyListener modifyListener = new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }
    };

    @Override
    public void createControl(Composite parent) {
        // Check for exception
        if (ex != null) {
            setErrorMessage(ex.getLocalizedMessage());
        }
        scrollTop = new ScrolledComposite(parent,    SWT.H_SCROLL | SWT.V_SCROLL);
        scrollTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scrollTop.setExpandVertical(true);
        scrollTop.setExpandHorizontal(true);

        setControl(scrollTop);

        top = new Composite(scrollTop, SWT.NONE);
        top.setLayout(new GridLayout());

        createVerticalSpacer(top, 1);

        // provide the tool combo if it is not excluded
        if (!noToolCombo)
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

        updateLaunchConfigurationDialog();
    }

    private void recomputeSize() {
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
            @Override
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

    private void createBasicOptions(Composite top) {
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

    private void createErrorOptions(Composite top) {
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

        //Option only visible for valgrind > 3.4.0
        mainStackSizeTop = new Composite(errorTop, SWT.NONE);
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
        mainStackSizeTop.setVisible(false);

        //Option only visible for valgrind > 3.6.0
        dSymUtilButton = new Button(errorTop, SWT.CHECK);
        dSymUtilButton.setText(Messages.getString("ValgrindOptionsTab.dsymutil")); //$NON-NLS-1$
        dSymUtilButton.addSelectionListener(selectListener);
        dSymUtilButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dSymUtilButton.setVisible(false);
    }

    private void updateErrorOptions() {
        if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0)
            mainStackSizeTop.setVisible(true);
        else
            mainStackSizeTop.setVisible(false);

        if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_6_0) >= 0)
            dSymUtilButton.setVisible(true);
        else
            dSymUtilButton.setVisible(false);
    }

    private void createSuppressionsOption(Composite top) {
        Composite browseTop = new Composite(top, SWT.BORDER);
        browseTop.setLayout(new GridLayout(2, false));
        GridData browseData = new GridData(GridData.FILL_BOTH);
        browseTop.setLayoutData(browseData);

        Label suppFileLabel = new Label(browseTop, SWT.NONE);
        suppFileLabel.setText(Messages.getString("ValgrindOptionsTab.suppressions_file")); //$NON-NLS-1$

        createVerticalSpacer(browseTop, 1);

        suppFileList = new List(browseTop, SWT.BORDER);
        suppFileList.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttonTop = new Composite(browseTop, SWT.NONE);
        GridLayout buttonLayout = new GridLayout();
        buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
        buttonTop.setLayout(buttonLayout);
        buttonTop.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false));

        Button workspaceBrowseButton = createPushButton(buttonTop, Messages.getString("ValgrindOptionsTab.Workspace"), null);  //$NON-NLS-1$
        workspaceBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
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
                    suppFileList.add(fileLoc);
                    updateLaunchConfigurationDialog();
                }
            }
        });
        Button fileBrowseButton = createPushButton(buttonTop, Messages.getString("ValgrindOptionsTab.File_System"), null); //$NON-NLS-1$
        fileBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String filePath = null;
                FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                filePath = dialog.open();
                if (filePath != null) {
                    suppFileList.add(filePath);
                    updateLaunchConfigurationDialog();
                }
            }
        });
        Button removeButton = createPushButton(buttonTop, Messages.getString("ValgrindOptionsTab.Supp_remove"), null); //$NON-NLS-1$
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selected = suppFileList.getSelectionIndices();
                if (selected.length > 0) {
                    suppFileList.remove(selected);
                    updateLaunchConfigurationDialog();
                }
            }
        });
    }

    private void handleToolChanged() {
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
            ex = e;
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
        dynamicTab.setValgrindVersion(valgrindVersion);
        dynamicTab.createControl(dynamicTabHolder);

        dynamicTabHolder.layout(true);
    }

    private void loadDynamicTab() throws CoreException {
         dynamicTab = getPlugin().getToolPage(tool);
    }

    public IValgrindToolPage getDynamicTab() {
        return dynamicTab;
    }

    private ValgrindLaunchPlugin getPlugin() {
        return ValgrindLaunchPlugin.getDefault();
    }

    @Override
    public String getName() {
        return Messages.getString("ValgrindOptionsTab.Valgrind_Options"); //$NON-NLS-1$
    }

    @Override
    public Image getImage() {
        return AbstractUIPlugin.imageDescriptorFromPlugin(ValgrindLaunchPlugin.PLUGIN_ID, "icons/valgrind-icon.png").createImage(); //$NON-NLS-1$
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        isInitializing = true;
        getControl().setRedraw(false);
        launchConfiguration = configuration;
        launchConfigurationWorkingCopy = null;

        if (checkVersion) {
            try {
                IProject project;
                try {
                    project = ConfigUtils.getProject(ConfigUtils.getProjectName(configuration));
                } catch (Exception e1) {
                    // no project is still a possibility the validator handles
                    project = null;
                }
                valgrindVersion = getPlugin().getValgrindVersion(project);
            } catch (CoreException e) {
                ex = e;
            }
        }

        updateErrorOptions();

        try {
            if (!noToolCombo) {
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
            }
            handleToolChanged();

            traceChildrenButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, LaunchConfigurationConstants.DEFAULT_GENERAL_TRACECHILD));
            runFreeresButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, LaunchConfigurationConstants.DEFAULT_GENERAL_FREERES));
            demangleButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, LaunchConfigurationConstants.DEFAULT_GENERAL_DEMANGLE));
            numCallersSpinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, LaunchConfigurationConstants.DEFAULT_GENERAL_NUMCALLERS));
            errorLimitButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, LaunchConfigurationConstants.DEFAULT_GENERAL_ERRLIMIT));
            showBelowMainButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, LaunchConfigurationConstants.DEFAULT_GENERAL_BELOWMAIN));
            maxStackFrameSpinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, LaunchConfigurationConstants.DEFAULT_GENERAL_MAXFRAME));
            java.util.List<String> suppFiles = configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILES, LaunchConfigurationConstants.DEFAULT_GENERAL_SUPPFILES);
            suppFileList.setItems(suppFiles.toArray(new String[suppFiles.size()]));

            // 3.4.0 specific
            if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
                mainStackSizeButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK_BOOL, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK_BOOL));
                mainStackSizeSpinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK));
                checkMainStackEnablement();
            }

            // 3.6.0 specific
            if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_6_0) >= 0) {
                dSymUtilButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DSYMUTIL, LaunchConfigurationConstants.DEFAULT_GENERAL_DSYMUTIL));
            }
        } catch (CoreException e) {
            ex = e;
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
        String[] suppFiles = suppFileList.getItems();
        boolean result = true;
        for (int i = 0; i < suppFiles.length && result; i++) {
            try {
                IPath suppfile = getPlugin().parseWSPath(suppFiles[i]);
                if (!suppfile.toFile().exists()) {
                    setErrorMessage(NLS.bind(Messages.getString("ValgrindOptionsTab.suppressions_file_doesnt_exist"), suppFiles[i])); //$NON-NLS-1$
                    result = false;
                }
            } catch (CoreException e) {
                // should only occur if there's a cycle in variable substitution
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, tool);

        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, traceChildrenButton.getSelection());
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, runFreeresButton.getSelection());

        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, demangleButton.getSelection());
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, numCallersSpinner.getSelection());
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, errorLimitButton.getSelection());
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, showBelowMainButton.getSelection());
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, maxStackFrameSpinner.getSelection());
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILES, Arrays.asList(suppFileList.getItems()));

        // 3.4.0 specific
        if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK_BOOL, mainStackSizeButton.getSelection());
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK, mainStackSizeSpinner.getSelection());
        }

        // 3.6.0 specific
        if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_6_0) >= 0) {
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DSYMUTIL, dSymUtilButton.getSelection());
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_FULLPATH_AFTER, LaunchConfigurationConstants.DEFAULT_FULLPATH_AFTER);
        }

        if (dynamicTab != null) {
            dynamicTab.performApply(configuration);
        }
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        launchConfigurationWorkingCopy = configuration;

        if (noToolCombo)
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, tool);
        else
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, LaunchConfigurationConstants.DEFAULT_TOOL);
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, LaunchConfigurationConstants.DEFAULT_GENERAL_TRACECHILD);
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, LaunchConfigurationConstants.DEFAULT_GENERAL_FREERES);

        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, LaunchConfigurationConstants.DEFAULT_GENERAL_DEMANGLE);
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, LaunchConfigurationConstants.DEFAULT_GENERAL_NUMCALLERS);
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, LaunchConfigurationConstants.DEFAULT_GENERAL_ERRLIMIT);
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, LaunchConfigurationConstants.DEFAULT_GENERAL_BELOWMAIN);
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, LaunchConfigurationConstants.DEFAULT_GENERAL_MAXFRAME);
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILES, LaunchConfigurationConstants.DEFAULT_GENERAL_SUPPFILES);

        // 3.4.0 specific
        if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK_BOOL, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK_BOOL);
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK);
        }

        // 3.6.0 specific
        if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_6_0) >= 0) {
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DSYMUTIL, LaunchConfigurationConstants.DEFAULT_GENERAL_DSYMUTIL);
            configuration.setAttribute(LaunchConfigurationConstants.ATTR_FULLPATH_AFTER, LaunchConfigurationConstants.DEFAULT_FULLPATH_AFTER);
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

    public List getSuppFileList() {
        return suppFileList;
    }

    public Combo getToolsCombo() {
        return toolsCombo;
    }

    public String[] getTools() {
        return tools;
    }
}
