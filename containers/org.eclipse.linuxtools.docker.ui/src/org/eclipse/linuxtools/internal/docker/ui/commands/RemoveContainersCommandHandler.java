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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.internal.docker.ui.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.swt.widgets.Display;

/**
 * Command handler to kill all the selected {@link IDockerContainer}
 * 
 * @author xcoulon
 *
 */
public class RemoveContainersCommandHandler extends BaseContainersCommandHandler {

	private static final String CONTAINERS_REMOVE_MSG = "ContainersRemove.msg"; //$NON-NLS-1$
	private static final String CONTAINER_REMOVE_MSG = "ContainerRemove.msg"; //$NON-NLS-1$
	private static final String CONTAINER_REMOVE_ERROR_MSG = "ContainerRemoveError.msg"; //$NON-NLS-1$
	private static final String CONTAINER_REMOVE_CONFIRM = "ContainerRemoveConfirm.msg"; //$NON-NLS-1$
	private static final String CONTAINER_REMOVE_LIST = "ContainerRemoveList.msg"; //$NON-NLS-1$

	@Override
	void executeInJob(final IDockerContainer container,
			final IDockerConnection connection) {
		try {
			connection.removeContainer(container.id());
			RunConsole rc = RunConsole.findConsole(container.id());
			if (rc != null)
				RunConsole.removeConsole(rc);
		} catch (DockerException | InterruptedException e) {
			final String errorMessage = DVMessages.getFormattedString(
					CONTAINER_REMOVE_ERROR_MSG, container.name());
			openError(errorMessage, e);
		} finally {
			// always get images as we sometimes get errors on intermediate
			// images
			// being removed but we will remove some top ones successfully
			connection.getContainers(true);
		}
	}

	@Override
	String getJobName(final List<IDockerContainer> selectedContainers) {
		return DVMessages.getString(CONTAINERS_REMOVE_MSG);
	}

	@Override
	String getTaskName(final IDockerContainer image) {
		return DVMessages.getFormattedString(CONTAINER_REMOVE_MSG, image.name());
	}

	private class DialogResponse {
		private boolean response;

		public boolean getResponse() {
			return response;
		}

		public void setResponse(boolean value) {
			response = value;
		}
	}

	@Override
	boolean confirmed(List<IDockerContainer> selectedContainers) {
		// ask for confirmation before deleting images
		List<String> containersToRemove = new ArrayList<>();
		for (IDockerContainer container : selectedContainers) {
			containersToRemove.add(container.name());
		}
		final List<String> names = containersToRemove;
		final DialogResponse response = new DialogResponse();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				boolean result = MessageDialog
						.openConfirm(Display.getDefault().getActiveShell(),
								DVMessages.getString(CONTAINER_REMOVE_CONFIRM),
								DVMessages.getFormattedString(
										CONTAINER_REMOVE_LIST,
										names.toString()));
				response.setResponse(result);
			}
		});
		return response.getResponse();
	}

}
