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

import org.eclipse.reddeer.eclipse.ui.browser.WebBrowserView;
import org.mockito.Mockito;

public class MockBrowserView {

	public static Builder open() {
		return new Builder().open();
	}

	public static Builder openPageURL(String url) {
		return new Builder().openPageURL(url);
	}

	public static Builder setText(String text) {
		return new Builder().setText(text);
	}

	public static class Builder {

		private String url;
		private String text;

		public Builder setText(String text) {
			this.text = text;
			return this;
		}

		public Builder openPageURL(String url) {
			this.url = url;
			return this;
		}

		public Builder open() {
			return this;
		}

		public WebBrowserView build() {
			final WebBrowserView browserView = Mockito.mock(WebBrowserView.class);
			Mockito.when(browserView.getPageURL()).thenReturn(this.url);
			Mockito.when(browserView.getText()).thenReturn(this.text);
			return browserView;
		}

	}

}
