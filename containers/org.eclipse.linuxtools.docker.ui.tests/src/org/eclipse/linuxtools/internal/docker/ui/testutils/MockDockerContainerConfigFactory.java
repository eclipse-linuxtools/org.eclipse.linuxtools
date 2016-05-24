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

import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.mockito.Mockito;

/**
 * A factory for mock {@link IDockerContainerConfig}s.
 */
public class MockDockerContainerConfigFactory {

	public static Builder cmd(final String cmd) {
		return new Builder().cmd(cmd);
	}
	
	public static class Builder {
		
		private final IDockerContainerConfig containerConfig;

		private Builder() {
			this.containerConfig = Mockito
					.mock(IDockerContainerConfig.class, Mockito.RETURNS_DEEP_STUBS);
		}
		
		public Builder cmd(final String cmd) {
			Mockito.when(this.containerConfig.cmd()).thenReturn(Arrays.asList(cmd));
			return this;
		}
		
		public IDockerContainerConfig build() {
			Mockito.when(this.containerConfig.exposedPorts()).thenReturn(Collections.emptySet());
			Mockito.when(this.containerConfig.env()).thenReturn(Collections.emptyList());
			return this.containerConfig;
		}
	}


}
