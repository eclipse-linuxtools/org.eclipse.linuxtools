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

import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
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
