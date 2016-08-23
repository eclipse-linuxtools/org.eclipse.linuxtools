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

package org.eclipse.linuxtools.internal.docker.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerCompose;
import org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants;

/**
 * An {@link IDockerProgressHandler} {@link Job} to execute a
 * {@code docker-compose stop} commandline.
 */
public class DockerComposeStopJob extends Job {

	/** The {@link IDockerConnection} to use. */
	private final IDockerConnection connection;

	/** The workDir containing the {@code docker-compose.yml}. */
	private final String workingDir;

	/**
	 * Constructor
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} to use
	 * @param workingDir
	 *            the workDir containing the {@code docker-compose.yml}
	 */
	public DockerComposeStopJob(final IDockerConnection connection,
			final String workingDir) {
		super(JobMessages.getString("DockerComposeStop.title")); //$NON-NLS-1$
		this.connection = connection;
		this.workingDir = workingDir;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final String dockerComposeInstallDir = Activator.getDefault()
				.getPreferenceStore().getString(
						PreferenceConstants.DOCKER_COMPOSE_INSTALLATION_DIRECTORY);
		try {
			final Process dockerComposeStopProcess = DockerCompose.getInstance()
					.stop(this.connection,
					dockerComposeInstallDir, this.workingDir);
			dockerComposeStopProcess.waitFor();
		} catch (InterruptedException | DockerException e) {
			Activator.logErrorMessage(
					JobMessages.getString("DockerComposeStop.error"), e); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}

}
