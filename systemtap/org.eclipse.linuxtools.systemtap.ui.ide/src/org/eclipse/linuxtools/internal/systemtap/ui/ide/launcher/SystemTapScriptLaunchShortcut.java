/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;

public class SystemTapScriptLaunchShortcut extends ProfileLaunchShortcut implements ILaunchShortcut2 {

    @Override
    public void launch(IEditorPart editor, String mode) {
        String path;
        String project = null;
        if(editor.getEditorInput() instanceof PathEditorInput){
            path = ((PathEditorInput)editor.getEditorInput()).getPath().toString();
        } else {
            IFile file = ResourceUtil.getFile(editor.getEditorInput());
            path = file.getLocation().toString();
            project = file.getProject().getName();
        }
        this.searchAndLaunch(path, project);
    }

    @Override
    public void launch(ISelection selection, String mode) {
        IFile file = (IFile)((TreeSelection)selection).getFirstElement();
        String path = file.getLocation().toOSString();
        String project = file.getProject().getName();
        this.searchAndLaunch(path, project);
    }

    private void searchAndLaunch(String path, String project){
        ILaunchConfiguration configuration = findLaunchConfiguration(path, project);
        if (configuration == null){
            return;
        }
        try {
            configuration.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
        } catch (CoreException e) {
            ExceptionErrorDialog.openError(Messages.SystemTapScriptLaunchShortcut_couldNotLaunchScript, e);
        }

    }

    private ILaunchConfiguration findLaunchConfiguration(String scriptPath, String scriptProject) {
        ILaunchConfiguration configuration = null;
        ArrayList<ILaunchConfiguration> candidateConfigurations = new ArrayList<>();
        try {
            ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfiguration[] configs = launchManager
                    .getLaunchConfigurations(getLaunchConfigType());

            for (ILaunchConfiguration config: configs){
                if (config.getAttribute(SystemTapScriptLaunchConfigurationTab.SCRIPT_PATH_ATTR, "").equals(scriptPath)){ //$NON-NLS-1$
                    candidateConfigurations.add(config);
                }
            }

            int candidateCount = candidateConfigurations.size();
            if (candidateCount == 0) {
                LinkedList<String> configNames = new LinkedList<>();
                configs = launchManager.getLaunchConfigurations();
                for (ILaunchConfiguration config : configs) {
                    configNames.add(config.getName());
                }
                String configName = (scriptProject == null ? "" : scriptProject + " ") //$NON-NLS-1$ //$NON-NLS-2$
                        + Path.fromOSString(scriptPath).lastSegment();
                String wcName = configName;
                int conflict_index, conflict_count = 0;
                while ((conflict_index = configNames.indexOf(wcName)) != -1) {
                    wcName = configName.concat(String.format(" (%d)", ++conflict_count)); //$NON-NLS-1$
                    configNames.remove(conflict_index);
                }

                ILaunchConfigurationType type = getLaunchConfigType();
                ILaunchConfigurationWorkingCopy wc = type.newInstance(null, wcName);
                wc.setAttribute(SystemTapScriptLaunchConfigurationTab.SCRIPT_PATH_ATTR, scriptPath);
                configuration = wc.doSave();
            } else if (candidateCount == 1) {
                configuration = candidateConfigurations.get(0);
            } else {
                configuration = chooseConfiguration(candidateConfigurations,
                        ILaunchManager.RUN_MODE);
            }
        } catch (CoreException e) {
            ExceptionErrorDialog.openError(Messages.SystemTapScriptLaunchShortcut_couldNotFindConfig, e);
        }

        return configuration;
    }

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(
                SystemTapScriptLaunchConfigurationDelegate.CONFIGURATION_TYPE);
    }

    @Override
    protected void setDefaultProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
        return null;
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
        return null;
    }

    @Override
    public IResource getLaunchableResource(ISelection selection) {
        return null;
    }

    @Override
    public IResource getLaunchableResource(IEditorPart editorpart) {
        return null;
    }



}
