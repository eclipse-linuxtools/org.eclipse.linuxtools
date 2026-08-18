/*******************************************************************************
 * Copyright (c) 2015, 2021 Red Hat.
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
import java.util.Map;

import org.mandas.docker.client.messages.ContainerConfig;
import org.mandas.docker.client.messages.ContainerInfo;
import org.mandas.docker.client.messages.HostConfig;
import org.mandas.docker.client.messages.NetworkSettings;
import org.mandas.docker.client.messages.PortBinding;

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

	public static Builder labels(Map<String, String> labels) {
		return new Builder().labels(labels);
	}

	public static class Builder {

		private String id;

		private String image;

		private Map<String, String> labels;

		private Map<String, List<PortBinding>> ports;

		private List<String> links;

		private List<String> volumes;

		private String networkMode;

		private String ipAddress;

		private Boolean privilegedMode;

		private List<String> securityOpt;

		private Builder() {
		}

		public Builder labels(Map<String, String> labels) {
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
			this.id = id;
			return this;
		}

		public Builder link(final String link) {
			if (this.links == null) {
				this.links = List.of();
			}

			ArrayList<String> tmp = new ArrayList<>(this.links);
			tmp.add(link);
			this.links = List.copyOf(tmp);
			return this;
		}

		public Builder securityOpt(final String opt) {
			if (this.securityOpt == null) {
				this.securityOpt = List.of();
			}

			ArrayList<String> tmp = new ArrayList<>(this.securityOpt);
			tmp.add(opt);
			this.securityOpt = List.copyOf(tmp);
			return this;
		}

		public Builder volume(final String volume) {
			if (this.volumes == null) {
				this.volumes = List.of();
			}

			ArrayList<String> tmp = new ArrayList<>(this.volumes);
			tmp.add(volume);
			this.volumes = List.copyOf(tmp);
			return this;
		}

		public Builder networkMode(final String networkMode) {
			this.networkMode = networkMode;
			return this;
		}

		public Builder image(final String image) {
			this.image = image;
			return this;
		}

		public Builder port(final String privatePort, final String hostIp, final String hostPort) {
			if (this.ports == null) {
				this.ports = Map.of();
			}
			final PortBinding binding = new PortBinding(hostIp, hostPort);

			HashMap<String, List<PortBinding>> tmp = new HashMap<>(this.ports);
			tmp.put(privatePort, new ArrayList<>());
			this.ports = Map.copyOf(tmp);
			ports.get(privatePort).add(binding);
			return this;
		}

		public ContainerInfo build() {
			final NetworkSettings networkSettings = NetworkSettings.builder().nullValuedPorts(this.ports)
					.ipAddress(this.ipAddress).build();
			final HostConfig hostConfig = HostConfig.builder().links(this.links).securityOpt(this.securityOpt)
					.binds(this.volumes).networkMode(this.networkMode).privileged(this.privilegedMode).build();
			final ContainerConfig containerConfig = ContainerConfig.builder().labels(this.labels).build();
			return new ContainerInfo(this.id, new Date(), null, null, containerConfig, hostConfig, null, this.image,
					networkSettings, null, null, null, null, null, null, null, null, null, null, null, null, null,
					null);
		}
	}

}
