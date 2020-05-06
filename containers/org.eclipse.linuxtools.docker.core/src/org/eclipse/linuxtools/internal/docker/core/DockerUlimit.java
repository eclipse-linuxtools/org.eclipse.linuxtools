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

import org.eclipse.linuxtools.docker.core.IDockerUlimit;

import org.mandas.docker.client.messages.HostConfig.Ulimit;

public class DockerUlimit implements IDockerUlimit {

	private final String name;
	private final Long soft;
	private final Long hard;

	public DockerUlimit(final Ulimit ulimit) {
		this.name = ulimit.name();
		this.soft = ulimit.soft();
		this.hard = ulimit.hard();
	}

	private DockerUlimit(final Builder builder) {
		this.name = builder.name;
		this.soft = builder.soft;
		this.hard = builder.hard;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Long soft() {
		return soft;
	}

	@Override
	public Long hard() {
		return hard;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String name;
		private Long soft;
		private Long hard;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder soft(Long soft) {
			this.soft = soft;
			return this;
		}

		public Builder hard(Long hard) {
			this.hard = hard;
			return this;
		}

		public IDockerUlimit build() {
			return new DockerUlimit(this);
		}

	}

}
