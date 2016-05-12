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
package org.eclipse.linuxtools.internal.docker.core;


import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.spotify.docker.client.messages.ImageSearchResult;

/**
 * Binding for Image Search Results from a Registry V1.
 * 
 * While the Docker Remote API search against Docker Hub may return just a list
 * of {@link ImageSearchResult}, standard V1 registries seem to require this
 * kind of structure as the results are paginated.
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ImageSearchResultV1 {

	@JsonProperty("num_pages") //$NON-NLS-1$
	private int totalPages;

	@JsonProperty("num_results") //$NON-NLS-1$
	private int totalResults;

	@JsonProperty("page_size") //$NON-NLS-1$
	private int pageSize;

	@JsonProperty("page") //$NON-NLS-1$
	private int page;

	@JsonProperty("query") //$NON-NLS-1$
	private String query;

	@JsonProperty("results") //$NON-NLS-1$
	private List<ImageSearchResult> result;

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<ImageSearchResult> getResult() {
		return result;
	}

	public void setResult(List<ImageSearchResult> result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("num_pages", getTotalPages()) //$NON-NLS-1$
				.add("num_results", getTotalResults()) //$NON-NLS-1$
				.add("page_size", getPageSize()) //$NON-NLS-1$
				.add("page", getPage()) //$NON-NLS-1$
				.add("query", getQuery()).toString(); //$NON-NLS-1$
	}

}

