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

import org.eclipse.linuxtools.docker.core.IDockerBlkioDeviceRate;

import org.mandas.docker.client.messages.HostConfig.BlkioDeviceRate;

public class DockerBlkioDeviceRate implements IDockerBlkioDeviceRate {

	private final String path;
	private final Integer rate;

	public DockerBlkioDeviceRate(final BlkioDeviceRate blkioDeviceRate) {
		this.path = blkioDeviceRate.path();
		this.rate = blkioDeviceRate.rate();
	}

	private DockerBlkioDeviceRate(final Builder builder) {
		this.path = builder.path;
		this.rate = builder.rate;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public Integer rate() {
		return rate;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String path;
		private Integer rate;

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder rate(Integer rate) {
			this.rate = rate;
			return this;
		}

		public IDockerBlkioDeviceRate build() {
			return new DockerBlkioDeviceRate(this);
		}
	}

}
