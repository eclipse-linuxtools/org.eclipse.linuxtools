/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.eclipse.linuxtools.internal.docker.ui.launch.IDockerComposeLaunchConfigurationConstants.DOCKER_CONNECTION;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IDockerComposeLaunchConfigurationConstants.WORKING_DIR;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IDockerComposeLaunchConfigurationConstants.WORKING_DIR_WORKSPACE_RELATIVE_LOCATION;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.jobs.DockerComposeUpJob;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * The {@link ILaunchConfigurationDelegate} to run the "docker-compose up'
 * command from the directory containing the specified 'docker-compose.yml'
 * file.
 */
public class DockerComposeUpLaunchConfigurationDelegate
		implements ILaunchConfigurationDelegate {

	@Override
	public void launch(final ILaunchConfiguration configuration,
			final String mode, final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {
		final String sourcePathLocation = configuration
				.getAttribute(WORKING_DIR, (String) null);
		final boolean sourcePathWorkspaceRelativeLocation = configuration
				.getAttribute(WORKING_DIR_WORKSPACE_RELATIVE_LOCATION, false);
		final IPath sourcePath = BuildDockerImageUtils.getPath(
				sourcePathLocation, sourcePathWorkspaceRelativeLocation);
		final String connectionName = configuration
				.getAttribute(DOCKER_CONNECTION, (String) null);
		final IDockerConnection connection = DockerConnectionManager
				.getInstance().getConnectionByName(connectionName);
		if (connection != null && sourcePath != null) {
			final Job dockerComposeUpJob = new DockerComposeUpJob(connection,
					sourcePath.toOSString(), configuration);
			dockerComposeUpJob.schedule();
		} else {
			final ILaunchGroup launchGroup = DebugUITools
					.getLaunchGroup(configuration, "run"); //$NON-NLS-1$
			// prompt the user with the launch configuration editor
			Display.getDefault()
					.syncExec(() -> DebugUITools.openLaunchConfigurationDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							configuration, launchGroup.getIdentifier(), null));

		}
	}
}
