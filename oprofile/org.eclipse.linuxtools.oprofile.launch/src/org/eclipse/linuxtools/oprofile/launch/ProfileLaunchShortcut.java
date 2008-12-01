/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This class is used to launch the profiling configuration from
 * the right click 'Profile With OProfile' popup menu. 
 */
public class ProfileLaunchShortcut implements ILaunchShortcut {

	//only need the IFile from the open file to be able to get the 
	// project it is contained in
	public void launch(IEditorPart editor, String mode) {
		launch(((IFileEditorInput)editor.getEditorInput()).getFile(), mode);
	}

	//guaranteed to only have one element in the selection
	// because the launch shortcut won't be enabled if more
	// than one element is selected -- check plugin.xml for
	// the enablement condition
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			launch(((IStructuredSelection) selection).getFirstElement(), mode);
		}
	}
		
	
	public void launch (final Object element, String mode) {
		//find the project associated with the element
		final IProject project;
		
		if (element instanceof IFile) {
			project = ((IFile)element).getProject();
		} else if (element instanceof IProject) {
			project = (IProject)element;
		} else if (element instanceof IBinary) {
			project = ((IBinary)element).getCProject().getProject();
		} else {
			project = null;
			System.out.println(element);
		}
		
		if (project == null) {
			MessageDialog.openError(LaunchPlugin.getActiveWorkbenchShell(), OprofileLaunchMessages.getString("launchshortcut.errordialog.title"), OprofileLaunchMessages.getString("launchshortcut.no_project_error"));
		} else {
		
			//find the C launch configuration(s) associated with the project,
			// then find any profiling configurations that use any of these C 
			// launch configs
			ConfigRunner configRunner = new ConfigRunner(project);
			
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(LaunchPlugin.getActiveWorkbenchShell());
			
			try {
				dialog.run(true, false, configRunner);
			} 
			catch (InvocationTargetException e) { e.printStackTrace(); } 
			catch (InterruptedException e) { e.printStackTrace(); }
			
			ArrayList<ILaunchConfiguration> profilingLaunchConfigs = configRunner.getProfilingConfigs();
			
			if (profilingLaunchConfigs == null) {
				MessageDialog.openError(null, OprofileLaunchMessages.getString("launchshortcut.errordialog.title"), OprofileLaunchMessages.getString("launchshortcut.errordialog.no_launch_error"));
			} else if (profilingLaunchConfigs.size() == 1) {
				launchProfilingLaunchConfiguration(profilingLaunchConfigs.get(0), mode);
			} else if (profilingLaunchConfigs.size() > 1) {
				openLaunchSelectionDialog(profilingLaunchConfigs, mode);
			}
		}
	}
	
	//inner class to run the profiling lookup in a separate thread with a progress monitor
	class ConfigRunner implements IRunnableWithProgress {
		private ArrayList<ILaunchConfiguration> profiling_configs;
		private IProject project;
		
		ConfigRunner(IProject p) {
			project = p;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			ArrayList<ILaunchConfiguration> c_configs = findCDTLaunchConfigFromProject(project);
			if (c_configs.isEmpty()) {
				profiling_configs = null;
				return;
			}
			profiling_configs = findOProfileLaunchConfig(c_configs);
			if (profiling_configs.isEmpty()) {
				profiling_configs = null;
				return;
			}
		}

		public ArrayList<ILaunchConfiguration> getProfilingConfigs() { 
			return profiling_configs; 
		}
	}


	private ArrayList<ILaunchConfiguration> findCDTLaunchConfigFromProject(final IProject project) {
		ArrayList<ILaunchConfiguration> applicableLaunchConfigs = new ArrayList<ILaunchConfiguration>();
		
		if (project != null) {
			try {
				ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
				final ILaunchConfiguration [] configs = mgr.getLaunchConfigurations();
				
				for (ILaunchConfiguration currentConfig : configs) {
					String configProjectName = currentConfig.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
					
					if (configProjectName.length() > 0 && project.getName().equals(configProjectName)) {
						applicableLaunchConfigs.add(currentConfig);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return applicableLaunchConfigs;
	}
	
	
	private ArrayList<ILaunchConfiguration> findOProfileLaunchConfig(final ArrayList<ILaunchConfiguration> launcheslist) {
		final ILaunchConfiguration [] claunches = new ILaunchConfiguration[launcheslist.size()];
		launcheslist.toArray(claunches);
		ArrayList<ILaunchConfiguration> goodOProfileLaunchConfigs = new ArrayList<ILaunchConfiguration>();
		
		if (!launcheslist.isEmpty()) {
			try {
				ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
				final ILaunchConfiguration [] configs = mgr.getLaunchConfigurations();
								
				for (ILaunchConfiguration currentCConfig : claunches) {
					for (ILaunchConfiguration profilingConfig : configs) {
						String associatedCLaunchStr = profilingConfig.getAttribute(LaunchPlugin.ATTR_C_LAUNCH_CONFIG, "");
						
						if (associatedCLaunchStr.length() > 0) {
							ILaunchConfiguration associatedCLaunchConfig = mgr.getLaunchConfiguration(associatedCLaunchStr);
							
							if (currentCConfig.equals(associatedCLaunchConfig)) {
								goodOProfileLaunchConfigs.add(profilingConfig);
								continue;
							}
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}		
		}
		
		return goodOProfileLaunchConfigs;
	}

	//give a profiling launch configuration, launch it with a ProfileLaunchConfigurationDelegate
	// as if it had been invoke from the configurations dialog 
	private void launchProfilingLaunchConfiguration(ILaunchConfiguration launchConfiguration, String mode) {
		if (launchConfiguration != null) {
			try {
				launchConfiguration.launch(mode, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	//if there is more than one matching profile launch configuration, pop up a selection
	// dialog for the user to choose from, then launch it
	private void openLaunchSelectionDialog(ArrayList<ILaunchConfiguration> profilingLaunchConfigs, String launchMode) {
		ILaunchConfiguration [] configs = new ILaunchConfiguration[profilingLaunchConfigs.size()];
		profilingLaunchConfigs.toArray(configs);
		
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(LaunchPlugin.getActiveWorkbenchShell(), new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof ILaunchConfiguration) {
					return ((ILaunchConfiguration)element).getName();
				} else {
					return OprofileLaunchMessages.getString("launchshortcut.launchselectiondialog.no_project_name");
				}
			}
		});
		
		dialog.setTitle(OprofileLaunchMessages.getString("launchshortcut.launchselectiondialog.title"));
		dialog.setMessage(OprofileLaunchMessages.getString("launchshortcut.launchselectiondialog.message"));
		dialog.setElements(configs);
		dialog.setBlockOnOpen(true);
		dialog.open();
		
		ILaunchConfiguration result = null;
		Object [] results = dialog.getResult();
		
		if (results != null && results.length > 0) {
			result = (ILaunchConfiguration)results[0];
		}
		
		launchProfilingLaunchConfiguration(result, launchMode);
	}

}