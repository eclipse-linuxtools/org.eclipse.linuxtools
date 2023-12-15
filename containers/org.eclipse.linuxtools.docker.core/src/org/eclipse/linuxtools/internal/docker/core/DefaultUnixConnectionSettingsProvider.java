/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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
package org.eclipse.linuxtools.internal.docker.core;

import java.io.File;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettingsProvider;

public class DefaultUnixConnectionSettingsProvider
		implements IDockerConnectionSettingsProvider {

	@Override
	public List<IDockerConnectionSettings> getConnectionSettings() {
		final Path unixSocketPath = Path.of("/var/run/docker.sock"); //$NON-NLS-1$
		File unixSocketFile = unixSocketPath.toFile();
		if (unixSocketFile.exists() && unixSocketFile.canRead()
				&& unixSocketFile.canWrite()) {
			final UnixDomainSocketAddress address = UnixDomainSocketAddress
					.of(unixSocketPath);
			try (final SocketChannel channel = SocketChannel.open(address)) {
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
