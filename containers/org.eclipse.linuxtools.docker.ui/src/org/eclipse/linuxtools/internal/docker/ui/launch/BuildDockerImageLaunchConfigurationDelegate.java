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

import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.DOCKER_CONNECTION;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.jobs.BuildDockerImageJob;

/**
 * The {@link ILaunchConfigurationDelegate} to trigger the build of a Docker
 * Image.
 *
 */
public class BuildDockerImageLaunchConfigurationDelegate
		implements ILaunchConfigurationDelegate {

	private static final String MISSING_CONNECTION_ERROR_MSG = "MissingConnectionError.msg"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		final String sourcePathLocation = configuration
				.getAttribute(SOURCE_PATH_LOCATION, (String) null);
		final boolean sourcePathWorkspaceRelativeLocation = configuration
				.getAttribute(SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION, false);
		final IPath sourcePath = getPath(sourcePathLocation,
				sourcePathWorkspaceRelativeLocation);
		final String connectionName = configuration
				.getAttribute(DOCKER_CONNECTION, (String) null);
		final DockerConnection connection = (DockerConnection) getDockerConnection(
				connectionName);
		if (connection == null) {
			Activator
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							LaunchMessages.getFormattedString(
									MISSING_CONNECTION_ERROR_MSG,
									connectionName)));
		}
		try {
			final Job buildImageJob = new BuildDockerImageJob(connection,
					sourcePath);
			buildImageJob.schedule();
		} catch (DockerException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					e.getMessage(), e));
		}
	}

	private IPath getPath(final String sourcePathLocation,
			final boolean sourcePathWorkspaceRelativeLocation) {
		if (sourcePathWorkspaceRelativeLocation) {
			return ResourcesPlugin.getWorkspace().getRoot()
					.findMember(new Path(sourcePathLocation)).getLocation();
		}
		return new Path(sourcePathLocation);
	}

	/**
	 * Finds the {@link IDockerConnection} from the given name
	 * 
	 * @param connectionName
	 *            the name of the connection to find
	 * @return the {@link IDockerConnection} or <code>null</code> if none
	 *         matched.
	 */
	private IDockerConnection getDockerConnection(final String connectionName) {
		for (IDockerConnection connection : DockerConnectionManager
				.getInstance().getConnections()) {
			if (connection.getName().equals(connectionName)) {
				return connection;
			}
		}
		return null;
	}

}
