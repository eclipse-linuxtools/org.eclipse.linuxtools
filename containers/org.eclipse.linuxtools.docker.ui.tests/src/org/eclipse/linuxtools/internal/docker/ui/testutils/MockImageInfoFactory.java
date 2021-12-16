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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mandas.docker.client.messages.ContainerConfig;
import org.mandas.docker.client.messages.ImageInfo;
import org.mockito.Mockito;

/**
 * A factory for mock {@link ImageInfo}s.
 */
public class MockImageInfoFactory {

	public static Builder volume(final String volume) {
		return new Builder().volume(volume);
	}

	public static class Builder {

		private final ImageInfo imageInfo;

		private Set<String> volumes;

		private List<String> command;

		private List<String> entrypoint;

		private List<String> env;

		private Builder() {
			this.imageInfo = Mockito.mock(ImageInfo.class, Mockito.RETURNS_DEEP_STUBS);
		}

		public Builder volume(final String volume) {
			if (this.volumes == null) {
				this.volumes = Set.of(volume);
			}
			Set<String> tmpVolumes = new HashSet<>();
			tmpVolumes.addAll(this.volumes);
			tmpVolumes.add(volume);
			this.volumes = Collections.unmodifiableSet(tmpVolumes);
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

		public Builder env(final List<String> env) {
			this.env = env;
			return this;
		}

		public ImageInfo build() {
			final ContainerConfig config = Mockito.mock(ContainerConfig.class);
			final ContainerConfig containerConfig = Mockito.mock(ContainerConfig.class);
			Mockito.when(this.imageInfo.config()).thenReturn(config);
			Mockito.when(this.imageInfo.containerConfig()).thenReturn(containerConfig);
			Mockito.when(config.cmd()).thenReturn(List.copyOf(this.command));
			Mockito.when(config.entrypoint()).thenReturn(List.copyOf(this.entrypoint));
			Mockito.when(config.volumes()).thenReturn(Set.copyOf(this.volumes));
			Mockito.when(config.env()).thenReturn(List.copyOf(this.env));
			return imageInfo;
		}
	}

}
