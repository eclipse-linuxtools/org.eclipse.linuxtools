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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostIp == null) ? 0 : hostIp.hashCode());
		result = prime * result
				+ ((hostPort == null) ? 0 : hostPort.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DockerPortBinding other = (DockerPortBinding) obj;
		if (hostIp == null) {
			if (other.hostIp != null)
				return false;
		} else if (!hostIp.equals(other.hostIp))
			return false;
		if (hostPort == null) {
			if (other.hostPort != null)
				return false;
		} else if (!hostPort.equals(other.hostPort))
			return false;
		return true;
	}

}
