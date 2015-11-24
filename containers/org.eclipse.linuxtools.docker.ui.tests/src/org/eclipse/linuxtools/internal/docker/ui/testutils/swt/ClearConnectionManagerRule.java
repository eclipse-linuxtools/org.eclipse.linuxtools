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

package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import java.util.stream.Stream;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.junit.rules.ExternalResource;

/**
 * Clears the connection manager after each test.
 */
public class ClearConnectionManagerRule extends ExternalResource {

	@Override
	protected void after() {
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		final SWTBotView dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		final DockerExplorerView dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference()
				.getView(true));
		SWTUtils.syncExec(() -> {
			Stream.of(DockerConnectionManager.getInstance().getConnections())
					.forEach(c -> DockerConnectionManager.getInstance().removeConnection(c));
			dockerExplorerView.getCommonViewer().refresh(true);
			dockerExplorerView.showConnectionsOrExplanations();
		});
		DockerConnectionManager.getInstance().setConnectionSettingsFinder(new DefaultDockerConnectionSettingsFinder());
	}
}
