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

package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
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
	 * @param connections
	 *            the connection to configure in the
	 *            {@link DockerConnectionManager} via a mocked
	 *            {@link IDockerConnectionStorageManager}
	 */
	public static void configureConnectionManager(final IDockerConnection... connections) {
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connections);
		configureConnectionManager(connectionStorageManager);
	}

	/**
	 * Configures the {@link DockerConnectionManager} with the given array of
	 * {@link IDockerConnection} (can be mocked) and refreshes the associated
	 * {@link DockerExplorerView}.
	 *
	 * @param connectionStorageManager
	 *            the {@link IDockerConnectionStorageManager} to use (can be
	 *            mocked)
	 */
	public static void configureConnectionManager(final IDockerConnectionStorageManager connectionStorageManager) {
		DockerConnectionManager.getInstance().setConnectionStorageManager(connectionStorageManager);
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		final SWTBotView dockerExplorerBotView = SWTUtils.getSWTBotView(bot, DockerExplorerView.VIEW_ID);
		final SWTBotView dockerContainersBotView = SWTUtils.getSWTBotView(bot, DockerContainersView.VIEW_ID);
		bot.getDisplay().syncExec(() -> {
			DockerConnectionManager.getInstance().reloadConnections();
			if (dockerExplorerBotView != null) {
				final DockerExplorerView dockerExplorerView = (DockerExplorerView) dockerExplorerBotView
						.getViewReference().getView(false);
				if (dockerExplorerView != null) {
					dockerExplorerView.getCommonViewer().refresh();
					dockerExplorerView.showConnectionsOrExplanations();
				}
			}
			if (dockerContainersBotView != null) {
				final DockerContainersView dockerContainersView = (DockerContainersView) dockerContainersBotView
						.getViewReference().getView(false);
				if (dockerContainersView != null) {
					dockerContainersView.getViewer().refresh();
				}
			}
		});
	}

}
