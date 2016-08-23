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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerCompose;
import org.eclipse.linuxtools.internal.docker.ui.consoles.DockerComposeConsole;
import org.eclipse.linuxtools.internal.docker.ui.consoles.DockerComposeConsoleUtils;
import org.eclipse.linuxtools.internal.docker.ui.preferences.PreferenceConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;

/**
 * An {@link IDockerProgressHandler} {@link Job} to run the
 * {@code docker-compose up} commandline.
 */
public class DockerComposeUpJob extends Job {

	/** The {@link IDockerConnection} to use. */
	private final IDockerConnection connection;

	/** The workingDir containing the {@code docker-compose.yml}. */
	private final String workingDir;

	/** The {@link ILaunchConfiguration} that was used to launch the job. */
	private final ILaunchConfiguration launchConfiguration;

	/** The dockerComposeConsole used to display output messages. */
	private final DockerComposeConsole dockerComposeConsole;

	/**
	 * Constructor
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} to use
	 * @param workingDir
	 *            the workingDir containing the {@code docker-compose.yml}
	 * @param launchConfiguration
	 */
	public DockerComposeUpJob(final IDockerConnection connection,
			final String workingDir,
			final ILaunchConfiguration launchConfiguration) {
		super(JobMessages.getString("DockerComposeUp.title")); //$NON-NLS-1$
		this.connection = connection;
		this.workingDir = workingDir;
		this.launchConfiguration = launchConfiguration;
		this.dockerComposeConsole = DockerComposeConsoleUtils
				.findConsole(connection, workingDir);
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final String dockerComposeInstallDir = Activator.getDefault()
				.getPreferenceStore().getString(
						PreferenceConstants.DOCKER_COMPOSE_INSTALLATION_DIRECTORY);

		final Thread commandThread = new Thread(() -> {
			// open console view
			ConsolePlugin.getDefault().getConsoleManager()
					.showConsoleView(dockerComposeConsole);
			try {
				// run the 'docker-compose up' command
				final Process dockerComposeSystemProcess = DockerCompose
						.getInstance()
						.up(this.connection, dockerComposeInstallDir,
								this.workingDir);
				final ILaunch launch = new Launch(launchConfiguration,
						ILaunchManager.RUN_MODE, null);
				final IProcess dockerComposeProcess = DebugPlugin.newProcess(
						launch, dockerComposeSystemProcess,
						"docker-compose up"); //$NON-NLS-1$
				dockerComposeConsole
						.setDockerComposeProcess(dockerComposeProcess); // $NON-NLS-1$
				final int exitCode = dockerComposeSystemProcess.waitFor();
				if (exitCode != 0) {
					Activator.logErrorMessage(
							"'docker-compose up' exited with code " + exitCode); //$NON-NLS-1$
				}
			} catch (DockerException | InterruptedException e) {
				Display.getDefault()
						.asyncExec(() -> MessageDialog.openError(
								Display.getCurrent().getActiveShell(),
								JobMessages.getString(
										"DockerCompose.dialog.title"), //$NON-NLS-1$
								e.getMessage()));
				Activator.log(e);
			}

		});
		commandThread.start();
		return Status.OK_STATUS;

	}

}
