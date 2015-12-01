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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ImageInfo;

/**
 * 
 */
public class MockDockerImageInfoFactory {

	public static Builder volume(final String volume) {
		return new Builder().volume(volume);
	}
	
	public static class Builder {
		
		private final ImageInfo imageInfo;

		private Set<String> volumes;

		private List<String> command;

		private List<String> entrypoint;
		
		private Builder() {
			this.imageInfo = Mockito
					.mock(ImageInfo.class, Mockito.RETURNS_DEEP_STUBS);
		}
		
		public Builder volume(final String volume) {
			if(this.volumes == null) {
				this.volumes = new HashSet<>();
			}
			this.volumes.add(volume);
			return this;
		}

		public Builder command(final List<String> command) {
			this.command = command;
			return this;
		}
		
		public Builder entrypoint(final List<String> entrypoint) {
			this.entrypoint = entrypoint;
			return this;
		}
		
		public ImageInfo build() {
			final ContainerConfig config = Mockito.mock(ContainerConfig.class);
			final ContainerConfig containerConfig = Mockito.mock(ContainerConfig.class);
			Mockito.when(this.imageInfo.config()).thenReturn(config);
			Mockito.when(this.imageInfo.containerConfig()).thenReturn(containerConfig);
			Mockito.when(config.cmd()).thenReturn(this.command);
			Mockito.when(config.entrypoint()).thenReturn(this.entrypoint);
			Mockito.when(config.volumes()).thenReturn(this.volumes);
			return imageInfo;
		}
	}


}
