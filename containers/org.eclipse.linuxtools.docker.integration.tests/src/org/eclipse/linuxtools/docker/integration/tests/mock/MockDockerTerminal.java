/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.docker.integration.tests.mock;

import org.eclipse.linuxtools.docker.reddeer.ui.DockerTerminal;
import org.mockito.Matchers;
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
			Mockito.when(dockerTerminal.getTextFromPage(Matchers.anyString())).thenReturn(this.text);
			return dockerTerminal;
		}

	}

}
