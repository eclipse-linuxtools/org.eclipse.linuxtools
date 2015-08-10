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

	public static final String IMAGE_SEARCH_RESULT = "imageSearchResult"; //$NON-NLS-1$

	public static final String SELECTED_IMAGE = "selectedImage"; //$NON-NLS-1$

	public static final String IMAGE_TAG_SEARCH_RESULT = "imageTagSearchResult"; //$NON-NLS-1$

	public static final String SELECTED_IMAGE_TAG = "selectedImageTag"; //$NON-NLS-1$

	private final IDockerConnection selectedConnection;

	private String term = null;

	private List<IDockerImageSearchResult> imageSearchResult;

	private IDockerImageSearchResult selectedImage;

	private List<DockerImageTagSearchResult> imageTagSearchResult;

	private DockerImageTagSearchResult selectedImageTag;

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

	public List<IDockerImageSearchResult> getImageSearchResult() {
		return imageSearchResult;
	}

	public void setImageSearchResult(
			final List<IDockerImageSearchResult> searchResult) {
		firePropertyChange(IMAGE_SEARCH_RESULT, this.imageSearchResult,
				this.imageSearchResult = searchResult);
		// set the first item as the selected image
		if (!this.imageSearchResult.isEmpty()) {
			setSelectedImage(this.imageSearchResult.get(0));
		}
	}

	public IDockerImageSearchResult getSelectedImage() {
		return this.selectedImage;
	}

	public void setSelectedImage(final IDockerImageSearchResult selectedImage) {
		firePropertyChange(SELECTED_IMAGE, this.selectedImage,
				this.selectedImage = selectedImage);
	}

	public List<DockerImageTagSearchResult> getImageTagSearchResult() {
		return imageTagSearchResult;
	}

	public void setImageTagSearchResult(
			final List<DockerImageTagSearchResult> searchTagResult) {
		firePropertyChange(IMAGE_TAG_SEARCH_RESULT, this.imageTagSearchResult,
				this.imageTagSearchResult = searchTagResult);
		// set the first item as the selected image
		if (!this.imageTagSearchResult.isEmpty()) {
			setSelectedImageTag(this.imageTagSearchResult.get(0));
		}
	}

	public DockerImageTagSearchResult getSelectedImageTag() {
		return this.selectedImageTag;
	}

	public void setSelectedImageTag(
			final DockerImageTagSearchResult selectedImageTag) {
		firePropertyChange(SELECTED_IMAGE_TAG, this.selectedImageTag,
				this.selectedImageTag = selectedImageTag);
	}

}
