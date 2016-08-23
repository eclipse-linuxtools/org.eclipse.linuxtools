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

package org.eclipse.linuxtools.internal.docker.ui.consoles;

import java.util.stream.Stream;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;

/**
 * A utility class for the {@code Docker Compose} {@link MessageConsole}.
 */
public class DockerComposeConsoleUtils {

	/**
	 * The constant to store the working directory for a Docker Compose process
	 * execution.
	 */
	public static final String WORKING_DIR = "org.eclipse.linuxtools.internal.docker.ui.consoles.DockerComposeConsole.workingDir"; //$NON-NLS-1$

	/**
	 * The constant to store the name of the connection associated with a Docker
	 * Compose process execution.
	 */
	public static final String DOCKER_CONNECTION = "org.eclipse.linuxtools.internal.docker.ui.consoles.DockerComposeConsole.dockerConnection"; //$NON-NLS-1$

	private DockerComposeConsoleUtils() {
	}

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} that is used to run the
	 *            {@code docker-compose} command.
	 *
	 * @param workingDir
	 *            The working directory in which the {@code docker-compose}
	 *            command is executed. <code>null</code>.
	 * @return A console instance or <code>null</code> if the given workingDir
	 *         was <code>null</code>.
	 */
	public static DockerComposeConsole findConsole(
			final IDockerConnection connection, final String workingDir) {
		if (workingDir == null) {
			return null;
		}
		return Stream
				.of(ConsolePlugin.getDefault().getConsoleManager()
						.getConsoles())
				.filter(c -> c.getType() != null && c.getType()
						.equals(DockerComposeConsole.CONSOLE_TYPE))
				.map(c -> (DockerComposeConsole) c)
				.filter(c -> c.getAttribute(DOCKER_CONNECTION) != null
						&& c.getAttribute(DOCKER_CONNECTION).equals(connection)
						&& c.getAttribute(WORKING_DIR) != null
						&& c.getAttribute(WORKING_DIR).equals(workingDir))
				.findFirst()
				// if no match found, create and register a new console
				.orElseGet(() -> {
					final DockerComposeConsole dockerComposeConsole = new DockerComposeConsole(
							connection, workingDir);
					dockerComposeConsole.setAttribute(DOCKER_CONNECTION,
							connection);
					dockerComposeConsole.setAttribute(WORKING_DIR, workingDir);
					ConsolePlugin.getDefault().getConsoleManager().addConsoles(
							new IConsole[] { dockerComposeConsole });
					return dockerComposeConsole;
				});
	}

}
