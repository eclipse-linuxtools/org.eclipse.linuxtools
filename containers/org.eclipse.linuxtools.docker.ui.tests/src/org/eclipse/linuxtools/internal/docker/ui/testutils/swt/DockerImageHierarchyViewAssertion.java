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
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImageHierarchyView;

/**
 * Custom assertions on an {@link DockerImageHierarchyView}.
 */
public class DockerImageHierarchyViewAssertion
		extends AbstractAssert<DockerImageHierarchyViewAssertion, DockerImageHierarchyView> {

	protected DockerImageHierarchyViewAssertion(final DockerImageHierarchyView actual) {
		super(actual, DockerImageHierarchyViewAssertion.class);
	}

	public static DockerImageHierarchyViewAssertion assertThat(final DockerImageHierarchyView actual) {
		return new DockerImageHierarchyViewAssertion(actual);
	}

	public DockerImageHierarchyViewAssertion isEmpty() {
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

	public DockerImageHierarchyViewAssertion isNotEmpty() {
		notNullValue();
		if(!actual.isShowingConnectionsPane()) {
			failWithMessage("Expected Docker Explorer View to show the connections pane but it did not");
		}
		return this;
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
