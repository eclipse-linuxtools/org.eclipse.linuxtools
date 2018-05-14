/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

package org.eclipse.linuxtools.docker.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Testing the logic to retrieve the list of tags from a given Docker image name
 * (or repository).
 *
 * NOTE: tests are ignored because they fail on Hudson (timeout) - need to
 * verify if the Hudson slave can connect to Docker Hub.
 */
@Ignore
public class ImageTagsListTest {

	private RegistryInfo dockerHubRegistry = new RegistryInfo(AbstractRegistry.DOCKERHUB_REGISTRY, true);

	@Test
	public void shouldRetrieveTagsForOfficialImage() throws DockerException {
		// given
		final String imageName = "php";
		// when
		final List<IRepositoryTag> tags = dockerHubRegistry.getTags(imageName);
		// then
		assertThat(tags).isNotEmpty();
	}

	@Test
	public void shouldRetrieveTagsForRegularImage() throws DockerException {
		// given
		final String imageName = "jboss/wildfly";
		// when
		final List<IRepositoryTag> tags = dockerHubRegistry.getTags(imageName);
		// then
		assertThat(tags).isNotEmpty();
	}

}
