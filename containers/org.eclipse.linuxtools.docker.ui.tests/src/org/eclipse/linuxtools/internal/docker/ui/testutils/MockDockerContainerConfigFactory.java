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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.mockito.Mockito;

/**
 * A factory for mock {@link IDockerContainerConfig}s.
 */
public class MockDockerContainerConfigFactory {

	public static Builder cmd(final String cmd) {
		return new Builder().cmd(cmd);
	}

	public static Builder labels(final Map<String, String> labels) {
		return new Builder().labels(labels);
	}

	public static class Builder {

		private final DockerContainerConfig containerConfig;
		private final Map<String, String> labels;

		private Builder() {
			this.containerConfig = Mockito.mock(DockerContainerConfig.class, Mockito.RETURNS_DEEP_STUBS);
			this.labels = Collections.emptyMap();
		}

		public Builder labels(final Map<String, String> labels) {
			Mockito.when(this.containerConfig.labels()).thenReturn(labels);
			return this;
		}

		public Builder cmd(final String cmd) {
			Mockito.when(this.containerConfig.cmd()).thenReturn(Arrays.asList(cmd));
			return this;
		}

		public DockerContainerConfig build() {
			Mockito.when(this.containerConfig.exposedPorts()).thenReturn(Collections.emptySet());
			Mockito.when(this.containerConfig.env()).thenReturn(Collections.emptyList());
			Mockito.when(this.containerConfig.labels()).thenReturn(this.labels);
			return this.containerConfig;
		}
	}

}
