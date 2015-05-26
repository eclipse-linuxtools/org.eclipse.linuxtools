/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImageSearchResult;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * Databinding model for the {@link ImageSearchPage}
 * 
 * @author xcoulon
 *
 */
public class ImageSearchModel extends BaseDatabindingModel {

	public static final String TERM = "term"; //$NON-NLS-1$

	public static final String SELECTED_IMAGE = "selectedImage"; //$NON-NLS-1$

	public static final String SEARCH_RESULT = "searchResult"; //$NON-NLS-1$

	private final IDockerConnection selectedConnection;

	private String term = null;

	private IDockerImageSearchResult selectedImage;

	private List<IDockerImageSearchResult> searchResult;

	public ImageSearchModel(final IDockerConnection selectedConnection) {
		this.selectedConnection = selectedConnection;
	}

	public IDockerConnection getSelectedConnection() {
		return selectedConnection;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(final String term) {
		firePropertyChange(TERM, this.term, this.term = term);
	}

	public IDockerImageSearchResult getSelectedImage() {
		return this.selectedImage;
	}

	public void setSelectedImage(final IDockerImageSearchResult selectedImage) {
		firePropertyChange(SELECTED_IMAGE, this.selectedImage,
				this.selectedImage = selectedImage);
	}

	public List<IDockerImageSearchResult> getSearchResult() {
		return searchResult;
	}

	public void setSearchResult(
			final List<IDockerImageSearchResult> searchResult) {
		firePropertyChange(SEARCH_RESULT, this.searchResult,
				this.searchResult = searchResult);
		// set the first item as the selected image
		if (!this.searchResult.isEmpty()) {
			setSelectedImage(this.searchResult.get(0));
		}
	}

}
