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
		final DockerExplorerView dockerExplorerView = getDockerExplorerView(new SWTWorkbenchBot());
		if(dockerExplorerView != null) {
			SWTUtils.syncExec(() -> {
				DockerConnectionManager.getInstance().reloadConnections();
				dockerExplorerView.getCommonViewer().refresh();
				dockerExplorerView.showConnectionsOrExplanations();
			});
			SWTUtils.wait(1, TimeUnit.SECONDS);
		}
	}
	
	private static DockerExplorerView getDockerExplorerView(final SWTWorkbenchBot bot) {
		return bot.views().stream().filter(v -> v.getReference().getId().equals(DockerExplorerView.VIEW_ID))
				.map(viewBot -> (DockerExplorerView) (viewBot.getViewReference().getView(true))).findFirst()
				.orElse(null);
	}

}
