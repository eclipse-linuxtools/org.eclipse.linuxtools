/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - bug 178731
 * Elliott Baron <ebaron@redhat.com> - Modified implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.profiling.launch.Messages;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

public abstract class ProfileLaunchShortcut implements ILaunchShortcut {

	public void launch(IEditorPart editor, String mode) {
		searchAndLaunch(new Object[] { editor.getEditorInput() }, mode);
	}

	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection) selection).toArray(), mode);
		}
	}

	public void launch(IBinary bin, String mode) {
		ILaunchConfiguration config = findLaunchConfiguration(bin, mode);
		if (config != null) {
			DebugUITools.launch(config, mode);
		}
	}

	/**
	 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
	 *
	 * @return a re-useable config or <code>null</code> if none
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {
		ILaunchConfiguration configuration = null;
		ILaunchConfigurationType configType = getLaunchConfigType();
		List<ILaunchConfiguration> candidateConfigs = Collections.emptyList();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				IPath programPath = CDebugUtils.getProgramPath(config);
				String projectName = CDebugUtils.getProjectName(config);
				IPath binPath = bin.getResource().getProjectRelativePath();
				if (programPath != null && programPath.equals(binPath)) {
					if (projectName != null && projectName.equals(bin.getCProject().getProject().getName())) {
						candidateConfigs.add(config);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// If there are no existing configs associated with the IBinary, create one.
		// If there is exactly one config associated with the IBinary, return it.
		// Otherwise, if there is more than one config associated with the IBinary, prompt the
		// user to choose one.
		int candidateCount = candidateConfigs.size();
		if (candidateCount < 1) {
			configuration = createConfiguration(bin);
		} else if (candidateCount == 1) {
			configuration = candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a config.  A null result means the user
			// cancelled the dialog, in which case this method returns null,
			// since cancelling the dialog should also cancel launching anything.
			configuration = chooseConfiguration(candidateConfigs, mode);
		}
		return configuration;
	}

	/**
	 * Can return <code>getLaunchManager().getLaunchConfigurationType(String ID)</code>.
	 * This String will be used to identify your configuration type to Eclipse, and should
	 * be unique.
	 *
	 * @return The launch configuration type.
	 */
	protected abstract ILaunchConfigurationType getLaunchConfigType();

	protected abstract void setDefaultProfileAttributes(ILaunchConfigurationWorkingCopy wc) throws CoreException;

	/**
	 * Set default attributes for the given configuration.
	 *
	 * @param wc
	 * @since 1.2
	 */
	public void setDefaultProfileLaunchShortcutAttributes(ILaunchConfigurationWorkingCopy wc){
		try {
			setDefaultProfileAttributes(wc);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected ILaunchConfiguration createConfiguration(IBinary bin) {
		return createConfiguration(bin, true);
	}

	/**
	 * Create a launch configuration based on a binary, and optionally
	 * save it to the underlying resource.
	 *
	 * @param bin a representation of a binary
	 * @param save true if the configuration should be saved to the
	 * underlying resource, and false if it should not be saved.
	 * @return a launch configuration generated for the binary.
	 * @since 1.2
	 */
	protected ILaunchConfiguration createConfiguration(IBinary bin, boolean save) {
		ILaunchConfiguration config = null;
		try {
			String projectName = bin.getResource().getProjectRelativePath().toString();
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName(bin.getElementName()));

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, projectName);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, bin.getCProject().getElementName());
			wc.setMappedResources(new IResource[] {bin.getResource(), bin.getResource().getProject()});
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);

			setDefaultProfileAttributes(wc);

			if (save){
				config = wc.doSave();
			} else {
				config = wc;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Method searchAndLaunch.
	 * @param objects
	 * @param mode
	 */
	private void searchAndLaunch(final Object[] elements, String mode) {
		if (elements != null && elements.length > 0) {
			IBinary bin = null;
			if (elements.length == 1 && elements[0] instanceof IBinary) {
				bin = (IBinary) elements[0];
			} else {
				final List<IBinary> results = new ArrayList<IBinary>();
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getActiveWorkbenchShell());
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor pm) throws InterruptedException {
						int nElements = elements.length;
						pm.beginTask(Messages.getString("ProfileLaunchShortcut.Looking_for_executables"), nElements); //$NON-NLS-1$
						try {
							IProgressMonitor sub = new SubProgressMonitor(pm, 1);
							for (int i = 0; i < nElements; i++) {
								if (elements[i] instanceof IAdaptable) {
									IResource r = (IResource) ((IAdaptable) elements[i]).getAdapter(IResource.class);
									if (r != null) {
										ICProject cproject = CoreModel.getDefault().create(r.getProject());
										if (cproject != null) {
											try {
												IBinary[] bins = cproject.getBinaryContainer().getBinaries();

												for (int j = 0; j < bins.length; j++) {
													if (bins[j].isExecutable()) {
														results.add(bins[j]);
													}
												}
											} catch (CModelException e) {
												// TODO should this be simply ignored ?
											}
										}
									}
								}
								if (pm.isCanceled()) {
									throw new InterruptedException();
								}
								sub.done();
							}
						} finally {
							pm.done();
						}
					}
				};
				try {
					dialog.run(true, true, runnable);
				} catch (InterruptedException e) {
					return;
				} catch (InvocationTargetException e) {
					handleFail(e.getMessage());
					return;
				}
				int count = results.size();
				if (count == 0) {
					handleFail(Messages.getString("ProfileLaunchShortcut.Binary_not_found")); //$NON-NLS-1$
				} else if (count > 1) {
					bin = chooseBinary(results, mode);
				} else {
					bin = results.get(0);
				}
			}
			if (bin != null) {
				launch(bin, mode);
			}
		} else {
			handleFail(Messages.getString("ProfileLaunchShortcut.no_project_selected")); //$NON-NLS-1$
		}
	}

	protected void handleFail(String message) {
		MessageDialog.openError(getActiveWorkbenchShell(), Messages.getString("ProfileLaunchShortcut.Launcher"), message); //$NON-NLS-1$
	}

	/**
	 * Prompts the user to select a  binary
	 *
	 * @return the selected binary or <code>null</code> if none.
	 */
	protected IBinary chooseBinary(List<IBinary> binList, String mode) {
		ILabelProvider programLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary)element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getPath().lastSegment());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary)element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getCPU() + (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
					name.append(" - "); //$NON-NLS-1$
					name.append(bin.getPath().toString());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		TwoPaneElementSelector dialog = new TwoPaneElementSelector(getActiveWorkbenchShell(), programLabelProvider, qualifierLabelProvider);
		dialog.setElements(binList.toArray());
		dialog.setTitle(Messages.getString("ProfileLaunchShortcut.Profile")); //$NON-NLS-1$
		dialog.setMessage(Messages.getString("ProfileLaunchShortcut.Choose_a_local_application")); //$NON-NLS-1$
		dialog.setUpperListLabel(Messages.getString("ProfileLaunchShortcut.Binaries")); //$NON-NLS-1$
		dialog.setLowerListLabel(Messages.getString("ProfileLaunchShortcut.Qualifier")); //$NON-NLS-1$
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			return (IBinary) dialog.getFirstResult();
		}

		return null;
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the specified
	 * launch configurations.  Return the chosen config, or <code>null</code> if the
	 * user cancelled the dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList, String mode) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getActiveWorkbenchShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(Messages.getString("ProfileLaunchShortcut.Launch_Configuration_Selection"));  //$NON-NLS-1$
		dialog.setMessage(Messages.getString("ProfileLaunchShortcut.Choose_a_launch_configuration"));  //$NON-NLS-1$
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	protected Shell getActiveWorkbenchShell() {
		return ProfileLaunchPlugin.getActiveWorkbenchShell();
	}

}