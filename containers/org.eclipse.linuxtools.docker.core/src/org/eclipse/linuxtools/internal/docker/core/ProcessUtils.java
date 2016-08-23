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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.DockerException;

/**
 * A utility class to run Java {@link Process}es such as {@code docker-machine}
 * or {@code docker-compose}.
 */
@Deprecated
public class ProcessUtils {

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
	public static boolean checkPathToCommand(final String path,
			final String cmdName) {
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
	public static FluentProcessBuilder processBuilder(final String baseCmdDir,
			final String cmdName, final String... args) throws DockerException {
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
			final boolean commandExists = checkPathToCommand(baseCmdDir,
					cmdName);
			if (!commandExists) {
				throw new DockerException(
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

		public AsyncProcessRunner outputListener(
				final Consumer<String> outputConsumer) {
			return new AsyncProcessRunner(this.processBuilder, outputConsumer);
		}

		public String[] start() {
			try {
				final Process p = processBuilder.start();
				p.waitFor();
				if (p.exitValue() == 0) {
					final List<String> result = new ArrayList<>();
					try (final InputStream inputStream = p.getInputStream();
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
					try (final InputStream errorStream = p.getErrorStream();
							final BufferedReader buff = new BufferedReader(
									new InputStreamReader(errorStream))) {
						String line;
						while ((line = buff.readLine()) != null) {
							errorMessage.append(line).append('\n'); // $NON-NLS-1$
						}
					}
					Activator.log(new Status(IStatus.WARNING,
							Activator.PLUGIN_ID,
							ProcessMessages.getFormattedString("Process_Error", //$NON-NLS-1$
									this.processBuilder.command().stream()
											.collect(Collectors.joining(" ")),
									p.exitValue(), errorMessage.toString())));
				}
			} catch (IOException | InterruptedException e) {
				Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						ProcessMessages.getFormattedString("Process_Exception", //$NON-NLS-1$
								this.processBuilder.command().stream()
										.collect(Collectors.joining(" ")),
								e.getMessage()),
						e));
			}
			return new String[0];
		}
	}

	/**
	 * Process executor
	 */
	public static class AsyncProcessRunner {

		private final ProcessBuilder processBuilder;

		private final Consumer<String> outputConsumer;

		public AsyncProcessRunner(final ProcessBuilder processBuilder,
				final Consumer<String> outputConsumer) {
			this.processBuilder = processBuilder;
			this.outputConsumer = outputConsumer;
		}

		/**
		 * Executes the {@link Process} and sends output lines from the standard
		 * {@code outputStream} and the {@code errorStream} to the
		 * {@code outputConsumer} provided in the constructor.
		 * 
		 * @return the {@link Process}
		 * @throws DockerException
		 *             if something wrong happened when the process was executed
		 */
		public Process start() throws DockerException {
			try {
				// we need to redirect the error stream into the standard output
				// stream
				processBuilder.redirectErrorStream(true);
				final Process process = processBuilder.start();
				try (final InputStream inputStream = process.getInputStream();
						final BufferedReader buff = new BufferedReader(
								new InputStreamReader(inputStream))) {
					String line;
					while ((line = buff.readLine()) != null) {
						this.outputConsumer.accept(line + '\n'); // $NON-NLS-1$
					}
				}

				return process;
			} catch (IOException e) {
				throw new DockerException(
						ProcessMessages.getFormattedString("Process_Exception", //$NON-NLS-1$
								this.processBuilder.command().stream()
										.collect(Collectors.joining(" ")),
								e.getMessage()),
						e);
			}
		}

	}

}
