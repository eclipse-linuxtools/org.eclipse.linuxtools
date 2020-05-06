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

import org.eclipse.linuxtools.docker.core.IDockerRestartPolicy;

import org.mandas.docker.client.messages.HostConfig.RestartPolicy;

public class DockerRestartPolicy implements IDockerRestartPolicy {

	private final String name;
	private final Integer maxRetryCount;

	public DockerRestartPolicy(final RestartPolicy policy) {
		this.name = policy.name();
		this.maxRetryCount = policy.maxRetryCount();
	}

	private DockerRestartPolicy(final Builder builder) {
		this.name = builder.name;
		this.maxRetryCount = builder.maxRetryCount;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Integer maxRetryCount() {
		return maxRetryCount;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String name;
		private Integer maxRetryCount;

		public Builder pathOnHost(String name) {
			this.name = name;
			return this;
		}

		public Builder maxRetryCount(Integer maxRetryCount) {
			this.maxRetryCount = maxRetryCount;
			return this;
		}

		public IDockerRestartPolicy build() {
			return new DockerRestartPolicy(this);
		}

	}

}
