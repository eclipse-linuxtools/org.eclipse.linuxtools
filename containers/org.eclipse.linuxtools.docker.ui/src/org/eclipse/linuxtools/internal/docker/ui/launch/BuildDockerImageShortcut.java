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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageBuildDialog;
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
		ILaunchConfiguration config = findLaunchConfiguration(resource);
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
	 *
	 * @return A re-useable config or <code>null</code> if none.
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IResource resource) {
		ILaunchConfiguration configuration = null;
		ILaunchConfigurationType configType = LaunchConfigurationUtils
				.getLaunchConfigType(
						IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);
		List<ILaunchConfiguration> candidateConfigs = Collections.emptyList();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList<>(configs.length);
			for (ILaunchConfiguration config : configs) {
				String sourcePath = config.getAttribute(
						IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION,
						""); //$NON-NLS-1$
				boolean workspaceRelative = config.getAttribute(
						IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION,
						false);
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
			configuration = createConfiguration(resource);
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
	 * @return a launch configuration generated for the Dockerfile build.
	 */
	protected ILaunchConfiguration createConfiguration(
			final IResource resource) {
		try {
			final IDockerConnection[] connections = DockerConnectionManager
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
			} else {
				final ImageBuildDialog dialog = new ImageBuildDialog(
						getActiveWorkbenchShell());
				final int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					final ILaunchConfigurationType configType = LaunchConfigurationUtils
							.getLaunchConfigType(
									IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);
					final ILaunchConfigurationWorkingCopy wc = configType
							.newInstance(null,
									DebugPlugin.getDefault().getLaunchManager()
											.generateLaunchConfigurationName(
													createLaunchConfigurationName(
															dialog.getRepoName(),
															resource))); // $NON-NLS-1$
					wc.setAttribute(
							IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION,
							resource.getFullPath().removeLastSegments(1)
									.toString());
					wc.setAttribute(
							IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION,
							true);

					final IDockerConnection connection = dialog.getConnection();
					final String repoName = dialog.getRepoName();
					wc.setAttribute(IDockerImageBuildOptions.DOCKER_CONNECTION,
							connection.getName());
					wc.setAttribute(IDockerImageBuildOptions.REPO_NAME,
							repoName);
					return wc.doSave();
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return null;
	}

	/**
	 * Creates a Launch Configuration name from the given repoName or from the
	 * given resource's project if the repoName was <code>null</code>.
	 * 
	 * @param imageName the full image name
	 * @param resource the Dockerfile to use to build the image
	 * @return the {@link ILaunchConfiguration} name
	 */
	public static String createLaunchConfigurationName(final String imageName,
			final IResource resource) {
		if (imageName != null) {
			final String repository = BuildDockerImageUtils
					.getRepository(imageName);
			final String name = BuildDockerImageUtils.getName(imageName);
			final String tag = BuildDockerImageUtils.getTag(imageName);
			final StringBuilder configNameBuilder = new StringBuilder();
			// image name is the minimum requirement
			if (name != null) {
				if (repository != null) {
					configNameBuilder.append(repository).append('_'); // $NON-NLS-1$
				}
				if (name != null) {
					configNameBuilder.append(name);
				}
				if (tag != null) {
					configNameBuilder.append(" [").append(tag).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					configNameBuilder.append(" [latest]"); //$NON-NLS-1$
				}
				return configNameBuilder.toString();
			}
		}
		return "Dockerfile [" //$NON-NLS-1$
				+ resource.getProject().getName() + "]"; //$NON-NLS-1$
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
