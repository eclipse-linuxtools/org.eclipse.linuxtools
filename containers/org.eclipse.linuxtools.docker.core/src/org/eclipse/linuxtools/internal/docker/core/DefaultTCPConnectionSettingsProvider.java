/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsProvider;

public class DefaultTCPConnectionSettingsProvider implements IDockerConnectionSettingsProvider {

	@Override
	public List<IDockerConnectionSettings> getConnectionSettings() {
		final TCPConnectionSettings tcp = new TCPConnectionSettings(
				"127.0.0.1:2375", null); //$NON-NLS-1$
		tcp.setName(tcp.getHost());
		DockerConnection conn = new DockerConnection.Builder().tcpConnection(tcp);
		try {
			conn.open(false);
			conn.close();
		} catch (DockerException e) {
			return null;
		}
		return Arrays.asList(new IDockerConnectionSettings[] { tcp });
	}

}
