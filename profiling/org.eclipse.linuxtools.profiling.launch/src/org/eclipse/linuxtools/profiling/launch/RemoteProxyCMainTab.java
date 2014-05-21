/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ken Ryall (Nokia) - bug 178731
 *       IBM Corporation
 *       Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.ui.CAbstractMainTab;
import org.eclipse.cdt.launch.ui.ICDTLaunchHelpContextIds;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;
import org.eclipse.linuxtools.profiling.launch.ui.ResourceSelectorWidget;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;


/**
 * A launch configuration tab that displays and edits project and main type name
 * launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 *
 * @since 1.1
 */
@SuppressWarnings("restriction")
public class RemoteProxyCMainTab extends CAbstractMainTab {

    /**
     * Tab identifier used for ordering of tabs added using the
     * <code>org.eclipse.debug.ui.launchConfigurationTabs</code> extension
     * point.
     *
     * @since 6.0
     */
    public static final String TAB_ID = "org.eclipse.linuxtools.profiling.launch.RemoteProxyCMainTab"; //$NON-NLS-1$

    private final boolean fWantsTerminalOption;
    protected Button fTerminalButton;

    protected ResourceSelectorWidget exeSelector;

    protected Text copyFromExeText;
    protected Button enableCopyFromExeButton;
    protected ResourceSelectorWidget copyFromExeSelector;
    protected Label toLabel;
    protected String fPreviouslyCheckedCopyFromExe;
    protected boolean fPreviouslyCheckedCopyFromExeIsValid;
    protected String fPreviouslyCheckedCopyFromExeErrorMsg;

    protected Text workingDirText;
    protected String fPreviouslyCheckedWorkingDir;
    protected boolean fPreviouslyCheckedWorkingDirIsValid;
    protected String fPreviouslyCheckedWorkingDirErrorMsg;

    private final boolean dontCheckProgram;
    private final boolean fSpecifyCoreFile;

    public static final int WANTS_TERMINAL = 1;
    public static final int DONT_CHECK_PROGRAM = 2;
    /** @since 6.0 */
    public static final int SPECIFY_CORE_FILE = 4;

    public static final String ATTR_REMOTE_WORKING_DIRECTORY_NAME = "REMOTE_WORKING_DIRECTORY_NAME"; //$NON-NLS-1$
    public static final String ATTR_COPY_FROM_EXE_NAME = "COPY_FROM_EXE_NAME"; //$NON-NLS-1$
    public static final String ATTR_ENABLE_COPY_FROM_EXE = "ENABLE_COPY_FROM_EXE"; //$NON-NLS-1$

    public RemoteProxyCMainTab() {
        this(WANTS_TERMINAL);
    }

    public RemoteProxyCMainTab(boolean terminalOption) {
        this(terminalOption ? WANTS_TERMINAL : 0);
    }

    public RemoteProxyCMainTab(int flags) {
        fWantsTerminalOption = (flags & WANTS_TERMINAL) != 0;
        dontCheckProgram = (flags & DONT_CHECK_PROGRAM) != 0;
        fSpecifyCoreFile = (flags & SPECIFY_CORE_FILE) != 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);

        ProfileLaunchPlugin
        .getDefault()
        .getWorkbench()
        .getHelpSystem()
        .setHelp(
                getControl(),
                ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

        GridLayout topLayout = new GridLayout();
        comp.setLayout(topLayout);

        createVerticalSpacer(comp, 1);
        createCopyFromExeGroup(comp, 1);
        createExeFileGroup(comp, 1);
        createWorkingDirGroup(comp, 1);
        createProjectGroup(comp, 1);
        createBuildOptionGroup(comp, 1);
        createVerticalSpacer(comp, 1);
        if (fSpecifyCoreFile) {
            createCoreFileGroup(comp, 1);
        }
        if (wantsTerminalOption() /* && ProcessFactory.supportesTerminal() */) {
            createTerminalOption(comp, 1);
        }
    }

    protected boolean wantsTerminalOption() {
        return fWantsTerminalOption;
    }

