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
