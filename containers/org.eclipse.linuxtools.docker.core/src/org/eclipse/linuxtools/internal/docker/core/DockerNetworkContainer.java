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

import org.eclipse.linuxtools.docker.core.IDockerNetworkContainer;

import com.spotify.docker.client.messages.Network;

public class DockerNetworkContainer implements IDockerNetworkContainer {

	private String endpointId;
	private String macAddress;
	private String ipv4address;
	private String ipv6address;

	public DockerNetworkContainer(final Network.Container container) {
		this.endpointId = container.endpointId();
		this.macAddress = container.macAddress();
		this.ipv4address = container.ipv4address();
		this.ipv6address = container.ipv6address();
	}

	@Override
	public String endpointId() {
		return endpointId;
	}

	@Override
	public String macAddress() {
		return macAddress;
	}

	@Override
	public String ipv4address() {
		return ipv4address;
	}

	@Override
	public String ipv6address() {
		return ipv6address;
	}

}
