/*******************************************************************************
 * Copyright (c) 2019, 2020 Red Hat.
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

import org.eclipse.linuxtools.docker.core.IDockerDevice;

import org.mandas.docker.client.messages.Device;

public class DockerDevice implements IDockerDevice {

	private final String pathOnHost;
	private final String pathInContainer;
	private final String cgroupPermissions;

	public DockerDevice(final Device device) {
		this.pathOnHost = device.pathOnHost();
		this.pathInContainer = device.pathInContainer();
		this.cgroupPermissions = device.cgroupPermissions();
	}

	private DockerDevice(final Builder builder) {
		this.pathOnHost = builder.pathOnHost;
		this.pathInContainer = builder.pathInContainer;
		this.cgroupPermissions = builder.cgroupPermissions;
	}
	
	@Override
	public String pathOnHost() {
		return pathOnHost;
	}

	@Override
	public String pathInContainer() {
		return pathInContainer;
	}

	@Override
	public String cgroupPermissions() {
		return cgroupPermissions;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		
		private String pathOnHost;
		private String pathInContainer;
		private String cgroupPermissions;
		
		public Builder pathOnHost(String pathOnHost) {
			this.pathOnHost = pathOnHost;
			return this;
		}
		
		public Builder pathInContainer(String pathInContainer) {
			this.pathInContainer = pathInContainer;
			return this;
		}
		
		public Builder cgroupsPermission(String cgroupsPermission) {
			this.cgroupPermissions = cgroupsPermission;
			return this;
		}
		
		public IDockerDevice build() {
			return new DockerDevice(this);
		}

	}

}
