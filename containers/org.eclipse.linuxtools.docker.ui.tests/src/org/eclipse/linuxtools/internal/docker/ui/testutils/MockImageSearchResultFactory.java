/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat.
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

import org.mockito.Mockito;

import org.mandas.docker.client.messages.ImageSearchResult;

/**
 * Factory for mocked {@link ImageSearchResult} instances.
 */
public class MockImageSearchResultFactory {

	public static ImageSearchResultBuilder name(final String name) {
		return new ImageSearchResultBuilder(name);
	}

	public static class ImageSearchResultBuilder {

		private final String name;

		public ImageSearchResultBuilder(final String name) {
			this.name = name;
		}

		public ImageSearchResult build() {
			final ImageSearchResult imageSearchResult = Mockito.mock(ImageSearchResult.class);
			Mockito.when(imageSearchResult.name()).thenReturn(this.name);
			return imageSearchResult;
		}

	}

}
