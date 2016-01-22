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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;

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
	 */
	public static void configureConnectionManager(
			final IDockerConnection... connections) {
		DockerConnectionManager.getInstance()
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.providing(connections));
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		final DockerExplorerView dockerExplorerView = getView(bot, DockerExplorerView.VIEW_ID);
		final DockerContainersView dockerContainersView = getView(bot, DockerContainersView.VIEW_ID);
		SWTUtils.syncExec(() -> {
			DockerConnectionManager.getInstance().reloadConnections();
			if (dockerExplorerView != null) {
				dockerExplorerView.getCommonViewer().refresh();
				dockerExplorerView.showConnectionsOrExplanations();
			}
			if (dockerContainersView != null) {
				dockerContainersView.getViewer().refresh();
			}
		});
		SWTUtils.wait(1, TimeUnit.SECONDS);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getView(final SWTWorkbenchBot bot, final String viewId) {
		final Optional<SWTBotView> viewBot = bot.views().stream().filter(v -> v.getReference().getId().equals(viewId))
				.findFirst();
		if(viewBot.isPresent()) {
			return UIThreadRunnable.syncExec(() ->  (T) (viewBot.get().getViewReference().getView(true)));
		}
		return null;
	}

}
