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

package org.eclipse.linuxtools.internal.docker.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;

/**
 * A utility class to run the {@code docker-compose} command line.
 */
public class DockerCompose {

	private static DockerCompose instance = new DockerCompose();

	private ProcessLauncher processLauncher = new ProcessLauncher();

	public static DockerCompose getInstance() {
		return instance;
	}

	private DockerCompose() {

	}

	/**
	 * Replace the default {@link ProcessLauncher} with another instance. Used
	 * to testing by injecting a mock instance here.
	 * 
	 * @param processLauncher
	 *            the new {@link ProcessLauncher}.
	 */
	public void setProcessLauncher(ProcessLauncher processLauncher) {
		this.processLauncher = processLauncher;
	}

	/**
	 * Checks that the given {@code dockerMachineInstallDir} contains the
	 * {@code docker-compose} command
	 * 
	 * @param dockerComposeInstallDir
	 *            the directory to check
	 * @return <code>true</code> if the system-specific command was found,
	 *         <code>false</code> otherwise.
	 */
	public boolean checkPathToDockerCompose(
			final String dockerComposeInstallDir) {
		return this.processLauncher.checkPathToCommand(dockerComposeInstallDir,
				getDockerComposeCommandName());
	}

	/**
	 * Runs the {@code docker-compose up} command from the specified
	 * {@code baseDir}
	 * 
	 * @param dockerComposeInstallDir
	 *            the directory in which the {@code docker-compose} command is
	 *            located
	 * @param baseDir
	 *            the base directory in which the {@code docker-compose} command
	 *            should be ran. This directory should contain a
	 *            {@code docker-compose.yml} file.
	 * @throws DockerException
	 *             if something wrong happened
	 */
	public Process up(final IDockerConnection connection,
			final String dockerComposeInstallDir, final String baseDir)
			throws DockerException {
		return this.processLauncher.processBuilder(dockerComposeInstallDir,
				getDockerComposeCommandName(), 
				getArguments(connection, "up")) //$NON-NLS-1$
				.workingDir(baseDir).start();
	}

	public Process stop(final IDockerConnection connection,
			final String dockerComposeInstallDir, final String baseDir)
			throws DockerException {
		return this.processLauncher
				.processBuilder(dockerComposeInstallDir,
						getDockerComposeCommandName(),
						getArguments(connection, "stop")) //$NON-NLS-1$
				.workingDir(baseDir).start();
	}

	private static String[] getArguments(final IDockerConnection connection,
			final String commandName) {
		switch (connection.getSettings().getType()) {
		case UNIX_SOCKET_CONNECTION:
			return new String[] { commandName };
		case TCP_CONNECTION:
			final TCPConnectionSettings connectionSettings = (TCPConnectionSettings) connection
					.getSettings();
			// include the flags to connect to the host
			final List<String> args = new ArrayList<>();
			args.add("-H");
			args.add(connectionSettings.getHost());
			if (connectionSettings.isTlsVerify()
					&& connectionSettings.getPathToCertificates() != null) {
				args.add("--tlsverify"); //$NON-NLS-1$
				args.add("--tlscacert"); //$NON-NLS-1$
				args.add(getFilePath(connectionSettings.getPathToCertificates(),
						"ca.pem")); //$NON-NLS-1$
				args.add("--tlscert"); //$NON-NLS-1$
				args.add(getFilePath(connectionSettings.getPathToCertificates(),
						"cert.pem")); //$NON-NLS-1$
				args.add("--tlskey"); //$NON-NLS-1$
				args.add(getFilePath(connectionSettings.getPathToCertificates(),
						"key.pem")); //$NON-NLS-1$

			}
			args.add(commandName);

			return args.toArray(new String[0]);
		}
		return null;
	}

	private static String getFilePath(final String pathToCertificates,
			final String certificateFileName) {
		if (pathToCertificates.endsWith(File.separator)) {
			return pathToCertificates + certificateFileName;
		}
		return pathToCertificates + File.separator + certificateFileName;
	}

	public static String getDockerComposeCommandName() {
		if (SystemUtils.isWindows()) {
			return "docker-compose.exe"; //$NON-NLS-1$
		}
		return "docker-compose"; //$NON-NLS-1$
	}

}