    protected void createTerminalOption(Composite parent, int colSpan) {
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout mainLayout = new GridLayout();
        mainLayout.numColumns = 1;
        mainLayout.marginHeight = 0;
        mainLayout.marginWidth = 0;
        mainComp.setLayout(mainLayout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = colSpan;
        mainComp.setLayoutData(gd);

        fTerminalButton = createCheckButton(mainComp,
                LaunchMessages.CMainTab_UseTerminal);
        fTerminalButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
        fTerminalButton.setEnabled(PTY.isSupported());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
     * .debug.core.ILaunchConfiguration)
     */
    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        filterPlatform = getPlatform(config);
        updateProjectFromConfig(config);
        updateCopyFromExeFromConfig(config);
        updateProgramFromConfig(config);
        updateWorkingDirFromConfig(config);
        updateCoreFromConfig(config);
        updateBuildOptionFromConfig(config);
        updateTerminalFromConfig(config);
    }

    protected void updateTerminalFromConfig(ILaunchConfiguration config) {
        if (fTerminalButton != null) {
            boolean useTerminal = true;
            try {
                useTerminal = config.getAttribute(
                        ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
                        ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
            } catch (CoreException e) {
                ProfileLaunchPlugin.log(e);
            }
            fTerminalButton.setSelection(useTerminal);
        }
    }

    /**
     * @param config The launch config to update from.
     * @since 6.0
     * */
    protected void updateCoreFromConfig(ILaunchConfiguration config) {
        if (fCoreText != null) {
            String coreName = EMPTY_STRING;
            try {
                coreName = config.getAttribute(
                        ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
                        EMPTY_STRING);
            } catch (CoreException ce) {
                ProfileLaunchPlugin.log(ce);
            }
            fCoreText.setText(coreName);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy config) {
        super.performApply(config);
        ICProject cProject = this.getCProject();
        if (cProject != null && cProject.exists()) {
            config.setMappedResources(new IResource[] { cProject.getProject() });
        } else {
            // the user typed in a non-existent project name. Ensure that
            // won't be suppressed from the dialog. This matches JDT behaviour
            config.setMappedResources(null);
        }
        config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                fProjText.getText());
        if (enableCopyFromExeButton != null) {
            config.setAttribute(
                    ATTR_ENABLE_COPY_FROM_EXE,
                    enableCopyFromExeButton.getSelection());
        }
        if (copyFromExeText != null) {
            config.setAttribute(
                    ATTR_COPY_FROM_EXE_NAME,
                    copyFromExeText.getText());
        }
        if (fProgText != null) {
            config.setAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
                    fProgText.getText());
        }
        if (workingDirText != null) {
            config.setAttribute(
                    ATTR_REMOTE_WORKING_DIRECTORY_NAME,
                    workingDirText.getText());
        }
        if (fCoreText != null) {
            config.setAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
                    fCoreText.getText());
        }
        if (fTerminalButton != null) {
            config.setAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
                    fTerminalButton.getSelection());
        }
    }

    /**
     * Show a dialog that lists all main types
     */
    @Override
    protected void handleSearchButtonSelected() {
        if (getCProject() == null) {
            MessageDialog
            .openInformation(
                    getShell(),
                    LaunchMessages.CMainTab_Project_required,
                    LaunchMessages.CMainTab_Enter_project_before_searching_for_program);
            return;
        }

        ILabelProvider programLabelProvider = new CElementLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IBinary) {
                    IBinary bin = (IBinary) element;
                    StringBuffer name = new StringBuffer();
                    name.append(bin.getPath().lastSegment());
                    return name.toString();
                }
                return super.getText(element);
            }

            @Override
            public Image getImage(Object element) {
                if (!(element instanceof ICElement)) {
                    return super.getImage(element);
                }
                ICElement celement = (ICElement) element;

                if (celement.getElementType() == ICElement.C_BINARY) {
                    IBinary belement = (IBinary) celement;
                    if (belement.isExecutable()) {
                        return DebugUITools
                                .getImage(IDebugUIConstants.IMG_ACT_RUN);
                    }
                }

                return super.getImage(element);
            }
        };

        ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IBinary) {
                    IBinary bin = (IBinary) element;
                    StringBuffer name = new StringBuffer();
                    name.append(bin.getCPU()
                            + (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
                    name.append(" - "); //$NON-NLS-1$
                    name.append(bin.getPath().toString());
                    return name.toString();
                }
                return super.getText(element);
            }
        };

        TwoPaneElementSelector dialog = new TwoPaneElementSelector(getShell(),
                programLabelProvider, qualifierLabelProvider);
        dialog.setElements(getBinaryFiles(getCProject()));
        dialog.setMessage(LaunchMessages.CMainTab_Choose_program_to_run);
        dialog.setTitle(LaunchMessages.CMainTab_Program_Selection);
        dialog.setUpperListLabel(LaunchMessages.Launch_common_BinariesColon);
        dialog.setLowerListLabel(LaunchMessages.Launch_common_QualifierColon);
        dialog.setMultipleSelection(false);
        // dialog.set
        if (dialog.open() == Window.OK) {
            IBinary binary = (IBinary) dialog.getFirstResult();
            fProgText.setText(binary.getResource().getProjectRelativePath()
                    .toString());
        }
    }

    /**
     * @since 6.0
     */
    @Override
    protected void createProjectGroup(Composite parent, int colSpan) {
        Composite projComp = new Composite(parent, SWT.NONE);
        GridLayout projLayout = new GridLayout();
        projLayout.numColumns = 2;
        projLayout.marginHeight = 0;
        projLayout.marginWidth = 0;
        projComp.setLayout(projLayout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = colSpan;
        projComp.setLayoutData(gd);

        fProjLabel = new Label(projComp, SWT.NONE);
        fProjLabel.setText(LaunchMessages.CMainTab_ProjectColon);
        gd = new GridData();
        gd.horizontalSpan = 2;
        fProjLabel.setLayoutData(gd);

        fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        fProjText.setLayoutData(gd);
        fProjText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent evt) {
                // if project changes, invalidate program name cache
                fPreviouslyCheckedProgram = null;

                updateBuildConfigCombo(""); //$NON-NLS-1$
                updateLaunchConfigurationDialog();
            }
        });

        fProjButton = createPushButton(projComp,
                LaunchMessages.Launch_common_Browse_1, null);
        fProjButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent evt) {
                handleProjectButtonSelected();
                updateLaunchConfigurationDialog();
            }
        });
    }

    @Override
    protected void updateProgramFromConfig(ILaunchConfiguration config) {
        super.updateProgramFromConfig(config);
        if(fProgText.getText().equals(EMPTY_STRING)){
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            if(this.fProjText != null){
                IProject project = root.getProject(this.fProjText.getText());
                if(project != null){
                    fProgText.setText(project.getLocationURI().toString());
                }
            }
        }
    }

    protected void updateCopyFromExeFromConfig(ILaunchConfiguration config) {
        if (copyFromExeText != null) {
            String workingDir = EMPTY_STRING;
            try {
                workingDir = config.getAttribute(ATTR_COPY_FROM_EXE_NAME, EMPTY_STRING);
            } catch (CoreException ce) {
                ProfileLaunchPlugin.log(ce);
            }
            copyFromExeText.setText(workingDir);
        }
        if (enableCopyFromExeButton != null) {
            boolean enableCopyFromExe = false;
            try {
                enableCopyFromExe = config.getAttribute(ATTR_ENABLE_COPY_FROM_EXE, false);
            } catch (CoreException ce) {
                ProfileLaunchPlugin.log(ce);
            }
            setEnableCopyFromSection(enableCopyFromExe);
        }
    }

    private void setEnableCopyFromSection(boolean enable) {
        // setSelection will be redundant if called from a listener
        enableCopyFromExeButton.setSelection(enable);
        copyFromExeSelector.setEnabled(enable);
        toLabel.setEnabled(enable);
    }

    protected void createCopyFromExeGroup(Composite parent, int colSpan) {
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout mainLayout = new GridLayout();
        mainLayout.numColumns = 1;
        mainLayout.marginHeight = 0;
        mainLayout.marginWidth = 0;
        mainComp.setLayout(mainLayout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = colSpan;
        mainComp.setLayoutData(gd);

        enableCopyFromExeButton = createCheckButton(mainComp, ProxyLaunchMessages.copy_cpp_executable);
        copyFromExeSelector = new ResourceSelectorWidget(mainComp,
                ResourceSelectorWidget.ResourceType.FILE,
                2, ProxyLaunchMessages.executable_origin, null);
        toLabel = new Label(mainComp, SWT.NONE);
        toLabel.setText(ProxyLaunchMessages.to);

        // The "copy from" check box is initially off, the selector and "To:" label
        // are disabled.
        copyFromExeSelector.setEnabled(false);
        enableCopyFromExeButton.setSelection(false);

        toLabel.setEnabled(false);

        enableCopyFromExeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean copyEnabled = enableCopyFromExeButton.getSelection();
                setEnableCopyFromSection(copyEnabled);
                updateLaunchConfigurationDialog();
            }
        });

        copyFromExeText = copyFromExeSelector.getURIText();
        copyFromExeText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
    }


    protected void createExeFileGroup(Composite parent, int colSpan) {
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout mainLayout = new GridLayout();
        mainLayout.numColumns = 1;
        mainLayout.marginHeight = 0;
        mainLayout.marginWidth = 0;
        mainComp.setLayout(mainLayout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = colSpan;
        mainComp.setLayoutData(gd);
        exeSelector = new ResourceSelectorWidget(mainComp,
                ResourceSelectorWidget.ResourceType.FILE,
                2, "C/C++ executable", null); //$NON-NLS-1$
        fProgText = exeSelector.getURIText();
        fProgText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    protected void updateWorkingDirFromConfig(ILaunchConfiguration config) {
        if (workingDirText != null) {
            String projectDir = EMPTY_STRING;
            try {
                projectDir = config.getAttribute(ATTR_REMOTE_WORKING_DIRECTORY_NAME, EMPTY_STRING);
            } catch (CoreException ce) {
                ProfileLaunchPlugin.log(ce);
            }

            if (projectDir.equals(EMPTY_STRING)){
                if(this.fProjText != null){
                    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                    IProject project = root.getProject(this.fProjText.getText());
                    try {
                        projectDir = RemoteProxyManager.getInstance().getRemoteProjectLocation(project);
                    } catch (CoreException e) {
                        setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.error_accessing_working_directory);
                    }
                }
            }
            workingDirText.setText(projectDir);
        }
    }

    protected void createWorkingDirGroup(Composite parent, int colSpan) {
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout mainLayout = new GridLayout();
        mainLayout.numColumns = 1;
        mainLayout.marginHeight = 0;
        mainLayout.marginWidth = 0;
        mainComp.setLayout(mainLayout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = colSpan;
        mainComp.setLayoutData(gd);
        ResourceSelectorWidget workingDirSelector = new ResourceSelectorWidget(mainComp,
                ResourceSelectorWidget.ResourceType.DIRECTORY,
                2, "Working directory", null); //$NON-NLS-1$
        workingDirText = workingDirSelector.getURIText();
        workingDirText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    private boolean checkCopyFromExe(IProject project) {
        if (!enableCopyFromExeButton.getSelection()) {
            setErrorMessage(null);
            return true;
        }

        String name = copyFromExeText.getText().trim();
        if (name.length() == 0) {
            setErrorMessage(ProxyLaunchMessages.copy_from_exe_is_not_specified);
            return false;
        }
        // Avoid constantly checking the binary if nothing relevant has
        // changed (binary or project name). See bug 277663.
        if (name.equals(fPreviouslyCheckedCopyFromExe)) {
            if (fPreviouslyCheckedCopyFromExeErrorMsg != null) {
                setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg);
            }
            return fPreviouslyCheckedCopyFromExeIsValid;
        }
        fPreviouslyCheckedCopyFromExe = name;
        fPreviouslyCheckedCopyFromExeIsValid = true; // we'll flip this below if
        // not true
        fPreviouslyCheckedCopyFromExeErrorMsg = null; // we'll set this below if
        // there's an error
        IPath exePath;
        URI exeURI = null;
        boolean passed = false;

        try {
            exeURI = new URI(name);
            String exePathStr = exeURI.getPath();
            if (exePathStr == null) {
                setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.uri_of_copy_from_exe_is_invalid);
                fPreviouslyCheckedCopyFromExeIsValid = false;
                return false;
            }
            exePath = Path.fromOSString(exeURI.getPath());
            if (!exePath.isAbsolute() && exeURI != null && !exeURI.isAbsolute()) {
                URI projectURI = project.getLocationURI();
                exeURI = new URI(projectURI.getScheme(),
                        projectURI.getAuthority(), projectURI.getRawPath() + '/'
                        + exePath.toString(), EMPTY_STRING);
            }
            if (exeURI != null) {
                passed = true;
            }
        } catch (URISyntaxException e) {
            setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.uri_of_copy_from_exe_is_invalid);
            fPreviouslyCheckedCopyFromExeIsValid = false;
            return false;
        }

        if (!passed) {
            setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.copy_from_exe_does_not_exist);
            fPreviouslyCheckedCopyFromExeIsValid = false;
            return false;
        }

        passed = false;
        try {
            IRemoteFileProxy exeFileProxy;
            exeFileProxy = RemoteProxyManager.getInstance().getFileProxy(exeURI);
            if (exeFileProxy != null) {
                String exeFilePath = exeURI.getPath();
                IFileStore exeFS = exeFileProxy
                        .getResource(exeFilePath);
                if (exeFS != null) {
                    IFileInfo exeFI = exeFS.fetchInfo();
                    if (exeFI != null) {
                        if (exeFI.exists()) {
                            if (exeFI.getAttribute(EFS.ATTRIBUTE_EXECUTABLE) &&
                                    !exeFI.isDirectory()) {
                                passed = true;
                            } else {
                                setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.copy_from_exe_does_not_have_execution_rights);
                            }
                        } else {
                            setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.copy_from_exe_does_not_exist);
                        }
                    } else {
                        setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.error_accessing_copy_from_exe);
                    }
                } else {
                    setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.error_accessing_copy_from_exe);
                }
            } else {
                setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.scheme_error_in_copy_from_exe);
            }
        } catch (CoreException e) {
            setErrorMessage(fPreviouslyCheckedCopyFromExeErrorMsg = ProxyLaunchMessages.connection_of_copy_from_exe_cannot_be_opened);
        }
        if (!passed) {
            fPreviouslyCheckedCopyFromExeIsValid = false;
            return false;
        }
        setErrorMessage(null);
        return true;
    }


    private boolean checkProgram(IProject project) {
        String name = fProgText.getText().trim();
        if (name.length() == 0) {
            setErrorMessage(ProxyLaunchMessages.executable_is_not_specified);
            return false;
        }
        // Avoid constantly checking the binary if nothing relevant has
        // changed (binary or project name). See bug 277663.
        if (name.equals(fPreviouslyCheckedProgram)) {
            if (fPreviouslyCheckedProgramErrorMsg != null) {
                setErrorMessage(fPreviouslyCheckedProgramErrorMsg);
            }
            return fPreviouslyCheckedProgramIsValid;
        }
        fPreviouslyCheckedProgram = name;
        fPreviouslyCheckedProgramIsValid = true; // we'll flip this below if
        // not true
        fPreviouslyCheckedProgramErrorMsg = null; // we'll set this below if
        // there's an error
        IPath exePath;
        URI exeURI = null;
        boolean passed = false;

        try {
            exeURI = new URI(name);
            String exePathStr = exeURI.getPath();
            if (exePathStr == null) {
                setErrorMessage(fPreviouslyCheckedProgramErrorMsg = ProxyLaunchMessages.uri_of_executable_is_invalid);
                fPreviouslyCheckedProgramIsValid = false;
                return false;
            }

            exePath = Path.fromOSString(exeURI.getPath());
            if (!exePath.isAbsolute() && exeURI != null && !exeURI.isAbsolute()) {
                URI projectURI = project.getLocationURI();
                exeURI = new URI(projectURI.getScheme(),
                        projectURI.getAuthority(), projectURI.getRawPath() + '/'
                        + exePath.toString(), EMPTY_STRING);
            }
            if (exeURI != null) {
                passed = true;
            }
        } catch (URISyntaxException e) {
            setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.uri_of_executable_is_invalid);
            fPreviouslyCheckedProgramIsValid = false;
            return false;
        }

        if (!passed) {
            setErrorMessage(fPreviouslyCheckedProgramErrorMsg = LaunchMessages.CMainTab_Program_does_not_exist);
            fPreviouslyCheckedProgramIsValid = false;
            return false;
        }

        passed = false;
        try {
            IRemoteFileProxy exeFileProxy;
            exeFileProxy = RemoteProxyManager.getInstance().getFileProxy(exeURI);
            if (exeFileProxy != null) {
                String exeFilePath = exeURI.getPath();
                IFileStore exeFS = exeFileProxy
                        .getResource(exeFilePath);
                if (exeFS != null) {
                    IFileInfo exeFI = exeFS.fetchInfo();
                    if (exeFI != null) {
                        if (dontCheckProgram || enableCopyFromExeButton.getSelection()) {
                            // The program may not exist yet if we are copying it.
                            passed = true;
                        } else {
                            if (exeFI.exists()) {
                                if (exeFI.getAttribute(EFS.ATTRIBUTE_EXECUTABLE) &&
                                        !exeFI.isDirectory()) {
                                    passed = true;
                                } else {
                                    setErrorMessage(fPreviouslyCheckedProgramErrorMsg = ProxyLaunchMessages.executable_does_not_have_execution_rights);
                                }
                            } else {
                                setErrorMessage(fPreviouslyCheckedProgramErrorMsg = ProxyLaunchMessages.executable_does_not_exist);
                            }
                        }
                    } else {
                        setErrorMessage(fPreviouslyCheckedProgramErrorMsg = ProxyLaunchMessages.error_accessing_executable);
                    }
                } else {
                    setErrorMessage(fPreviouslyCheckedProgramErrorMsg = ProxyLaunchMessages.error_accessing_executable);
                }
            } else {
                setErrorMessage(fPreviouslyCheckedProgramErrorMsg = ProxyLaunchMessages.scheme_error_in_executable);
            }
        } catch (CoreException e) {
            setErrorMessage(fPreviouslyCheckedProgramErrorMsg = ProxyLaunchMessages.connection_of_executable_cannot_be_opened);
        }
        if (!passed) {
            fPreviouslyCheckedProgramIsValid = false;
            return false;
        }
        setErrorMessage(null);
        return true;
    }

    private boolean checkWorkingDir(IProject project) {
        String name = workingDirText.getText().trim();
        if (name.length() == 0) {
            return true;  // an empty working directory means, "use the default"
        }
        // Avoid constantly checking the working dir if nothing relevant has
        // changed (project).
        if (name.equals(fPreviouslyCheckedWorkingDir)) {
            if (fPreviouslyCheckedWorkingDirErrorMsg != null) {
                setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg);
            }
            return fPreviouslyCheckedWorkingDirIsValid;
        }
        fPreviouslyCheckedWorkingDir = name;
        fPreviouslyCheckedWorkingDirIsValid = true; // we'll flip this below if
        // not true
        fPreviouslyCheckedWorkingDirErrorMsg = null; // we'll set this below if
        // there's an error
        IPath wdPath;
        URI wdURI = null;
        boolean passed = false;

        try {
            wdURI = new URI(name);
            String wdPathStr = wdURI.getPath();
            if (wdPathStr == null) {
                setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.uri_of_working_directory_is_invalid);
                fPreviouslyCheckedWorkingDirIsValid = false;
                return false;
            }

            wdPath = Path.fromOSString(wdURI.getPath());
            if (!wdPath.isAbsolute() && wdURI != null && !wdURI.isAbsolute()) {
                URI projectURI = project.getLocationURI();
                wdURI = new URI(projectURI.getScheme(),
                        projectURI.getAuthority(), projectURI.getRawPath() + '/'
                        + wdPath.toString(), EMPTY_STRING);
            }
            if (wdURI != null) {
                passed = true;
            }
        } catch (URISyntaxException e) {
            setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.uri_of_working_directory_is_invalid);
            fPreviouslyCheckedWorkingDirIsValid = false;
            return false;
        }

        if (!passed) {
            setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.working_directory_does_not_exist);
            fPreviouslyCheckedWorkingDirIsValid = false;
            return false;
        }

        passed = false;
        IRemoteFileProxy wdFileProxy;
        try {
            wdFileProxy = RemoteProxyManager.getInstance().getFileProxy(wdURI);
            if (wdFileProxy != null) {
                IFileStore wdFS = wdFileProxy
                        .getResource(wdURI.getPath());
                if (wdFS != null) {
                    IFileInfo wdFI = wdFS.fetchInfo();
                    if (wdFI != null) {
                        if (wdFI.exists()) {
                            if (wdFI.isDirectory()) {
                                passed = true;
                            } else {
                                setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.working_directory_is_not_a_directory);
                            }
                        } else {
                            setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.working_directory_does_not_exist);
                        }
                    } else {
                        setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.error_accessing_working_directory);
                    }
                } else {
                    setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.error_accessing_working_directory);
                }
            } else {
                setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.scheme_error_in_working_directory);
            }
        } catch (CoreException e) {
            setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.connection_of_working_directory_cannot_be_opened);
        }

        if (!passed) {
            fPreviouslyCheckedWorkingDirIsValid = false;
            return false;
        }
        setErrorMessage(null);
        return true;
    }

    /**
     * Compare two strings as equal or not, but allow for one or both of
     * the strings to be null.  If both are null, they are treated as equal.
     * If one is null and the other isn't, they are considered unequal.  If
     * they are both non-null, the result is the same as the
     * String.equals() method.
     * @param s1 First string to compare.
     * @param s2 Second string to compare.
     * @return Return true if equal, false otherwise.
     */
    private static boolean equal(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        if (s1 == null || s2 == null)
            return false;
        return s1.equals(s2);
    }

    /**
     * Check to see if the Executable's location has the same scheme and host name as
     * the working directory.  If not, this is an error.
     * @return true if they are compatible, and false if not compatible.
     */
    private boolean checkCompatibility() {
        String wdName = workingDirText.getText().trim();
        String progName = fProgText.getText().trim();
        URI wdURI;
        URI progURI;

        try {
            wdURI = new URI(wdName);
            progURI = new URI(progName);
        } catch (URISyntaxException e) {
            // this will have been dealt with by previous checks of the Program and Working
            // directory
            System.err.println(ProxyLaunchMessages.uri_syntax_error);
            return false;
        }
        String wdScheme = wdURI.getScheme();
        String progScheme = progURI.getScheme();
        if (wdScheme == null && progScheme == null) {
            // local filesystem. No further tests are needed.
            setErrorMessage(null);
            return true;
        }
        if (!equal(wdScheme, progScheme)) {
            setErrorMessage(ProxyLaunchMessages.scheme_of_working_directory_and_program_do_not_match);
            return false;
        }
        String wdAuth = wdURI.getAuthority();
        String progAuth = progURI.getAuthority();
        if (!equal(wdAuth, progAuth)) {
            setErrorMessage(ProxyLaunchMessages.connection_of_working_directory_and_program_do_not_match);
            return false;
        }
        String wdQuery = wdURI.getQuery();
        String progQuery = progURI.getQuery();
        if (!equal(wdQuery, progQuery)) {
            setErrorMessage(ProxyLaunchMessages.connection_of_working_directory_and_program_do_not_match);
            return false;
        }
        setErrorMessage(null);
        return true;
    }

    @Override
    public boolean isValid(ILaunchConfiguration config) {
        IProject project;

        this.setErrorMessage(null);
        setMessage(null);

        String name = fProjText.getText().trim();
        if (name.length() == 0) {
            setErrorMessage(LaunchMessages.CMainTab_Project_not_specified);
            return false;
        }
        project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        if (!project.exists()) {
            setErrorMessage(LaunchMessages.Launch_common_Project_does_not_exist);
            return false;
        }
        if (!project.isOpen()) {
            setErrorMessage(LaunchMessages.CMainTab_Project_must_be_opened);
            return false;
        }

        if (!checkCopyFromExe(project))
            return false;

        if (!checkProgram(project))
            return false;

        if (!checkWorkingDir(project))
            return false;

        if (!checkCompatibility())
            return false;

        if (fCoreText != null) {
            String coreName = fCoreText.getText().trim();
            // We accept an empty string. This should trigger a prompt to the
            // user
            // This allows to re-use the launch, with a different core file.
            if (!coreName.equals(EMPTY_STRING)) {
                if (coreName.equals(".") || coreName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
                    setErrorMessage(LaunchMessages.CMainTab_Core_does_not_exist);
                    return false;
                }
                IPath corePath = new Path(coreName);
                if (!corePath.toFile().exists()) {
                    setErrorMessage(LaunchMessages.CMainTab_Core_does_not_exist);
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
     * debug.core.ILaunchConfigurationWorkingCopy)
     */
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        // We set empty attributes for project & program so that when one config
        // is
        // compared to another, the existence of empty attributes doesn't cause
        // an
        // incorrect result (the performApply() method can result in empty
        // values
        // for these attributes being set on a config if there is nothing in the
        // corresponding text boxes)
        // plus getContext will use this to base context from if set.
        config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                EMPTY_STRING);
        config.setAttribute(
                ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID,
                EMPTY_STRING);
        config.setAttribute(
                ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
                EMPTY_STRING);

        // Set the auto choose build configuration to true for new
        // configurations.
        // Existing configurations created before this setting was introduced
        // will have this disabled.
        config.setAttribute(
                ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_AUTO,
                true);

        ICElement cElement = null;
        cElement = getContext(config, getPlatform(config));
        if (cElement != null) {
            initializeCProject(cElement, config);
            initializeProgramName(cElement, config);
        } else {
            // don't want to remember the interim value from before
            config.setMappedResources(null);
        }
        if (wantsTerminalOption()) {
            config.setAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
                    ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
        }
    }

    /**
     * Set the program name attributes on the working copy based on the
     * ICElement.
     * @param cElement
     * @param config
     */
    protected void initializeProgramName(ICElement cElement,
            ILaunchConfigurationWorkingCopy config) {
        boolean renamed = false;

        if (!(cElement instanceof IBinary)) {
            cElement = cElement.getCProject();
        }

        if (cElement instanceof ICProject) {
            IProject project = cElement.getCProject().getProject();
            String name = project.getName();
            ICProjectDescription projDes = CCorePlugin.getDefault()
                    .getProjectDescription(project);
            if (projDes != null) {
                String buildConfigName = projDes.getActiveConfiguration()
                        .getName();
                // Bug 234951
                name = NLS.bind(LaunchMessages.CMainTab_Configuration_name,
                        name, buildConfigName);
            }
            name = getLaunchConfigurationDialog().generateName(name);
            config.rename(name);
            renamed = true;
        }

        IBinary binary = null;
        if (cElement instanceof ICProject) {
            IBinary[] bins = getBinaryFiles((ICProject) cElement);
            if (bins != null && bins.length == 1) {
                binary = bins[0];
            }
        } else if (cElement instanceof IBinary) {
            binary = (IBinary) cElement;
        }

        String projectDir = EMPTY_STRING;
        IProject project = null;
        try {
            project = ConfigUtils.getProject(ConfigUtils.getProjectName(config));
        } catch (CoreException e) {
            setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.error_accessing_working_directory);
        }
        if(project != null){
            try {
                projectDir = RemoteProxyManager.getInstance().getRemoteProjectLocation(project);
            } catch (CoreException e) {
                setErrorMessage(fPreviouslyCheckedWorkingDirErrorMsg = ProxyLaunchMessages.error_accessing_working_directory);
            }
        }

        String path = EMPTY_STRING;
        if (binary != null) {
            path = binary.getResource().getProjectRelativePath().toOSString();
            if (!renamed) {
                String name = binary.getElementName();
                int index = name.lastIndexOf('.');
                if (index > 0) {
                    name = name.substring(0, index);
                }
                name = getLaunchConfigurationDialog().generateName(name);
                config.rename(name);
                renamed = true;
            }
        }
        config.setAttribute(
                ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, projectDir + IPath.SEPARATOR + path);

        if (!renamed) {
            String name = getLaunchConfigurationDialog().generateName(
                    cElement.getCProject().getElementName());
            config.rename(name);
        }
    }

    @Override
    public String getId() {
        return TAB_ID;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
     */
    @Override
    public String getName() {
        return LaunchMessages.CMainTab_Main;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
     */
    @Override
    public Image getImage() {
        return LaunchImages.get(LaunchImages.IMG_VIEW_MAIN_TAB);
    }
}
