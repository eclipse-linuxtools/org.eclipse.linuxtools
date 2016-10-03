/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import static org.hamcrest.Matchers.notNullValue;

import org.assertj.core.api.AbstractAssert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImageHierarchyView;

/**
 * Custom assertions on an {@link DockerImageHierarchyView}.
 */
public class DockerImageHierarchyViewAssertions
		extends AbstractAssert<DockerImageHierarchyViewAssertions, DockerImageHierarchyView> {

	protected DockerImageHierarchyViewAssertions(final DockerImageHierarchyView actual) {
		super(actual, DockerImageHierarchyViewAssertions.class);
	}

	public static DockerImageHierarchyViewAssertions assertThat(final DockerImageHierarchyView actual) {
		return new DockerImageHierarchyViewAssertions(actual);
	}

	public DockerImageHierarchyViewAssertions isEmpty() {
		notNullValue();
		try {
			// let's make sure changes in the UI were taken into account before
			// performing assertions
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if(actual.isShowingConnectionsPane()) {
			failWithMessage("Expected Docker Explorer View to show the explanation pane but it did not");
		}
		return this;
	}

	public DockerImageHierarchyViewAssertions isNotEmpty() {
		notNullValue();
		if(!actual.isShowingConnectionsPane()) {
			failWithMessage("Expected Docker Explorer View to show the connections pane but it did not");
		}
		return this;
	}

	public void hasSelectedElement(final IDockerImage expectedSelection) {
		notNullValue();
		final IStructuredSelection selection = (IStructuredSelection) actual.getCommonViewer().getSelection();
		if (selection.size() != 1) {
			failWithMessage("Expected Docker Image Hierarchy view to have <%s> selected elements, but there was <%s>",
					1, selection.size());
		}
		final Object selectedElement = ((IDockerImageHierarchyNode) selection.getFirstElement()).getElement();
		if (selectedElement instanceof IDockerImage) {
			final IDockerImage selectedImage = (IDockerImage) selectedElement;
			if (!selectedImage.id().equals(expectedSelection.id())) {
				failWithMessage(
						"Expected Docker Image Hierarchy view to have a Docker images with id <%s> as the selected element, but it was <%s>",
						expectedSelection.id(), ((IDockerImage) selectedElement).id());
			}
		} else {
			failWithMessage(
					"Expected Docker Image Hierarchy view to have a Docker Image as the selected element, but it was a <%s>",
					selectedElement.getClass());

		}

	}

	public void hasSelectedElement(final Object expectedSelection) {
		notNullValue();
		final IStructuredSelection selection = (IStructuredSelection) actual.getCommonViewer().getSelection();
		if (selection.size() != 1) {
			failWithMessage("Expected Docker Image Hierarchy view to have <%s> selected elements, but there was <%s>",
					1, selection.size());
		}
		final Object selectedElement = ((IDockerImageHierarchyNode) selection.getFirstElement()).getElement();
		if (!selectedElement.equals(expectedSelection)) {
			failWithMessage(
					"Expected Docker Image Hierarchy view to have <%s> as the selected element, but it was <%s>",
					expectedSelection, selectedElement);
		}
	}
}
