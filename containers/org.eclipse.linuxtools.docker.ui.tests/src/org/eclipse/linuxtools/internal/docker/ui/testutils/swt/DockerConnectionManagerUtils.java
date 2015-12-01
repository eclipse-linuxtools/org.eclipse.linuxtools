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

import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;

/**
 * 
 */
public class DockerConnectionManagerUtils {

	/**
	 * Configures the {@link DockerConnectionManager} with the given array of
	 * {@link IDockerConnection} (can be mocked) and refreshes the associated
	 * {@link DockerExplorerView}.
	 * 
	 * @param connections the connection to configure in the {@link DockerConnectionManager} via a mocked {@link IDockerConnectionStorageManager}
	 * @throws InterruptedException
	 */
	public static void configureConnectionManager(
			final IDockerConnection... connections) throws InterruptedException {
		DockerConnectionManager.getInstance()
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.providing(connections));
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		final SWTBotView dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		if(dockerExplorerViewBot != null) {
			final DockerExplorerView dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
			SWTUtils.syncExec(() -> {
				DockerConnectionManager.getInstance().reloadConnections();
				dockerExplorerView.getCommonViewer().refresh();
				dockerExplorerView.showConnectionsOrExplanations();
			});
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		}
	}
}
