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

import static org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier.DANGLING;
import static org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier.INTERMEDIATE;
import static org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier.TOP_LEVEL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier;
import org.eclipse.linuxtools.internal.docker.ui.testutils.DockerImageAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.messages.Image;

/**
 * Verifying that images are properly qualified as {@code Top-Level},
 * {@code Intermediate} or {@code Dangling}.
 */
public class DockerImageQualifierTestCase {

	/**
	 * A helper class to prepare dataset for parameterized test classes.
	 *
	 */
	static class ParameterizedDataset {

		private final List<Arguments> data = new ArrayList<>();

		/**
		 * Adds a pair of elements to the dataset
		 *
		 * @return this {@link ParameterizedDataset} for fluent method chaining
		 */
		public ParameterizedDataset add(final DockerImageQualifier qualifier, final Image... images) {
			this.data.add(Arguments.of(qualifier, images));
			return this;
		}

		/**
		 * @return the data in the dataset
		 */
		public Stream<Arguments> stream() {
			return this.data.stream();
		}
	}

	public static Stream<Arguments> getData() {
		final ParameterizedDataset dataset = new ParameterizedDataset();
		// top level because it has a repo and a tag
		dataset.add(TOP_LEVEL, MockImageFactory.of("foo", "", "foo:latest"));
		dataset.add(TOP_LEVEL, MockImageFactory.of("foo", "", "foo:latest", "foo:1.0"));
		// top level because it has a name
		dataset.add(TOP_LEVEL, MockImageFactory.of("foo", "", "foo:<none>"));
		// intermediate because it has a child image
		dataset.add(INTERMEDIATE, MockImageFactory.of("foo", "", "<none>:<none>"),
				MockImageFactory.of("bar", "foo", "bar:latest"));
		dataset.add(INTERMEDIATE, MockImageFactory.of("foo"), MockImageFactory.of("bar", "foo", "bar:latest"));
		// dangling because untagged because it is a leaf
		dataset.add(DANGLING, MockImageFactory.of("foo", "", "<none>:<none>"));
		dataset.add(DANGLING, MockImageFactory.of("foo"));
		return dataset.stream();
	}

	@ParameterizedTest
	@MethodSource("getData")
	public void verifyImageQualifier(final DockerImageQualifier qualifier, final Image[] images)
			throws DockerException {
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
