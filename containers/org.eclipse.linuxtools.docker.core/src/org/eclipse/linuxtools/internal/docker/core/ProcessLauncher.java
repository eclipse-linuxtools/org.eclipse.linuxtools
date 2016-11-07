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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.DockerProcessException;

/**
 * A utility class to run Java {@link Process} such as {@code docker-machine} or
 * {@code docker-compose}.
 */
public class ProcessLauncher {

	public static final String COMMAND_LINE = "org.eclipse.linuxtools.docker.compose.commandLine";

	/**
	 * Checks that the given {@code cmdName} exists in the given {@code path}
	 * 
	 * @param path
	 *            the system {@code PATH} to use to look for the command
	 * @param cmdName
	 *            the name of the command in the given {@code cmdDir}
	 * @return <code>true</code> if the command was found, <code>false</code>
	 *         otherwise.
	 */
	public boolean checkPathToCommand(final String path, final String cmdName) {
		return Stream.of(path.split(File.pathSeparator))
				.map(pathFragment -> new File(pathFragment, cmdName))
				.anyMatch(fullPath -> fullPath.exists());
	}

	/**
	 * Entry method to run a command.
	 * 
	 * @param baseCmdDir
	 *            the base directory containing the command to run
	 * @param cmdName
	 *            the name of the command to run
	 * @throws DockerException
	 *             if the {@code baseCmdDir} did not contain the specific
	 *             {@code cmdName}
	 */
	public FluentProcessBuilder processBuilder(final String baseCmdDir,
			final String cmdName, final String[] args) throws DockerException {
		return new FluentProcessBuilder(baseCmdDir, cmdName, args);
	}

	public static class FluentProcessBuilder {

		/** the {@link ProcessBuilder} to use to run the command. */
		private final ProcessBuilder processBuilder;

		/**
		 * Constructor.
		 * 
		 * @param baseCmdDir
		 *            the base directory containing the command to run
		 * @param cmdName
		 *            the name of the command to run
		 * @throws DockerException
		 *             if the {@code baseCmdDir} did not contain the specific
		 *             {@code cmdName}
		 */
		private FluentProcessBuilder(final String baseCmdDir,
				final String cmdName, final String[] args)
				throws DockerException {
			// check that the 'cmdName' can be found in the given
			// 'baseCmdDir'
			final boolean commandExists = new ProcessLauncher()
					.checkPathToCommand(baseCmdDir, cmdName);
			if (!commandExists) {
				throw new DockerProcessException(
						ProcessMessages.getFormattedString("Command_Not_Found", //$NON-NLS-1$
								baseCmdDir, cmdName));
			}
			final String[] command = new String[args.length + 1];
			command[0] = Paths.get(baseCmdDir, cmdName).toString();
			System.arraycopy(args, 0, command, 1, args.length);
			this.processBuilder = new ProcessBuilder(command);
		}

		public FluentProcessBuilder extraPath(final String... extraPaths) {
			final Map<String, String> environment = processBuilder
					.environment();
			final StringBuilder path = new StringBuilder();
			for (String extraPath : extraPaths) {
				path.append(File.pathSeparator).append(extraPath);
			}
			final String newEnvPath = environment.get("PATH") //$NON-NLS-1$
					+ path.toString();
			environment.put("PATH", newEnvPath); //$NON-NLS-1$
			return this;
		}

		public FluentProcessBuilder workingDir(final String workingDir) {
			this.processBuilder.directory(new File(workingDir));
			return this;
		}

		public FluentProcessBuilder redirectErrorStream(
				boolean redirectErrorStream) {
			this.processBuilder.redirectErrorStream(redirectErrorStream);
			return this;
		}

		public String getCommand() {
			return this.processBuilder.command().stream()
					.collect(Collectors.joining(" "));
		}

		/**
		 * Starts the process from the current settings.
		 * 
		 * @return the {@link Process} that was started by this process builder
		 * @throws DockerException
		 *             if an error occurred
		 */
		public Process start() throws DockerException {
			try {
				return this.processBuilder.start();
			} catch (Throwable e) {
				throw new DockerException(ProcessMessages
						.getFormattedString("Process_Start_Exception", //$NON-NLS-1$
								processBuilder.command().stream()
										.collect(Collectors.joining(" ")),
								e.getMessage()),
						e);
			}
		}

		/**
		 * Starts the {@link Process} from the current settings and returns the
		 * output.
		 * 
		 * @return the process output once it has completed.
		 * @throws DockerException
		 *             if an error occurred
		 */
		public String[] startAndGetResult() throws DockerException {
			final Process process = start();
			return extractResult(process);
		}

		private String[] extractResult(final Process process) {
			try {
			process.waitFor();
			if (process.exitValue() == 0) {
				final List<String> result = new ArrayList<>();
				try (final InputStream inputStream = process.getInputStream();
						final BufferedReader buff = new BufferedReader(
								new InputStreamReader(inputStream))) {
					String line;
					while ((line = buff.readLine()) != null) {
						result.add(line);
					}
				}
				return result.toArray(new String[0]);
			} else {
				final StringBuffer errorMessage = new StringBuffer();
				try (final InputStream errorStream = process.getErrorStream();
						final BufferedReader buff = new BufferedReader(
								new InputStreamReader(errorStream))) {
					String line;
					while ((line = buff.readLine()) != null) {
						errorMessage.append(line).append('\n'); // $NON-NLS-1$
					}
				}
				Activator.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
						ProcessMessages.getFormattedString("Process_Error", //$NON-NLS-1$
											this.getCommand(),
								process.exitValue(), errorMessage.toString())));
			}
			} catch (IOException | InterruptedException e) {
				Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						ProcessMessages.getFormattedString("Process_Exception", //$NON-NLS-1$
								this.getCommand(), e.getMessage()),
						e));
			}
			return new String[0];
		}
	}

}