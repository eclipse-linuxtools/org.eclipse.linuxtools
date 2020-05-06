/*******************************************************************************
 * Copyright (c) 2020 Red Hat.
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

import org.eclipse.linuxtools.docker.core.IDockerBlkioWeightDevice;

import org.mandas.docker.client.messages.HostConfig.BlkioWeightDevice;

public class DockerBlkioWeightDevice implements IDockerBlkioWeightDevice {

	private final String path;
	private final Integer weight;

	public DockerBlkioWeightDevice(final BlkioWeightDevice blkioWeightDevice) {
		this.path = blkioWeightDevice.path();
		this.weight = blkioWeightDevice.weight();
	}

	private DockerBlkioWeightDevice(final Builder builder) {
		this.path = builder.path;
		this.weight = builder.weight;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public Integer weight() {
		return weight;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String path;
		private Integer weight;
	
		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder weight(Integer weight) {
			this.weight = weight;
			return this;
		}

		public IDockerBlkioWeightDevice build() {
			return new DockerBlkioWeightDevice(this);
		}
	}

}
