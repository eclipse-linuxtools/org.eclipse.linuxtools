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

import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.DOCKER_CONNECTION;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.FORCE_RM_INTERMEDIATE_CONTAINERS;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.NO_CACHE;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.QUIET_BUILD;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.REPO_NAME;
import static org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions.RM_INTERMEDIATE_CONTAINERS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_LOCATION;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants.SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
	public void launch(final ILaunchConfiguration configuration,
			final String mode, final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {
		final String sourcePathLocation = configuration
				.getAttribute(SOURCE_PATH_LOCATION, (String) null);
		final boolean sourcePathWorkspaceRelativeLocation = configuration
				.getAttribute(SOURCE_PATH_WORKSPACE_RELATIVE_LOCATION,
						false);
		final IPath sourcePath = BuildDockerImageUtils.getPath(
				sourcePathLocation,
				sourcePathWorkspaceRelativeLocation);
		final String connectionName = configuration
				.getAttribute(DOCKER_CONNECTION, (String) null);
		final String repoName = configuration.getAttribute(REPO_NAME,
				(String) null);
		final DockerConnection connection = (DockerConnection) getDockerConnection(
				connectionName);
		final Map<String, Object> buildOptions = new HashMap<>();
		buildOptions.put(QUIET_BUILD,
				configuration.getAttribute(QUIET_BUILD, false));
		buildOptions.put(NO_CACHE, configuration.getAttribute(NO_CACHE, false));
		buildOptions.put(RM_INTERMEDIATE_CONTAINERS, configuration
				.getAttribute(RM_INTERMEDIATE_CONTAINERS, true));
		buildOptions.put(FORCE_RM_INTERMEDIATE_CONTAINERS, configuration
				.getAttribute(FORCE_RM_INTERMEDIATE_CONTAINERS, false));
		if (connection == null) {
			Activator
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							LaunchMessages.getFormattedString(
									MISSING_CONNECTION_ERROR_MSG,
									connectionName)));
		}
		try {
			if (connection != null && sourcePath != null) {
				final Job buildImageJob = new BuildDockerImageJob(connection,
						sourcePath, repoName, buildOptions);
				buildImageJob.schedule();
			} else {
				Activator.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
						LaunchMessages.getString(
								"BuildDockerImageLaunchConfiguration.error.incomplete"))); //$NON-NLS-1$
			}
		} catch (DockerException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					e.getMessage(), e));
		}
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
