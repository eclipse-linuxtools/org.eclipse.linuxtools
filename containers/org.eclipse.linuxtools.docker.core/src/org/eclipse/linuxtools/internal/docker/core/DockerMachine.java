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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.Messages;
import org.eclipse.osgi.util.NLS;

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
	 * @return the lines read in the {@link Process}' {@link InputStream} or an
	 *         empty array if the {@code docker-machine} command could not be
	 *         found in the {@code PATH}.
	 */
	private static String[] execute(final String dockerMachineInstallDir,
			final String[] args, final String... extraPaths) {
		try {
			// check that the 'docker-machine' can be found in the given
			// 'dockerMachineInstallDir'
			final boolean dockerMachineCommandExists = checkPathToDockerMachine(
					dockerMachineInstallDir);
			if (!dockerMachineCommandExists) {
				// log a warning and exit
				Activator
						.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
								NLS.bind(
										Messages.Docker_Machine_Command_Not_Found,
										dockerMachineInstallDir)));
				return new String[0];
			}
			final String[] command = new String[args.length + 1];
			command[0] = Paths.get(dockerMachineInstallDir, "docker-machine").toString(); //$NON-NLS-1$
			System.arraycopy(args, 0, command, 1, args.length);
			final ProcessBuilder processBuilder = new ProcessBuilder(command);
			final Map<String, String> environment = processBuilder
					.environment();
			final StringBuilder path = new StringBuilder();
			for (String extraPath : extraPaths) {
				path.append(File.pathSeparator).append(extraPath);
			}
			String newEnvPath = environment.get("PATH") + path.toString(); //$NON-NLS-1$
			environment.put("PATH", newEnvPath); //$NON-NLS-1$

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

	/**
	 * Checks that the 'docker-machine' command exists in the given {@code path}
	 * 
	 * @param path
	 *            to path to use to look for the 'docker-machine' command
	 * @return <code>true</code> if the command was found, <code>false</code>
	 *         otherwise.
	 */
	public static boolean checkPathToDockerMachine(final String path) {
		for (String pathFragment : path.split(File.pathSeparator)) {
			final File pathToDockerMachine = new File(pathFragment,
					"docker-machine"); //$NON-NLS-1$
			if (pathToDockerMachine.exists()) {
				return true;
			}
		}
		return false;
	}

}
