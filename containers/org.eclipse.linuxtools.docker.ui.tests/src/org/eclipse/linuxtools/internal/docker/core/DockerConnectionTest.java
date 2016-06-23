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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Image;

/**
 * Testing the {@link DockerConnection} class.
 */
public class DockerConnectionTest {

	@Test
	public void shouldLoadContainers() throws DockerException {
		// given
		final Container fooContainer = MockContainerFactory.id("foo").build();
		final Container barContainer = MockContainerFactory.id("bar").build();
		final DockerClient client = MockDockerClientFactory.container(fooContainer).container(barContainer).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		dockerConnection.open(false);
		// when
		final List<IDockerContainer> containers = dockerConnection.getContainers();
		// then
		assertThat(containers).hasSize(2);
	}

	@Test
	public void shouldLoadImages() throws DockerException {
		// given
		final Image fooImage = MockImageFactory.id("foo").build();
		final Image barImage = MockImageFactory.id("bar").build();
		final DockerClient client = MockDockerClientFactory.image(fooImage).image(barImage).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		dockerConnection.open(false);
		// when
		final List<IDockerImage> images = dockerConnection.getImages();
		// then
		assertThat(images).hasSize(2);

	}

	@Test
	public void hasImageTest() throws DockerException {
		// given
		final Image fooImage = MockImageFactory.id("foo")
				.name("foo", "foo:latest", "foo:1.0", "org/foo", "org/foo:1.0", "org/foo:latest").build();
		final DockerClient client = MockDockerClientFactory.image(fooImage).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		dockerConnection.open(false);
		// then
		assertTrue(dockerConnection.hasImage("foo", null));
		assertTrue(dockerConnection.hasImage("foo", "latest"));
		assertTrue(dockerConnection.hasImage("foo", "1.0"));
		assertTrue(dockerConnection.hasImage("org/foo", null));
		assertTrue(dockerConnection.hasImage("org/foo", "latest"));
		assertTrue(dockerConnection.hasImage("org/foo", "1.0"));
	}
}
