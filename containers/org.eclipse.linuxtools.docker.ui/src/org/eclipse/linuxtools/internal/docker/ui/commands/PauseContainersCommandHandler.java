/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;

/**
 * Command handler to pause all the selected {@link IDockerContainer}
 *
 */
public class PauseContainersCommandHandler extends BaseContainersCommandHandler {

	private static final String CONTAINERS_PAUSE_MSG = "ContainersPause.msg"; //$NON-NLS-1$
	private static final String CONTAINER_PAUSE_MSG = "ContainerPause.msg"; //$NON-NLS-1$
	private static final String CONTAINER_PAUSE_ERROR_MSG = "ContainerPauseError.msg"; //$NON-NLS-1$

	@Override
	void executeInJob(final IDockerContainer container, final IDockerConnection connection) {
		try {
			connection.pauseContainer(container.id());
		} catch (DockerException | InterruptedException e) {
			final String errorMessage = DVMessages.getFormattedString(CONTAINER_PAUSE_ERROR_MSG, container.id());
			openError(errorMessage, e);
		} 
	}

	@Override
	String getJobName(final List<IDockerContainer> selectedContainers) {
		return DVMessages.getString(CONTAINERS_PAUSE_MSG);
	}

	@Override
	String getTaskName(final IDockerContainer container) {
		return DVMessages.getFormattedString(CONTAINER_PAUSE_MSG, container.name());
	}

}
