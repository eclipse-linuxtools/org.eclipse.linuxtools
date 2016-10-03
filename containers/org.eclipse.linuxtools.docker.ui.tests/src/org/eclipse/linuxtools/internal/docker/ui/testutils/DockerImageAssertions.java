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

import static org.assertj.core.api.Assertions.fail;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier;

/**
 * Custom {@link Assertions} for the {@link IDockerImage} elements
 */
public class DockerImageAssertions extends AbstractAssert<DockerImageAssertions, IDockerImage> {

	public static DockerImageAssertions assertThat(final IDockerImage image) {
		return new DockerImageAssertions(image);
	}

	protected DockerImageAssertions(final IDockerImage actual) {
		super(actual, DockerImageAssertions.class);
	}

	public DockerImageAssertions is(final DockerImageQualifier qualifier) {
		switch (qualifier) {
		case DANGLING:
			assertThat(actual).isDanglingImage();
			break;
		case INTERMEDIATE:
			assertThat(actual).isIntermediateImage();
			break;
		case TOP_LEVEL:
			assertThat(actual).isTopLevelImage();
			break;
		}
		return this;
	}

	public DockerImageAssertions isTopLevelImage() {
		isNotNull();
		if (actual.isDangling() || actual.isIntermediateImage()) {
			fail("Expected images '" + actual.repo() + "' to be a top-level images but it was not.");
		}
		return this;
	}

	public DockerImageAssertions isIntermediateImage() {
		isNotNull();
		if (!actual.isIntermediateImage()) {
			fail("Expected images '" + actual.repo() + "' to be an intermediate images but it was not.");
		}
		return this;
	}

	public DockerImageAssertions isDanglingImage() {
		isNotNull();
		if (!actual.isDangling()) {
			fail("Expected images '" + actual.repo() + "' to be a danglingimage but it was not.");
		}
		return this;
	}

}
