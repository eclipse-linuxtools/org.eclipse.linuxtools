/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import org.mockito.Mockito;

import com.spotify.docker.client.messages.ImageSearchResult;

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
			Mockito.when(imageSearchResult.getName()).thenReturn(this.name);
			return imageSearchResult;
		}

	}

}
