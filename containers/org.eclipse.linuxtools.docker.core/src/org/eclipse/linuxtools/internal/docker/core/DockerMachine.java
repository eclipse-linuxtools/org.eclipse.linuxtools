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
package org.eclipse.linuxtools.internal.docker.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.docker.core.Activator;

/**
 * Utility class to discover Docker machines using the 'docker-machine' command
 * line in a process.
 */
public class DockerMachine {

	/**
	 * @param pathToDockerMachine
	 *            the path to 'docker-machine' stored in the preferences
	 * @return the names of the existing Docker Machines
	 */
	public static String[] getNames(final String pathToDockerMachine) {
		return execute(pathToDockerMachine, new String[] { "ls", "-q" }); //$NON-NLS-1$ //$NON-NLS-2$
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
	 */
	public static String getHost(final String name,
			final String dockerMachineInstallDir,
			final String vmDriverInstallDir) {
		final String[] res = execute(dockerMachineInstallDir,
				new String[] { "url", name }, //$NON-NLS-1$
				vmDriverInstallDir);
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
	 */
	public static String getCertPath(final String name,
			final String pathToDockerMachine, final String vmDriverInstallDir) {
		final String[] envVariables = execute(pathToDockerMachine,
				new String[] { "env", name }, //$NON-NLS-1$
				vmDriverInstallDir);
		for (String envVariable : envVariables) {
			if (envVariable.contains("DOCKER_CERT_PATH")) { //$NON-NLS-1$
				// DOCKER_CERT_PATH="/path/to/cert-folder"
				return envVariable.split("=")[1].replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		return null;
	}

	/**
	 * Executes the command given in parameter
	 * 
	 * @param args
	 *            command arguments
	 * @return the lines read in the {@link Process}' {@link InputStream}
	 */
	private static String[] execute(final String dockerMachineInstallDir,
			final String[] args, final String... extraPaths) {
		try {
			final String[] command = new String[args.length + 1];
			command[0] = dockerMachineInstallDir
					+ (dockerMachineInstallDir.endsWith(File.separator) ? "" //$NON-NLS-1$
							: File.separator)
					+ "docker-machine";
			System.arraycopy(args, 0, command, 1, args.length);
			final ProcessBuilder processBuilder = new ProcessBuilder(command);
			final Map<String, String> environment = processBuilder
					.environment();
			final StringBuilder path = new StringBuilder(
					dockerMachineInstallDir);
			for (String extraPath : extraPaths) {
				path.append(File.pathSeparator).append(extraPath);
			}
			environment.put("PATH", path.toString());
			final Process p = processBuilder.start();
			p.waitFor();
			if (p.exitValue() == 0) {
				final List<String> result = new ArrayList<>();
				try (BufferedReader buff = new BufferedReader(
						new InputStreamReader(p.getInputStream()))) {
					String line;
					while ((line = buff.readLine()) != null) {
						result.add(line);
					}
				}
				return result.toArray(new String[0]);
			} else {
				final StringBuffer errorMessage = new StringBuffer();
				try (BufferedReader buff = new BufferedReader(
						new InputStreamReader(p.getErrorStream()))) {
					String line;
					while ((line = buff.readLine()) != null) {
						errorMessage.append(line).append('\n');
					}
				}
				Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						errorMessage.toString()));
			}
		} catch (IOException | InterruptedException e) {
			Activator.log(e);
		}
		return new String[0];
	}

}
