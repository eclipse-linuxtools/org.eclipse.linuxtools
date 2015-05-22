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

import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Port mapping for {@link IDockerContainer}
 * @author xcoulon
 *
 */
public class DockerPortMapping implements IDockerPortMapping {

	private final int privatePort;

	private final int publicPort;
	
	private final String type;

	private final String ip;

	/**
	 * Full constructor
	 * 
	 * @param privatePort
	 *            private port
	 * @param publicPort
	 *            public port
	 * @param type
	 *            of port
	 * @param ip
	 *            of port
	 */
	@JsonCreator
	public DockerPortMapping(@JsonProperty("privatePort") int privatePort, @JsonProperty("publicPort") int publicPort, @JsonProperty("type") String type, @JsonProperty("ip") String ip) {
		this.privatePort = privatePort;
		this.publicPort = publicPort;
		this.type = type;
		this.ip = ip;
	}

	@Override
	public int getPrivatePort() {
		return privatePort;
	}

	@Override
	public int getPublicPort() {
		return publicPort;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getIp() {
		return ip;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + privatePort;
		result = prime * result + publicPort;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		DockerPortMapping other = (DockerPortMapping) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (privatePort != other.privatePort)
			return false;
		if (publicPort != other.publicPort)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
	
	
}
