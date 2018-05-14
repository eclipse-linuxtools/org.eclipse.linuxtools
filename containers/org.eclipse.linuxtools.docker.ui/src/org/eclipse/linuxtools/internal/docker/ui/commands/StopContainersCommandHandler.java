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
 * Command handler to stop all the selected {@link IDockerContainer}
 * 
 * @author xcoulon
 *
 */
public class StopContainersCommandHandler extends BaseContainersCommandHandler {

	private static final String CONTAINERS_STOP_MSG = "ContainersStop.msg"; //$NON-NLS-1$
	private static final String CONTAINER_STOP_MSG = "ContainerStop.msg"; //$NON-NLS-1$
	private static final String CONTAINER_STOP_ERROR_MSG = "ContainerStopError.msg"; //$NON-NLS-1$

	@Override
	void executeInJob(final IDockerContainer container, final IDockerConnection connection) {
		try {
			connection.stopContainer(container.id());
			connection.getContainers(true);
		} catch (DockerException | InterruptedException e) {
			final String errorMessage = DVMessages.getFormattedString(CONTAINER_STOP_ERROR_MSG, container.id());
			openError(errorMessage, e);
		} 
	}

	@Override
	String getJobName(final List<IDockerContainer> selectedContainers) {
		return DVMessages.getString(CONTAINERS_STOP_MSG);
	}

	@Override
	String getTaskName(final IDockerContainer container) {
		return DVMessages.getFormattedString(CONTAINER_STOP_MSG, container.name());
	}
}
