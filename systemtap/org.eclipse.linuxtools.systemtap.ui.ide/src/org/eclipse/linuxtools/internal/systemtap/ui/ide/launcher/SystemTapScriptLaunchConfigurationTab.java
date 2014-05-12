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

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SystemTapScriptLaunchConfigurationTab extends
        AbstractLaunchConfigurationTab {

    static final String SCRIPT_PATH_ATTR = "ScriptPath"; //$NON-NLS-1$
    static final String CURRENT_USER_ATTR = "executeAsCurrentUser"; //$NON-NLS-1$
    static final String USER_NAME_ATTR = "userName"; //$NON-NLS-1$
    static final String USER_PASS_ATTR = "userPassword"; //$NON-NLS-1$
    static final String LOCAL_HOST_ATTR = "executeOnLocalHost"; //$NON-NLS-1$
    static final String HOST_NAME_ATTR = "hostName"; //$NON-NLS-1$
    static final String PORT_ATTR = "port"; //$NON-NLS-1$
    static final String USE_DEFAULT_PORT_ATTR = "useDefaultPort"; //$NON-NLS-1$

    private Text scriptPathText;
    private Button currentUserCheckButton;
    private Text userNameText;
    private Label userNameLabel;
    private Text userPasswordText;
    private Label userPasswordLabel;
    private Button localHostCheckButton;
    private Group hostSettingsGroup;
    private Text hostNameText;
    private Label hostNameLabel;
    private Text portText;
    private Label portLabel;
    private Button portCheckButton;
    private FileDialog fileDialog;

    SelectionListener checkListener = new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            update();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            update();
        }

        private void update() {
            updateLaunchConfigurationDialog();
        }

    };

    /**
     * @return The path of the chosen script the Run Configuration will be applied to,
     * or <code>null</code> if no file exists at the given path.
     */
    IPath getScriptPath() {
        IPath scriptPath = new Path(scriptPathText.getText());
        return scriptPath.toFile().exists() ? scriptPath : null;
    }

    @Override
    public void createControl(Composite parent) {

        fileDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
        fileDialog.setText(Messages.SystemTapScriptLaunchConfigurationTab_selectScript);
        fileDialog.setFilterPath(Platform.getLocation().toOSString());

        GridLayout layout = new GridLayout();
        Composite top = new Composite(parent, SWT.NONE);
        setControl(top);
        top.setLayout(layout);
        top.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true));

        // Script path
        Group scriptSettingsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
        scriptSettingsGroup.setText(Messages.SystemTapScriptLaunchConfigurationTab_script);
        scriptSettingsGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false));
        layout = new GridLayout();
        layout.numColumns = 2;
        scriptSettingsGroup.setLayout(layout);
        scriptPathText = new Text(scriptSettingsGroup,  SWT.SINGLE | SWT.BORDER);
        scriptPathText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scriptPathText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        Button selectScriptButon = new Button(scriptSettingsGroup, 0);
        GridData gridData = new GridData();
        gridData.widthHint = 110;
        selectScriptButon.setLayoutData(gridData);
        selectScriptButon.setText(Messages.SystemTapScriptLaunchConfigurationTab_browse);
        selectScriptButon.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String path = fileDialog.open();
                if (path != null) {
                    scriptPathText.setText(path);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // User Settings
        Group userSettingsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
        layout = new GridLayout();
        userSettingsGroup.setLayout(layout);
        layout.numColumns = 2;
        userSettingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        currentUserCheckButton = new Button(userSettingsGroup, SWT.CHECK);
        currentUserCheckButton.setText(Messages.SystemTapScriptLaunchConfigurationTab_currentUser);
        currentUserCheckButton.setSelection(true);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        currentUserCheckButton.setLayoutData(gridData);
        currentUserCheckButton.addSelectionListener(checkListener);

        userNameLabel = new Label(userSettingsGroup, SWT.NONE);
        userNameLabel.setText(Messages.SystemTapScriptLaunchConfigurationTab_username);
        userNameText = new Text(userSettingsGroup, SWT.SINGLE | SWT.BORDER);
        userNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        userPasswordLabel = new Label(userSettingsGroup, SWT.NONE);
        userPasswordLabel.setText(Messages.SystemTapScriptLaunchConfigurationTab_password);
        userPasswordText = new Text(userSettingsGroup, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
        userPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        userSettingsGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false));
        userSettingsGroup.setText(Messages.SystemTapScriptLaunchConfigurationTab_user);

        userNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        userPasswordText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        // Host settings
        hostSettingsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
        hostSettingsGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false));
        hostSettingsGroup.setText(Messages.SystemTapScriptLaunchConfigurationTab_host);
        layout = new GridLayout();
        hostSettingsGroup.setLayout(layout);
        layout.numColumns = 2;

        localHostCheckButton = new Button(hostSettingsGroup, SWT.CHECK);
        localHostCheckButton.setText(Messages.SystemTapScriptLaunchConfigurationTab_runLocally);
        gridData = new GridData();
        gridData.horizontalSpan = 2;

        hostNameLabel = new Label(hostSettingsGroup, SWT.NONE);
        hostNameLabel.setText(Messages.SystemTapScriptLaunchConfigurationTab_hostname);
        hostNameText = new Text(hostSettingsGroup, SWT.SINGLE | SWT.BORDER);
        hostNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        localHostCheckButton.setLayoutData(gridData);
        localHostCheckButton.addSelectionListener(checkListener);
        hostNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        portCheckButton = new Button(hostSettingsGroup, SWT.CHECK);
        portCheckButton.setText(Messages.SystemTapScriptLaunchConfigurationTab_useDefaultPort);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        portCheckButton.setLayoutData(gridData);
        portCheckButton.addSelectionListener(checkListener);

        portLabel = new Label(hostSettingsGroup, SWT.NONE);
        portLabel.setText(Messages.SystemTapScriptLaunchConfigurationTab_port);
        portText = new Text(hostSettingsGroup, SWT.SINGLE | SWT.BORDER);
        portText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        portText.setTextLimit(5);
        portText.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                if (e.keyCode == SWT.BS) {
                    return;
                }
                for (int i = 0, n = e.text.length(); i < n; i++) {
                    if (!Character.isDigit(e.text.charAt(i))) {
                        e.doit = false;
                        return;
                    }
                }
            }
        });
        portText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void setUserGroupEnablement(boolean enable) {
        userNameText.setEnabled(enable);
        userNameLabel.setEnabled(enable);
        userPasswordText.setEnabled(enable);
        userPasswordLabel.setEnabled(enable);

        hostSettingsGroup.setEnabled(enable);
        localHostCheckButton.setEnabled(enable);
    }

    private void setHostGroupEnablement(boolean enable) {
        hostNameText.setEnabled(enable);
        hostNameLabel.setEnabled(enable);
        portCheckButton.setEnabled(enable);

        boolean portEnabled = enable && !portCheckButton.getSelection();
        portText.setEnabled(portEnabled);
        portLabel.setEnabled(portEnabled);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(SCRIPT_PATH_ATTR, getSelectedScriptPath());
        configuration.setAttribute(CURRENT_USER_ATTR, true);
        configuration.setAttribute(USER_NAME_ATTR, ""); //$NON-NLS-1$
        configuration.setAttribute(USER_PASS_ATTR, ""); //$NON-NLS-1$
        configuration.setAttribute(LOCAL_HOST_ATTR, true);
        configuration.setAttribute(HOST_NAME_ATTR, ""); //$NON-NLS-1$
        configuration.setAttribute(PORT_ATTR,
                SystemTapScriptLaunchConfigurationDelegate.DEFAULT_PORT);
        configuration.setAttribute(USE_DEFAULT_PORT_ATTR, true);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            this.scriptPathText.setText(configuration.getAttribute(SCRIPT_PATH_ATTR, "")); //$NON-NLS-1$
            this.currentUserCheckButton.setSelection(configuration.getAttribute(CURRENT_USER_ATTR, true));
            this.userNameText.setText(configuration.getAttribute(USER_NAME_ATTR, "")); //$NON-NLS-1$
            this.userPasswordText.setText(configuration.getAttribute(USER_PASS_ATTR, "")); //$NON-NLS-1$
            this.localHostCheckButton.setSelection(configuration.getAttribute(LOCAL_HOST_ATTR, true));
            this.hostNameText.setText(configuration.getAttribute(HOST_NAME_ATTR, "")); //$NON-NLS-1$
            this.portText.setText(Integer.toString(configuration.getAttribute(PORT_ATTR,
                    SystemTapScriptLaunchConfigurationDelegate.DEFAULT_PORT)));
            this.portCheckButton.setSelection(configuration.getAttribute(USE_DEFAULT_PORT_ATTR, true));
        } catch (CoreException e) {
            ExceptionErrorDialog.openError(Messages.SystemTapScriptLaunchConfigurationTab_errorInitializingTab, e);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(SCRIPT_PATH_ATTR, this.scriptPathText.getText());
        configuration.setAttribute(CURRENT_USER_ATTR, this.currentUserCheckButton.getSelection());
        configuration.setAttribute(USER_NAME_ATTR, this.userNameText.getText());
        configuration.setAttribute(USER_PASS_ATTR, this.userPasswordText.getText());
        configuration.setAttribute(LOCAL_HOST_ATTR, this.localHostCheckButton.getSelection());
        configuration.setAttribute(HOST_NAME_ATTR, this.hostNameText.getText());

        configuration.setAttribute(USE_DEFAULT_PORT_ATTR, portCheckButton.getSelection());
        String portString = this.portText.getText();
        configuration.setAttribute(PORT_ATTR, !portString.isEmpty() ? Integer.valueOf(portString) : 0);

        boolean enable = !currentUserCheckButton.getSelection();
        setUserGroupEnablement(enable);

        enable &= !localHostCheckButton.getSelection();
        setHostGroupEnablement(enable);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        setErrorMessage(null);

        IPath scriptPath = getScriptPath();
        if (scriptPath == null) {
            setErrorMessage(MessageFormat.format(Messages.SystemTapScriptLaunchConfigurationTab_fileNotFound, scriptPathText.getText()));
            return false;
        }
        String extension = scriptPath.getFileExtension();
        if (extension == null || !extension.equals("stp")) { //$NON-NLS-1$
            setErrorMessage(Messages.SystemTapScriptLaunchConfigurationTab_fileNotStp);
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return Messages.SystemTapScriptLaunchConfigurationTab_general;
    }

    private String getSelectedScriptPath() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        String pathString = ""; //$NON-NLS-1$

        if (window != null)
        {
            ISelection selection = window.getSelectionService().getSelection();

            // Figure out the selected systemtap script
            if (selection instanceof TreeSelection) {
                Object selectedElement = ((TreeSelection)selection).getFirstElement();
                if (selectedElement instanceof IFile)
                {
                    IPath path = ((IFile)selectedElement).getLocation();
                    pathString = path.toOSString();
                }
            }

            // If it is a text selection use the path from the active editor.
            if (selection instanceof TextSelection) {
                IEditorPart ed = window.getActivePage().getActiveEditor();
                if(ed.getEditorInput() instanceof PathEditorInput) {
                    pathString = ((PathEditorInput)ed.getEditorInput()).getPath().toString();
                } else {
                    pathString = ResourceUtil.getFile(ed.getEditorInput()).getLocation().toString();
                }
            }
        }

        if (pathString.endsWith(".stp")) { //$NON-NLS-1$
            return pathString;
        }

        return ""; //$NON-NLS-1$
    }

    @Override
    public Image getImage() {
        return AbstractUIPlugin.imageDescriptorFromPlugin(IDEPlugin.PLUGIN_ID,
                "icons/main_tab.gif").createImage(); //$NON-NLS-1$
    }
}
