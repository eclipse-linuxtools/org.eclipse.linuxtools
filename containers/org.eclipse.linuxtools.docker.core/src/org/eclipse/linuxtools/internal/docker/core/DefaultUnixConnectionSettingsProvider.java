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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsProvider;

import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

public class DefaultUnixConnectionSettingsProvider implements IDockerConnectionSettingsProvider {

	@Override
	public List<IDockerConnectionSettings> getConnectionSettings() {
		final File unixSocketFile = new File("/var/run/docker.sock"); //$NON-NLS-1$
		if (unixSocketFile.exists() && unixSocketFile.canRead()
				&& unixSocketFile.canWrite()) {
			final UnixSocketAddress address = new UnixSocketAddress(
					unixSocketFile);
			try (final UnixSocketChannel channel = UnixSocketChannel
					.open(address)) {
				// assume socket works
				final UnixSocketConnectionSettings socket = new UnixSocketConnectionSettings(
						DefaultDockerConnectionSettingsFinder.Defaults.DEFAULT_UNIX_SOCKET_PATH);
				socket.setName(socket.getPath());
				return Arrays.asList(socket);
			} catch (IOException e) {
				// do nothing, just assume socket did not work.
			}
		}
		return Collections.emptyList();
	}

}
