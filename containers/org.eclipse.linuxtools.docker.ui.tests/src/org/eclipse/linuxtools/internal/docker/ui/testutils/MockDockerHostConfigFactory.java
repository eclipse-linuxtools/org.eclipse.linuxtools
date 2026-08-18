/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;

/**
 * A factory for {@link IDockerHostConfig}s.
 * <p>
 * This builds a real {@link DockerHostConfig} rather than a Mockito mock,
 * because consumers such as
 * {@code LaunchConfigurationUtils#createRunImageLaunchConfiguration} downcast
 * {@link IDockerHostConfig} to {@link DockerHostConfig} to reach
 * {@code readonlyRootfs()}, which a mocked interface cannot satisfy.
 */
public class MockDockerHostConfigFactory {

	public static Builder publishAllPorts(final boolean publishAllPorts) {
		return new Builder().publishAllPorts(publishAllPorts);
	}

	public static Builder networkMode(final String networkMode) {
		return new Builder().networkMode(networkMode);
	}

	public static Builder privileged(final boolean privileged) {
		return new Builder().privileged(privileged);
	}

	public static Builder readonlyRootfs(final boolean readonlyRootfs) {
		return new Builder().readonlyRootfs(readonlyRootfs);
	}

	public static Builder securityOpt(final String... securityOpt) {
		return new Builder().securityOpt(securityOpt);
	}

	public static class Builder {

		private final DockerHostConfig.Builder hostConfig;

		private Builder() {
			// consumers iterate these without null-checking, and the mock this
			// factory used to return handed back empty collections via deep stubs
			this.hostConfig = DockerHostConfig.builder()
					.binds(List.of())
					.volumesFrom(List.of())
					.links(List.of())
					.securityOpt(List.of())
					.capDrop(List.of())
					.tmpfs(Map.of());
		}

		public Builder publishAllPorts(final boolean publishAllPorts) {
			this.hostConfig.publishAllPorts(publishAllPorts);
			return this;
		}

		public Builder networkMode(final String networkMode) {
			this.hostConfig.networkMode(networkMode);
			return this;
		}

		public Builder privileged(final boolean privileged) {
			this.hostConfig.privileged(privileged);
			return this;
		}

		public Builder readonlyRootfs(final boolean readonlyRootfs) {
			this.hostConfig.readonlyRootfs(readonlyRootfs);
			return this;
		}

		public Builder tmpfs(final Map<String, String> tmpfs) {
			this.hostConfig.tmpfs(tmpfs);
			return this;
		}

		public Builder securityOpt(final String... securityOpt) {
			this.hostConfig.securityOpt(securityOpt);
			return this;
		}

		public Builder capDrop(final String... capDrop) {
			this.hostConfig.capDrop(capDrop);
			return this;
		}

		public Builder capDrop(final List<String> capDrop) {
			this.hostConfig.capDrop(capDrop);
			return this;
		}

		public IDockerHostConfig build() {
			return this.hostConfig.build();
		}
	}

}
