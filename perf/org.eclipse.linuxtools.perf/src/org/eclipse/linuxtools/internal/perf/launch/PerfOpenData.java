/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.launch;

import java.net.URI;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class PerfOpenData extends ProfileLaunchShortcut implements
		IEditorLauncher {

    @Override
    public void open(IPath file) {
        // get project name of where the file resides.
        String projectName = null;
        IFile location = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(file);
        // If unable to get location from workspace, try getting from current file selection
        if(location == null){
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                ISelection selection = page.getSelection();
                if(selection instanceof ITreeSelection){
                        Object element = ((ITreeSelection)selection).getFirstElement();
                        if(element instanceof IFile){
                                IProject project = ((IFile)element).getProject();
                                projectName = project.getName();
                                URI fileURI = ((IFile)element).getLocationURI();
                                ILaunchConfiguration config = createDefaultConfiguration(projectName);
                                PerfCore.Report(config, null, null, null, fileURI.getPath().toString(), null);
                                PerfCore.RefreshView(fileURI.toString());
                        }
                }
        } else {
                projectName = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(file).getProject().getName();
                ILaunchConfiguration config = createDefaultConfiguration(projectName);
                PerfCore.Report(config, null, null, null, file.toOSString(), null);
                PerfCore.RefreshView(file.toOSString());
        }
    }

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				PerfPlugin.LAUNCHCONF_ID);
	}

	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) {
		wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
		wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
	}

	/**
	 * Create an ILaunchConfiguration instance given the project's name.
	 *
	 * @param projectName
	 * @return ILaunchConfiguration based on String projectName
	 */
	protected ILaunchConfiguration createDefaultConfiguration(String projectName) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(
					null,
					getLaunchManager().generateLaunchConfigurationName(
							projectName));
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
			config = wc;

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}
}