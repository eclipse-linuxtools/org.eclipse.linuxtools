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
package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.io.OutputStream;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerLoggingStatus;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class DisplayContainerLogCommandHandler extends AbstractHandler {

	private static final String CONTAINER_LOG_TITLE = "ContainerLog.title"; //$NON-NLS-1$
	private static final String ERROR_LOGGING_CONTAINER = "ContainerLogError.msg"; //$NON-NLS-1$

	private IDockerConnection connection;
	private IDockerContainer container;

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		List<IDockerContainer> selectedContainers = CommandUtils
				.getSelectedContainers(activePart);
		if (activePart instanceof DockerContainersView) {
			connection = ((DockerContainersView) activePart).getConnection();
		}
		if (selectedContainers.size() != 1 || connection == null)
			return null;
		container = selectedContainers.get(0);
		final String id = container.id();
		final String name = container.name();
		try {
			final RunConsole rc = RunConsole.findConsole(id, name);
			if (!rc.isAttached()) {
				rc.attachToConsole(connection);
			}
			if (rc != null) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						rc.setTitle(DVMessages.getFormattedString(
								CONTAINER_LOG_TITLE, name));
					}

				});
				OutputStream stream = rc
						.getOutputStream();
				// Only bother to ask for a log if
				// one isn't currently active
				EnumDockerLoggingStatus status = ((DockerConnection) connection)
						.loggingStatus(id);
				if (status != EnumDockerLoggingStatus.LOGGING_ACTIVE
						&& !((DockerConnection) connection)
								.getContainerInfo(id).config().tty()) {
					rc.clearConsole();
					((DockerConnection) connection).logContainer(id, stream);
				}
				rc.showConsole();
			}
		} catch (DockerException
				| InterruptedException e) {
			Display.getDefault().syncExec(
					new Runnable() {

						@Override
						public void run() {
							MessageDialog
							.openError(
									Display.getCurrent()
									.getActiveShell(),
									DVMessages
									.getFormattedString(
											ERROR_LOGGING_CONTAINER,
											id),
											e.getMessage());
				}
			});
		}
		return null;
	}


}
