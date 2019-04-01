/*******************************************************************************
 * Copyright (c) 2017,2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.docker.integration.tests.mock;

import org.eclipse.linuxtools.docker.reddeer.ui.DockerTerminal;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class MockDockerTerminal {

	public static Builder setText(String text) {
		return new Builder().setText(text);
	}

	public static class Builder {

		private String text;

		public Builder setText(String text) {
			this.text = text;
			return this;
		}

		public DockerTerminal build() {
			final DockerTerminal dockerTerminal = Mockito.mock(DockerTerminal.class);
			Mockito.when(dockerTerminal.getTextFromPage(ArgumentMatchers.anyString())).thenReturn(this.text);
			return dockerTerminal;
		}

	}

}
