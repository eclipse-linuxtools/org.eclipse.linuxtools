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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mandas.docker.client.messages.ContainerConfig;
import org.mandas.docker.client.messages.ImageConfig;
import org.mandas.docker.client.messages.ImageInfo;

/**
 * A factory for mock {@link ImageInfo}s.
 */
public class MockImageInfoFactory {

	public static Builder volume(final String volume) {
		return new Builder().volume(volume);
	}

	public static class Builder {

		private Set<String> volumes;

		private List<String> command;

		private List<String> entrypoint;

		private List<String> env;

		private Builder() {
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
			// the builder fields are optional, so copy defensively: List.copyOf(null)
			// throws rather than yielding an empty list
			final Set<String> volumes = this.volumes == null ? Set.of() : Set.copyOf(this.volumes);
			final ImageConfig config = ImageConfig.builder().cmd(copyOf(this.command))
					.entrypoint(copyOf(this.entrypoint)).volumes(volumes).env(copyOf(this.env)).build();
			final ContainerConfig containerConfig = ContainerConfig.builder().cmd(copyOf(this.command))
					.entrypoint(copyOf(this.entrypoint)).volumes(volumes).env(copyOf(this.env)).build();
			return new ImageInfo(null, null, null, new Date(), null, containerConfig, null, null, config, null, null,
					null, null);
		}

		private static List<String> copyOf(final List<String> values) {
			return values == null ? List.of() : List.copyOf(values);
		}
	}

}
