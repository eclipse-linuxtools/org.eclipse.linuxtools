/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;

import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;

/**
 * 
 */
public class MockDockerContainerInfoFactory {

	public static Builder port(final String privatePort, final String hostIp, final String hostPort) {
		return new Builder().port(privatePort, hostIp, hostPort);
	}
	
	public static Builder link(final String link) {
		return new Builder().link(link);
	}

	public static Builder volume(final String volume) {
		return new Builder().volume(volume);
	}

	public static class Builder {
		
		private final ContainerInfo containerInfo;

		private Map<String, List<PortBinding>> ports;
		
		private List<String> links;

		private List<String> volumes;
		
		private Builder() {
			this.containerInfo = Mockito
					.mock(ContainerInfo.class, Mockito.RETURNS_DEEP_STUBS);
		}
		
		public Builder link(final String link) {
			if(this.links == null) {
				this.links = new ArrayList<>();
			}
			this.links.add(link);
			return this;
		}

		public Builder volume(final String volume) {
			if(this.volumes == null) {
				this.volumes = new ArrayList<>();
			}
			this.volumes.add(volume);
			return this;
		}
		
		public Builder port(final String privatePort, final String hostIp, final String hostPort) {
			if(this.ports == null) {
				this.ports = new HashMap<>();
			}
			final PortBinding binding = Mockito.mock(PortBinding.class);
			Mockito.when(binding.hostIp()).thenReturn(hostIp);
			Mockito.when(binding.hostPort()).thenReturn(hostPort);
			ports.put(privatePort, new ArrayList<>());
			ports.get(privatePort).add(binding);
			return this;
		}
		
		public ContainerInfo build() {
			final NetworkSettings networkSettings = Mockito.mock(NetworkSettings.class);
			Mockito.when(this.containerInfo.networkSettings()).thenReturn(networkSettings);
			Mockito.when(networkSettings.ports()).thenReturn(this.ports);
			final HostConfig hostConfig = Mockito.mock(HostConfig.class);
			Mockito.when(this.containerInfo.hostConfig()).thenReturn(hostConfig);
			Mockito.when(hostConfig.links()).thenReturn(this.links);
			Mockito.when(hostConfig.binds()).thenReturn(this.volumes);
			return containerInfo;
		}
	}


}
