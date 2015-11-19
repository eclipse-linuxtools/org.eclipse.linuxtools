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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
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
		
		private final List<Container> containers = new ArrayList<>();
		
		private Builder() {
			this.dockerClient = Mockito.mock(DockerClient.class);
		}
		
		public Builder images(final List<Image> images) {
			try {
				Mockito.when(dockerClient.listImages(Matchers.any())).thenReturn(images);
			} catch (DockerException | InterruptedException e) {
				// rest assured, nothing will happen while mocking the DockerClient
			}
			return this;
		}
		
		
		public Builder container(final Container container) {
			this.containers.add(container);
			return this;
		}

		public Builder container(final Container container, final ContainerInfo containerInfo)  {
			this.containers.add(container);
			try {
				Mockito.when(this.dockerClient.inspectContainer(container.id())).thenReturn(containerInfo);
			} catch (DockerException | InterruptedException e) {
				// rest assured, nothing will happen while mocking the DockerClient
			}
			return this;
		}
		
		public DockerClient build() {
			try {
				Mockito.when(this.dockerClient.listContainers(Matchers.any())).thenReturn(this.containers);
			} catch (DockerException | InterruptedException e) {
				// nothing may happen when mocking the method call 
			}
			return this.dockerClient;
		}

		public DockerClient noContainers() {
			return this.dockerClient;
		}
		
	}
	
}
