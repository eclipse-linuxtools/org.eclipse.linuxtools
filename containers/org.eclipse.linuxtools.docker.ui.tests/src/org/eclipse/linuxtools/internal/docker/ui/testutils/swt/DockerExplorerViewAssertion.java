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
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;

/**
 * Custom assertions on an {@link DockerExplorerView}.
 */
public class DockerExplorerViewAssertion extends AbstractAssert<DockerExplorerViewAssertion, DockerExplorerView> {
	
	protected DockerExplorerViewAssertion(final DockerExplorerView actual) {
		super(actual, DockerExplorerViewAssertion.class);
	}

	public static DockerExplorerViewAssertion assertThat(final DockerExplorerView actual) {
		return new DockerExplorerViewAssertion(actual);
	}

	public DockerExplorerViewAssertion isEmpty() {
		notNullValue();
		if(actual.isShowingConnectionsPane()) {
			failWithMessage("Expected Docker Explorer View to show the explanation pane but it did not");
		}
		return this;
	}

	public DockerExplorerViewAssertion isNotEmpty() {
		notNullValue();
		if(!actual.isShowingConnectionsPane()) {
			failWithMessage("Expected Docker Explorer View to show the connections pane but it did not");
		}
		return this;
	}
}
