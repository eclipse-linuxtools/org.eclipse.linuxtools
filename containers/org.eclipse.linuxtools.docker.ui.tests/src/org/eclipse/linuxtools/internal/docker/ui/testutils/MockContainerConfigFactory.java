/*******************************************************************************
 * Copyright (c) 2017, 2021 Red Hat.
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

import java.util.Map;

import org.mandas.docker.client.messages.ContainerConfig;
import org.mockito.Mockito;

public class MockContainerConfigFactory {

	public static Builder labels(Map<String, String> labels) {
		return new Builder().labels(labels);
	}

	public static class Builder {

		private final ContainerConfig containerConfig;
		private Map<String, String> labels;

		private Builder() {
			this.containerConfig = Mockito.mock(ContainerConfig.class, Mockito.RETURNS_DEEP_STUBS);
			labels = Map.of();
		}

		public Builder labels(Map<String, String> labels) {
			this.labels = labels;
			Mockito.when(this.containerConfig.labels()).thenReturn(this.labels);
			return this;
		}

		public ContainerConfig build() {
			Mockito.when(this.containerConfig.labels()).thenReturn(this.labels);
			return this.containerConfig;
		}
	}

}
