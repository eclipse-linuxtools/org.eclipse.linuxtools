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

import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.mockito.Mockito;

public class MockConsoleView {

	public static Builder msg(String msg) {
		return new Builder().msg(msg);
	}

	public static class Builder {

		private String msg;

		public Builder msg(String msg) {
			this.msg = msg;
			return this;
		}

		public ConsoleView build() {
			final ConsoleView consoleView = Mockito.mock(ConsoleView.class);
			Mockito.when(consoleView.getConsoleText()).thenReturn(this.msg);
			return consoleView;
		}

	}

}
