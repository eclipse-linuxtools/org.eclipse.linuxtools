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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionSettingsFinder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Clears the connection manager after each test.
 */
public class ClearConnectionManagerRule implements AfterEachCallback {

	@Override
	public void afterEach(final ExtensionContext context) {
		removeAllConnections(DockerConnectionManager.getInstance());
		DockerConnectionManagerUtils.configureConnectionManager();
		DockerConnectionManager.getInstance().setConnectionSettingsFinder(new DefaultDockerConnectionSettingsFinder());
	}

	/**
	 * Removes all connections in the given {@link DockerConnectionManager}
	 *
	 * @param dockerConnectionManager
	 */
	public static void removeAllConnections(final DockerConnectionManager dockerConnectionManager) {
		final List<IDockerConnection> allConnections = new ArrayList<>(dockerConnectionManager.getAllConnections());
		allConnections.forEach(c -> dockerConnectionManager.removeConnection(c));
	}

}