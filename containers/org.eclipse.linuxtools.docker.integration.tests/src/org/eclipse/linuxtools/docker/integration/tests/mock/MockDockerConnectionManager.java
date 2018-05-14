/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests.mock;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionStorageManager;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerContainersTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;

public class MockDockerConnectionManager {

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
		DockerConnectionManager.getInstance().reloadConnections();

		DockerExplorerView de = new DockerExplorerView();
		de.open();
		de.refreshView();

		DockerImagesTab imageTab = new DockerImagesTab();
		imageTab.activate();
		imageTab.refresh();

		DockerContainersTab containerTab = new DockerContainersTab();
		containerTab.activate();
		containerTab.refresh();

	}

}
