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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.linuxtools.docker.core.AbstractRegistry;

public class RegistryInfo extends AbstractRegistry {

	/** the registry address. */
	private final String serverAddress;

	/** flag to indicate if the registry is the Docker Hub platform. */
	private final boolean dockerHubRegistry;

	public RegistryInfo(final String serverAddress,
			final boolean dockerHubRegistry) {
		this.serverAddress = serverAddress;
		this.dockerHubRegistry = dockerHubRegistry;
	}

	@Override
	public String getServerAddress() {
		return serverAddress;
	}

	@Override
	public String getServerHost() {
		try {
			final URL serverAddress = new URL(getServerAddress());
			final String serverHost = serverAddress.getHost()
					+ (serverAddress.getPort() != -1
					? ":" + serverAddress.getPort() : ""); //$NON-NLS-1$
			return serverHost;
		} catch (MalformedURLException e) {
			// assume there was no scheme, so just use the plain
			// server address
			return getServerAddress();
		}
	}

	@Override
	public boolean isDockerHubRegistry() {
		return this.dockerHubRegistry;
	}

	@Override
	public boolean isAuthProvided() {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (dockerHubRegistry ? 1231 : 1237);
		result = prime * result
				+ ((serverAddress == null) ? 0 : serverAddress.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RegistryInfo other = (RegistryInfo) obj;
		if (dockerHubRegistry != other.dockerHubRegistry) {
			return false;
		}
		if (serverAddress == null) {
			if (other.serverAddress != null) {
				return false;
			}
		} else if (!serverAddress.equals(other.serverAddress)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return serverAddress;
	}

}
