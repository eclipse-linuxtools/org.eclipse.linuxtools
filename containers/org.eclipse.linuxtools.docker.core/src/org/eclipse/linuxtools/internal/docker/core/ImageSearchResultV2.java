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
package org.eclipse.linuxtools.internal.docker.core;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import org.mandas.docker.client.messages.ImageSearchResult;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * Binding for Image Search Results from a Registry V2.
 *
 * Currently the returned image search results lack the same degree of
 * information returned from the V1 registries.
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ImageSearchResultV2 {

	@JsonProperty("repositories") //$NON-NLS-1$
	private List<String> repositories;

	public List<ImageSearchResult> getRepositories() {
		List<ImageSearchResult> result = new ArrayList<>();
		for (String repo : repositories) {
			result.add(new ImageResultV2(repo));
		}
		return result;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("results", getRepositories()).toString(); //$NON-NLS-1$
	}

	private class ImageResultV2 implements ImageSearchResult {

		private String name;

		public ImageResultV2(String name) {
			this.name = name;
		}

		@Override
		public String description() {
			return "";
		}

		@Override
		public boolean official() {
			return false;
		}

		@Override
		public boolean automated() {
			return false;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public int starCount() {
			return 0;
		}
	}

}
