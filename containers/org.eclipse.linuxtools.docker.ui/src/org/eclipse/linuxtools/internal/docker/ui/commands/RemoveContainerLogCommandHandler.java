/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.consoles.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.terminal.view.core.ITerminalService;
import org.eclipse.terminal.view.core.ITerminalsConnectorConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveContainerLogCommandHandler extends AbstractHandler {

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
		IDockerContainerInfo info = connection.getContainerInfo(container.id());
		if (info.config().tty()) {
			Map<String, Object> properties = new HashMap<>();
			properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID,
					"org.eclipse.terminal.connector.streams.launcher.streams");
			properties.put(
					ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
					"org.eclipse.terminal.connector.streams.StreamsConnector");
			properties.put(ITerminalsConnectorConstants.PROP_TITLE,
					info.name());
			ITerminalService service = Activator.getTerminalService();
			service.closeConsole(properties);
			return null;
		}
		final RunConsole rc = RunConsole.findConsole(container);
		if (rc != null) {
			RunConsole.removeConsole(rc);
		}
		return null;
	}

}
