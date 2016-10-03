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

import static org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier.DANGLING;
import static org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier.INTERMEDIATE;
import static org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier.TOP_LEVEL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier;
import org.eclipse.linuxtools.internal.docker.ui.testutils.DockerImageAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;

/**
 * Verifying that images are properly qualified as {@code Top-Level},
 * {@code Intermediate} or {@code Dangling}.
 */
@RunWith(Parameterized.class)
public class DockerImageQualifierTestCase {

	/**
	 * A helper class to prepare dataset for test classes running with the
	 * {@link Parameterized} JUnit runner.
	 *
	 */
	static class ParameterizedDataset {

		private final List<Object[]> data = new ArrayList<>();

		/**
		 * Adds a pair of elements to the dataset
		 *
		 * @return this {@link ParameterizedDataset} for fluent method chaining
		 */
		public ParameterizedDataset add(final DockerImageQualifier qualifier, final Image... images) {
			this.data.add(new Object[] { qualifier, images });
			return this;
		}

		/**
		 * @return the {@link List} of data in the dataset
		 */
		public List<Object[]> toList() {
			return this.data;
		}
	}

	@Parameters
	public static Collection<Object[]> getData() {
		final ParameterizedDataset dataset = new ParameterizedDataset();
		// top level because it has a repo and a tag
		dataset.add(TOP_LEVEL, MockImageFactory.id("foo").name("foo:latest").build());
		dataset.add(TOP_LEVEL, MockImageFactory.id("foo").name("foo:latest", "foo:1.0").build());
		// top level because it has a name
		dataset.add(TOP_LEVEL, MockImageFactory.id("foo").name("foo:<none>").build());
		// intermediate because it has a child image
		dataset.add(INTERMEDIATE, MockImageFactory.id("foo").name("<none>:<none>").build(),
				MockImageFactory.id("bar").parentId("foo").name("bar:latest").build());
		dataset.add(INTERMEDIATE, MockImageFactory.id("foo").build(),
				MockImageFactory.id("bar").parentId("foo").name("bar:latest").build());
		// dangling because untagged because it is a leaf
		dataset.add(DANGLING, MockImageFactory.id("foo").name("<none>:<none>").build());
		dataset.add(DANGLING, MockImageFactory.id("foo").build());
		return dataset.toList();
	}

	@Parameter(0)
	public DockerImageQualifier qualifier;

	@Parameter(1)
	public Image[] images;

	@Test
	public void verifyImageQualifier() throws DockerException {
		// given
		final DockerClient client = MockDockerClientFactory.images(images).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		// when
		dockerConnection.open(false);
		// then
		DockerImageAssertions.assertThat(dockerConnection.getImage("foo")).is(qualifier);
	}

}
