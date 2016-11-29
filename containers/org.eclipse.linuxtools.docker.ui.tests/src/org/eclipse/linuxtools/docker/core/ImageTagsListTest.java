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
