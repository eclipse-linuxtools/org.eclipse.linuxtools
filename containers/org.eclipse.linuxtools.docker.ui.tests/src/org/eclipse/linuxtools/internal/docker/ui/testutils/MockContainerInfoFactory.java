/*******************************************************************************
 * Copyright (c) 2015, 2020 Red Hat.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.mandas.docker.client.messages.ContainerConfig;
import org.mandas.docker.client.messages.ContainerInfo;
import org.mandas.docker.client.messages.HostConfig;
import org.mandas.docker.client.messages.NetworkSettings;
import org.mandas.docker.client.messages.PortBinding;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * A factory for mock {@link ContainerInfo}s.
 */
public class MockContainerInfoFactory {

	public static Builder port(final String privatePort, final String hostIp, final String hostPort) {
		return new Builder().port(privatePort, hostIp, hostPort);
	}

	public static Builder link(final String link) {
		return new Builder().link(link);
	}

	public static Builder volume(final String volume) {
		return new Builder().volume(volume);
	}

	public static Builder networkMode(final String networkMode) {
		return new Builder().networkMode(networkMode);
	}

	public static Builder id(final String id) {
		return new Builder().id(id);
	}

	public static Builder image(final String image) {
		return new Builder().image(image);
	}

	public static Builder ipAddress(final String ipAddress) {
		return new Builder().ipAddress(ipAddress);
	}

	public static ContainerInfo build() {
		return new Builder().build();
	}

	public static Builder privilegedMode(boolean mode) {
		return new Builder().privilegedMode(mode);
	}

	public static Builder securityOpt(String profile) {
		return new Builder().securityOpt(profile);
	}

	public static Builder labels(ImmutableMap<String, String> labels) {
		return new Builder().labels(labels);
	}

	public static class Builder {

		private final ContainerInfo containerInfo;

		private ImmutableMap<String, String> labels;

		private ImmutableMap<String, List<PortBinding>> ports;

		private ImmutableList<String> links;

		private ImmutableList<String> volumes;

		private String networkMode;

		private String ipAddress;

		private Boolean privilegedMode;

		private ImmutableList<String> securityOpt;

		private Builder() {
			this.containerInfo = Mockito.mock(ContainerInfo.class, Mockito.RETURNS_DEEP_STUBS);
			Mockito.when(this.containerInfo.created()).thenReturn(new Date());
			Mockito.when(this.containerInfo.path()).thenReturn(null);
			Mockito.when(this.containerInfo.args()).thenReturn(null);
			Mockito.when(this.containerInfo.hostConfig()).thenReturn(null);
			Mockito.when(this.containerInfo.state()).thenReturn(null);
			Mockito.when(this.containerInfo.image()).thenReturn(null);
			Mockito.when(this.containerInfo.networkSettings()).thenReturn(null);
			Mockito.when(this.containerInfo.resolvConfPath()).thenReturn(null);
			Mockito.when(this.containerInfo.hostnamePath()).thenReturn(null);
			Mockito.when(this.containerInfo.hostsPath()).thenReturn(null);
			Mockito.when(this.containerInfo.name()).thenReturn(null);
			Mockito.when(this.containerInfo.driver()).thenReturn(null);
			Mockito.when(this.containerInfo.execDriver()).thenReturn(null);
			Mockito.when(this.containerInfo.processLabel()).thenReturn(null);
			Mockito.when(this.containerInfo.hostsPath()).thenReturn(null);
			Mockito.when(this.containerInfo.mountLabel()).thenReturn(null);
			Mockito.when(this.containerInfo.mounts()).thenReturn(null);

		}

		public Builder labels(ImmutableMap<String, String> labels) {
			this.labels = labels;
			return this;
		}

		public Builder privilegedMode(boolean mode) {
			this.privilegedMode = mode;
			return this;
		}

		public Builder ipAddress(String ipAddress) {
			this.ipAddress = ipAddress;
			return this;
		}

		public Builder id(String id) {
			Mockito.when(this.containerInfo.id()).thenReturn(id);
			return this;
		}

		public Builder link(final String link) {
			if (this.links == null) {
				this.links = ImmutableList.of();
			}

			ArrayList<String> tmp = new ArrayList<>(this.links);
			tmp.add(link);
			this.links = ImmutableList.copyOf(tmp);
			return this;
		}

		public Builder securityOpt(final String opt) {
			if (this.securityOpt == null) {
				this.securityOpt = ImmutableList.of();
			}

			ArrayList<String> tmp = new ArrayList<>(this.securityOpt);
			tmp.add(opt);
			this.securityOpt = ImmutableList.copyOf(tmp);
			return this;
		}

		public Builder volume(final String volume) {
			if (this.volumes == null) {
				this.volumes = ImmutableList.of();
			}

			ArrayList<String> tmp = new ArrayList<>(this.volumes);
			tmp.add(volume);
			this.volumes = ImmutableList.copyOf(tmp);
			return this;
		}

		public Builder networkMode(final String networkMode) {
			this.networkMode = networkMode;
			return this;
		}

		public Builder image(final String image) {
			Mockito.when(this.containerInfo.image()).thenReturn(image);
			return this;
		}

		public Builder port(final String privatePort, final String hostIp, final String hostPort) {
			if (this.ports == null) {
				this.ports = ImmutableMap.of();
			}
			final PortBinding binding = Mockito.mock(PortBinding.class);
			Mockito.when(binding.hostIp()).thenReturn(hostIp);
			Mockito.when(binding.hostPort()).thenReturn(hostPort);

			HashMap<String, List<PortBinding>> tmp = new HashMap<>(this.ports);
			tmp.put(privatePort, new ArrayList<>());
			this.ports = ImmutableMap.copyOf(tmp);
			ports.get(privatePort).add(binding);
			return this;
		}

		public ContainerInfo build() {
			final NetworkSettings networkSettings = Mockito.mock(NetworkSettings.class);
			Mockito.when(this.containerInfo.networkSettings()).thenReturn(networkSettings);
			Mockito.when(networkSettings.ports()).thenReturn(this.ports);
			Mockito.when(networkSettings.ipAddress()).thenReturn(this.ipAddress);
			final HostConfig hostConfig = Mockito.mock(HostConfig.class);
			Mockito.when(this.containerInfo.hostConfig()).thenReturn(hostConfig);
			Mockito.when(hostConfig.links()).thenReturn(this.links);
			Mockito.when(hostConfig.securityOpt()).thenReturn(this.securityOpt);
			Mockito.when(hostConfig.binds()).thenReturn(this.volumes);
			Mockito.when(hostConfig.networkMode()).thenReturn(this.networkMode);
			Mockito.when(hostConfig.privileged()).thenReturn(this.privilegedMode);
			final ContainerConfig containerConfig = Mockito.mock(ContainerConfig.class);
			Mockito.when(this.containerInfo.config()).thenReturn(containerConfig);
			Mockito.when(containerConfig.labels()).thenReturn(this.labels);
			return containerInfo;
		}
	}

}
