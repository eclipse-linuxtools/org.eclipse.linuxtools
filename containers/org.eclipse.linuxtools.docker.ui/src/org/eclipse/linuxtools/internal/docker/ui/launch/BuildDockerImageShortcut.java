/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class BuildDockerImageShortcut implements ILaunchShortcut {
	private static final String LaunchShortcut_Error_Launching = "ImageBuildShortcut.error.msg"; //$NON-NLS-1$
	private static final String LaunchShortcut_No_Connections = "NoConnection.error.msg"; //$NON-NLS-1$
	private static final String LaunchShortcut_Config_Selection = "ImageBuildShortcutConfigSelection.title"; //$NON-NLS-1$
	private static final String LaunchShortcut_Choose_Launch = "ImageBuildShortcutChooseLaunch.msg"; //$NON-NLS-1$
	private static final String LaunchShortcut_Connection_Selection = "ImageBuildShortcutConnectionSelection.title"; //$NON-NLS-1$
	private static final String LaunchShortcut_Choose_Connection = "ImageBuildShortcutChooseConnection.msg"; //$NON-NLS-1$

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			IResource resource = (IResource) ((IStructuredSelection) selection)
					.toArray()[0];
			launch(resource, mode);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		launch(editor.getEditorInput().getAdapter(IResource.class), mode);
	}

	public void launch(IResource resource, String mode) {
		ILaunchConfiguration config = findLaunchConfiguration(resource, mode);
		if (config != null) {
			DebugUITools.launch(config, mode);
		}
	}
	
	/**
	 * Locate a configuration to launch for the given type. If one cannot be
	 * found, create one.
	 * 
	 * @param resource
	 *            The Dockerfile to look up launch for.
	 * @param mode
	 *            Launch mode.
	 *
	 * @return A re-useable config or <code>null</code> if none.
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IResource resource,
			String mode) {
		ILaunchConfiguration configuration = null;
		ILaunchConfigurationType configType = getLaunchConfigType();
		List<ILaunchConfiguration> candidateConfigs = Collections.emptyList();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList<>(configs.length);
			for (ILaunchConfiguration config : configs) {
				String sourcePath = config.getAttribute(
						IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION,
						""); //$NON-NLS-1$
				boolean workspaceRelative = config
						.getAttribute(IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION, false);
				IPath dockerfilePath = getPath(sourcePath, workspaceRelative);
				if (dockerfilePath
						.equals(resource.getLocation().removeLastSegments(1))) {
					candidateConfigs.add(config);
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}

		// If there are no existing configurations associated with the
		// Dockerfile,
		// create one. If there is exactly one configuration associated with the
		// Dockerfile, return it. Otherwise, if there is more than one
		// configuration associated with the Dockerfile, prompt the user to
		// choose
		// one.
		int candidateCount = candidateConfigs.size();
		if (candidateCount < 1) {
			configuration = createConfiguration(resource, mode, true);
		} else if (candidateCount == 1) {
			configuration = candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a configuration. A null result means
			// the user
			// cancelled the dialog, in which case this method returns null,
			// since canceling the dialog should also cancel launching
			// anything.
			configuration = chooseConfiguration(candidateConfigs);
		}
		return configuration;
	}

	/**
	 * Method getLaunchConfigType.
	 * 
	 * @return ILaunchConfigurationType
	 */
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);
	}

	/**
	 * Get the path of the Dockerfile.
	 * 
	 * @param sourcePathLocation
	 *            - location path of the Dockerfile
	 * @param sourcePathWorkspaceRelativeLocation
	 *            - true, if path above is relative to workspace
	 * @return the absolute file path of the Dockerfile
	 */
	private IPath getPath(final String sourcePathLocation,
			final boolean sourcePathWorkspaceRelativeLocation) {
		if (sourcePathWorkspaceRelativeLocation) {
			return ResourcesPlugin.getWorkspace().getRoot()
					.findMember(new Path(sourcePathLocation)).getLocation();
		}
		return new Path(sourcePathLocation);
	}

	/**
	 * Create a launch configuration based on a Dockerfile resource, and
	 * optionally save it to the underlying resource.
	 *
	 * @param resource
	 *            a Dockerfile file to build
	 * @param save
	 *            true if the configuration should be saved to the underlying
	 *            resource, and false if it should not be saved.
	 * @return a launch configuration generated for the Dockerfile build.
	 */
	protected ILaunchConfiguration createConfiguration(IResource resource,
			@SuppressWarnings("unused") String mode, boolean save) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					getLaunchManager()
							.generateLaunchConfigurationName("Dockerfile[" //$NON-NLS-1$
									+ resource.getProject().getName() + "]")); //$NON-NLS-1$

			wc.setAttribute(
					IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION,
					resource.getFullPath().removeLastSegments(1).toString());
			wc.setAttribute(
					IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION,
					true);

			IDockerConnection[] connections = DockerConnectionManager
					.getInstance().getConnections();
			if (connections.length == 0) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog.openError(
								Display.getCurrent().getActiveShell(),
								LaunchMessages.getString(
										LaunchShortcut_Error_Launching),
								LaunchMessages.getString(
										LaunchShortcut_No_Connections));
					}

				});
				return null;
			} else if (connections.length == 1) {
				wc.setAttribute(
					IBuildDockerImageLaunchConfigurationConstants.DOCKER_CONNECTION,
					connections[0].getName());
			} else {
				IDockerConnection connection = chooseConnection(connections);
				if (connection == null)
					return null;
				wc.setAttribute(
						IBuildDockerImageLaunchConfigurationConstants.DOCKER_CONNECTION,
						connection.getName());
			}
			if (save) {
				config = wc.doSave();
			} else {
				config = wc;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}

	/**
	 * Get the Debug launch manager.
	 * 
	 * @return the launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	private class ConnectionSelectionLabelProvider implements ILabelProvider {
		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public String getText(Object element) {
			return element.toString();
		}

		@Override
		public Image getImage(Object element) {
			return SWTImagesFactory.DESC_REPOSITORY_MIDDLE.createImage();
		}
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the
	 * connections to use to build the Image.
	 * 
	 * @param connections
	 *            Array of connections.
	 * @return The chosen connection, or <code>null</code> if the user cancelled
	 *         the dialog.
	 */
	protected IDockerConnection chooseConnection(
			IDockerConnection[] connections) {
		IDebugModelPresentation labelProvider = DebugUITools
				.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getActiveWorkbenchShell(),
				new ConnectionSelectionLabelProvider() {

				});

		dialog.setElements(connections);
		dialog.setTitle(
				LaunchMessages.getString(LaunchShortcut_Connection_Selection));
		dialog.setMessage(
				LaunchMessages.getString(LaunchShortcut_Choose_Connection));
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == IStatus.OK) {
			return (IDockerConnection) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the
	 * specified launch configurations.
	 * 
	 * @param configList
	 *            The list of launch configurations to choose from.
	 * @return The chosen config, or <code>null</code> if the user cancelled the
	 *         dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(
			List<ILaunchConfiguration> configList) {
		IDebugModelPresentation labelProvider = DebugUITools
				.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getActiveWorkbenchShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(
				LaunchMessages.getString(LaunchShortcut_Config_Selection));
		dialog.setMessage(
				LaunchMessages.getString(LaunchShortcut_Choose_Launch));
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == IStatus.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Get the active Workbench shell.
	 * 
	 * @return active shell as returned by the plug-in
	 */
	protected Shell getActiveWorkbenchShell() {
		return Activator.getActiveWorkbenchShell();
	}

}
