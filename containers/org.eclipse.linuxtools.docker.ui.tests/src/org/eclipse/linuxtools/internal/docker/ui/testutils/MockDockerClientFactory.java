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

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Image;

/**
 * Factory for mocked {@link IDockerConnection}
 */
public class MockDockerClientFactory {

	public static Builder images(final Image... images) {
		final Builder builder = new Builder();
		builder.images(Arrays.asList(images));
		return builder;
	}
	
	public static Builder noImages() {
		return images();
	}
	

	public static class Builder {
		
		private final DockerClient dockerClient;
		
		private Builder() {
			this.dockerClient = Mockito.mock(DockerClient.class);
		}
		
		public Builder images(final List<Image> images) {
			try {
				Mockito.when(dockerClient.listImages(Mockito.any())).thenReturn(images);
			} catch (DockerException | InterruptedException e) {
				// rest assured, nothing will happen while mocking the DockerClient
			}
			return this;
		}
		
		public Builder noImages() {
			return images(Collections.emptyList());
		}
		
		public DockerClient containers(final List<Container> containers) {
			try {
				Mockito.when(dockerClient.listContainers(Mockito.any())).thenReturn(containers);
			} catch (DockerException | InterruptedException e) {
				// rest assured, nothing will happen while mocking the DockerClient
			}
			return this.dockerClient;
		}
		
		public DockerClient containers(final Container... containers) {
			return containers(Arrays.asList(containers));
		}
		
		public DockerClient noContainers() {
			return containers(Collections.emptyList());
		}
		
	}
	
}
