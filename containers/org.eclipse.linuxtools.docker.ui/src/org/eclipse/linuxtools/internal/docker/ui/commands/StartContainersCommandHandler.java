/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getRunConsole;

import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;

/**
 * Command handler to start all the selected {@link IDockerContainer}
 * @author xcoulon
 *
 */
public class StartContainersCommandHandler extends BaseContainersCommandHandler {

	private static final String CONTAINERS_START_MSG = "ContainersStart.msg"; //$NON-NLS-1$
	private static final String CONTAINER_START_MSG = "ContainerStart.msg"; //$NON-NLS-1$
	private static final String CONTAINER_START_ERROR_MSG = "ContainerStartError.msg"; //$NON-NLS-1$

	@Override
	void executeInJob(final IDockerContainer container, final IDockerConnection connection) {
		try {
			final RunConsole console = getRunConsole(connection, container);
			if (console != null) {
				// if we are auto-logging, show the console
				console.showConsole();
				// Start the container
				((DockerConnection) connection).startContainer(container.id(), console.getOutputStream());
			} else {
				((DockerConnection) connection).startContainer(container.id(), null);
			}
			((DockerConnection) connection).getContainers(true);
		} catch (DockerException | InterruptedException e) {
			final String errorMessage = DVMessages.getFormattedString(CONTAINER_START_ERROR_MSG, container.id());
			openError(errorMessage, e);
		} 
	}

	@Override
	String getJobName(final List<IDockerContainer> selectedContainers) {
		return DVMessages.getString(CONTAINERS_START_MSG);
	}

	@Override
	String getTaskName(final IDockerContainer container) {
		return DVMessages.getFormattedString(CONTAINER_START_MSG, container.name());
	}
}
