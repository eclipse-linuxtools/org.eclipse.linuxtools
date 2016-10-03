/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.util.function.Predicate;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageBuildDialog;
import org.eclipse.linuxtools.internal.docker.ui.wizards.NewDockerConnection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * An {@link ILaunchShortcut} to build a Docker image from a selected
 * Dockerfile.
 */
public class BuildDockerImageShortcut
		extends BaseResourceAwareLaunchShortcut {

	@Override
	protected void launch(IResource resource, String mode) {
		// the predicate to apply on the launch configuration to find the
		// matching candidates
		final Predicate<ILaunchConfiguration> predicate = config -> {
			try {
				final String sourcePath = config.getAttribute(
						IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION,
						""); //$NON-NLS-1$
				final boolean workspaceRelative = config.getAttribute(
						IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION,
						false);
				final IPath dockerfilePath = getPath(sourcePath,
						workspaceRelative);
				return dockerfilePath
						.equals(resource.getLocation().removeLastSegments(1));
			} catch (CoreException e) {
				Activator.log(e);
				return false;
			}
		};

		final ILaunchConfiguration config = findLaunchConfiguration(
				IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID,
				resource, predicate);
		if (config != null) {
			DebugUITools.launch(config, mode);
		} else {
			Activator.log(new DockerException(LaunchMessages
					.getString("BuildDockerImageShortcut.launchconfig.error"))); //$NON-NLS-1$
		}
	}
	
	/**
	 * Create a launch configuration based on a Dockerfile resource, and
	 * optionally save it to the underlying resource.
	 *
	 * @param dockerfile
	 *            a {@code Dockerfile} file to build
	 * @return a launch configuration generated for the Dockerfile build.
	 */
	@Override
	protected ILaunchConfiguration createConfiguration(
			final IResource dockerfile) {
		try {
			if (!DockerConnectionManager.getInstance().hasConnections()) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						boolean confirm = MessageDialog.openQuestion(
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getShell(),
								LaunchMessages.getString(
										"BuildDockerImageShortcut.no.connections.msg"), //$NON-NLS-1$
								LaunchMessages.getString(
										"BuildDockerImageShortcut.no.connections.desc")); //$NON-NLS-1$
						if (confirm) {
							final NewDockerConnection newConnWizard = new NewDockerConnection();
							CommandUtils.openWizard(newConnWizard,
									PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow()
											.getShell());
						}
					}
				});
				return null;
			} else {
				final ImageBuildDialog dialog = new ImageBuildDialog(
						getActiveWorkbenchShell());
				final int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					return LaunchConfigurationUtils
							.createBuildImageLaunchConfiguration(
									dialog.getConnection(),
									dialog.getRepoName(), dockerfile);
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return null;
	}

}
