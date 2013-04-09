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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;

public class SystemTapScriptLaunchShortcut extends ProfileLaunchShortcut {

	@Override
	public void launch(IEditorPart editor, String mode) {
		String path = ""; //$NON-NLS-1$
		if(editor.getEditorInput() instanceof PathEditorInput){
			path = ((PathEditorInput)editor.getEditorInput()).getPath().toString();
		} else {
			path = ResourceUtil.getFile(editor.getEditorInput()).getLocation().toString();
		}
		this.searchAndLaunch(path);
	}

	@Override
	public void launch(ISelection selection, String mode) {
		IPath path = ((IFile)((TreeSelection)selection).getFirstElement()).getLocation();
		this.searchAndLaunch(path.toOSString());
	}

	private void searchAndLaunch(String path){
		ILaunchConfiguration configuration = findLaunchConfiguration(path);
		if (configuration == null){
			return;
		}
		try {
			configuration.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptLaunchShortcut_couldNotLaunchScript, e);
		}

	}

	protected ILaunchConfiguration findLaunchConfiguration(String scriptPath) {
		ILaunchConfiguration configuration = null;
		ArrayList<ILaunchConfiguration> candidateConfiguraions = new ArrayList<ILaunchConfiguration>();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager()
					.getLaunchConfigurations(getLaunchConfigType());

			for (ILaunchConfiguration config: configs){
				if (config.getAttribute(SystemTapScriptLaunchConfigurationTab.SCRIPT_PATH_ATTR, "").equals(scriptPath)){ //$NON-NLS-1$
					candidateConfiguraions.add(config);
				}
			}

			int candidateCount = candidateConfiguraions.size();
			if (candidateCount == 0) {
				ILaunchConfigurationType type = getLaunchConfigType();
				configuration = type.newInstance(null, "Default"); //$NON-NLS-1$
				ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
				wc.setAttribute(SystemTapScriptLaunchConfigurationTab.SCRIPT_PATH_ATTR, scriptPath);
				configuration = wc.doSave();
			} else if (candidateCount == 1) {
				configuration = candidateConfiguraions.get(0);
			} else {
				configuration = chooseConfiguration(candidateConfiguraions,
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



}
