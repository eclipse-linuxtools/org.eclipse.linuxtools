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

import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.mockito.Mockito;

/**
 * A factory for mock {@link IDockerHostConfig}s.
 */
public class MockDockerHostConfigFactory {

	public static Builder publishAllPorts(final boolean publishAllPorts) {
		return new Builder().publishAllPorts(publishAllPorts);
	}

	public static Builder networkMode(final String networkMode) {
		return new Builder().networkMode(networkMode);
	}

	public static class Builder {

		private final IDockerHostConfig hostConfig;

		private Builder() {
			this.hostConfig = Mockito
					.mock(IDockerHostConfig.class, Mockito.RETURNS_DEEP_STUBS);
		}

		public Builder publishAllPorts(final boolean publishAllPorts) {
			Mockito.when(this.hostConfig.publishAllPorts()).thenReturn(publishAllPorts);
			return this;
		}

		public Builder networkMode(final String networkMode) {
			Mockito.when(this.hostConfig.networkMode()).thenReturn(networkMode);
			return this;
		}

		public IDockerHostConfig build() {
			return this.hostConfig;
		}
	}


}
