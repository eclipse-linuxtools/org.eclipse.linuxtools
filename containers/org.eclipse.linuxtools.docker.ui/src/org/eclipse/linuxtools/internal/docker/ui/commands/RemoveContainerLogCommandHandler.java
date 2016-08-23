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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.internal.docker.ui.consoles.RunConsole;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
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
					"org.eclipse.tm.terminal.connector.streams.launcher.streams");
			properties.put(
					ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
					"org.eclipse.tm.terminal.connector.streams.StreamsConnector");
			properties.put(ITerminalsConnectorConstants.PROP_TITLE,
					info.name());
			ITerminalService service = TerminalServiceFactory.getService();
			service.closeConsole(properties, null);
			return null;
		}
		final RunConsole rc = RunConsole.findConsole(container);
		if (rc != null) {
			RunConsole.removeConsole(rc);
		}
		return null;
	}

}
