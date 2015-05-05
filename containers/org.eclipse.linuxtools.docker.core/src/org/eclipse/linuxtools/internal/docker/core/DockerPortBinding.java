/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerPortBinding;

import com.spotify.docker.client.messages.PortBinding;

public class DockerPortBinding implements IDockerPortBinding {

	private final String hostIp;
	private final String hostPort;

	public DockerPortBinding(final PortBinding portBinding) {
		this.hostIp = portBinding.hostIp();
		this.hostPort = portBinding.hostPort();
	}

	public DockerPortBinding(final String hostIp, final String hostPort) {
		this.hostIp = hostIp;
		this.hostPort = hostPort;
	}

	@Override
	public String hostIp() {
		return hostIp;
	}

	@Override
	public String hostPort() {
		return hostPort;
	}

	@Override
	public String toString() {
		return "PortBinding: hostIp=" + hostIp() + " hostPort=" + hostPort()
				+ "\n";
	}

}
