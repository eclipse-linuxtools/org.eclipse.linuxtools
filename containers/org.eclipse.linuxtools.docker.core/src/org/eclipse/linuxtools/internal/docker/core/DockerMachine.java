/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.DockerException;

/**
 * Utility class to discover Docker machines using the 'docker-machine' command
 * line in a process.
 */
public class DockerMachine {

	/**
	 * Checks that the given {@code dockerMachineInstallDir} contains the
	 * {@code docker-machine} command
	 * 
	 * @param dockerMachineInstallDir
	 *            the directory to check
	 * @return <code>true</code> if the system-specific command was found,
	 *         <code>false</code> otherwise.
	 */
	public static boolean checkPathToDockerMachine(
			final String dockerMachineInstallDir) {
		return ProcessUtils.checkPathToCommand(dockerMachineInstallDir,
				getDockerMachineExecutableName());
	}

	/**
	 * @param pathToDockerMachine
	 *            the path to 'docker-machine' stored in the preferences
	 * @return the names of the existing Docker Machines
	 * @throws DockerException
	 *             if something went wrong
	 */
	public static String[] getNames(final String pathToDockerMachine)
			throws DockerException {
		return ProcessUtils.processBuilder(pathToDockerMachine,
				getDockerMachineExecutableName(), new String[] { "ls", "-q" }) //$NON-NLS-1$ //$NON-NLS-2$
				.start();
	}

	/**
	 * @param name
	 *            the name of the Docker Machine to inspect
	 * @param dockerMachineInstallDir
	 *            the installation directory for Docker Machine
	 * @param vmDriverInstallDir
	 *            the installation directory for the underlying VM driver used
	 *            by Docker Machine
	 * @return the host URI to use to connect to it
	 * @throws DockerException
	 *             if something went wrong
	 */
	public static String getHost(final String name,
			final String dockerMachineInstallDir,
			final String vmDriverInstallDir) throws DockerException {
		final String[] res = ProcessUtils
				.processBuilder(dockerMachineInstallDir,
				getDockerMachineExecutableName(),
						new String[] { "url", name }) //$NON-NLS-1$
				.extraPath(vmDriverInstallDir).start();
		return res.length == 1 ? res[0] : null;
	}

	/**
	 * @param name
	 *            the name of the Docker Machine to inspect
	 * @param pathToDockerMachine
	 *            the path to 'docker-machine' stored in the preferences
	 * @param vmDriverInstallDir
	 *            the installation directory for the underlying VM driver used
	 *            by Docker Machine
	 * @return the path to the directory containing the certificates
	 * @throws DockerException
	 *             if something went wrong
	 */
	public static String getCertPath(final String name,
			final String pathToDockerMachine, final String vmDriverInstallDir)
			throws DockerException {
		final String[] envVariables = ProcessUtils.processBuilder(pathToDockerMachine,
				getDockerMachineExecutableName(),
				new String[] { "env", name }) //$NON-NLS-1$
				.extraPath(vmDriverInstallDir).start();
		for (String envVariable : envVariables) {
			if (envVariable.contains("DOCKER_CERT_PATH")) { //$NON-NLS-1$
				// DOCKER_CERT_PATH="/path/to/cert-folder"
				return envVariable.split("=")[1].replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		return null;
	}

	/**
	 * @return the name of the Docker Machine executable, depending on the
	 *         current operating system.
	 */
	private static String getDockerMachineExecutableName() {
		if (SystemUtils.isWindows()) {
			return "docker-machine.exe"; //$NON-NLS-1$
		}
		return "docker-machine"; //$NON-NLS-1$
	}
}
