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
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.Info;

/**
 * Factory for mocked {@link IDockerConnection}
 */
public class MockDockerClientFactory {

	/**
	 * @return an {@link DockerClient} with no {@link IDockerImage} and no {@link IDockerContainer}.
	 */
	public static DockerClient build() {
		return image(null).build();
	}

	/**
	 * @param image the first {@link Image} to use to build the {@link DockerClient}
	 * @return a {@link Builder} to build a {@link DockerClient}
	 */
	public static Builder image(final Image image) {
		final Builder builder = new Builder();
		builder.image(image);
		return builder;
	}
	
	/**
	 * @param image the first {@link Image} to use to build the {@link DockerClient}
	 * @param imageInfo the associated {@link ImageInfo}
	 * @return a {@link Builder} to build a {@link DockerClient}
	 */
	public static Builder image(final Image image, final ImageInfo imageInfo) {
		final Builder builder = new Builder();
		builder.image(image, imageInfo);
		return builder;
	}

	/**
	 * @param image the first {@link Container} to use to build the {@link DockerClient}
	 * @return a {@link Builder} to build a {@link DockerClient}
	 */
	public static Builder container(final Container container) {
		final Builder builder = new Builder();
		builder.container(container);
		return builder;
	}
	
	/**
	 * @param container the first {@link Container} to use to build the {@link DockerClient}
	 * @param containerInfo the associated {@link ContainerInfo}
	 * @return a {@link Builder} to build a {@link DockerClient}
	 */
	public static Builder container(final Container container, final ContainerInfo containerInfo) {
		final Builder builder = new Builder();
		builder.container(container, containerInfo);
		return builder;
	}

	public static class Builder {
		
		private final DockerClient dockerClient;
		
		private final List<Image> images = new ArrayList<>();

		private final List<Container> containers = new ArrayList<>();
		
		private Builder() {
			this.dockerClient = Mockito.mock(DockerClient.class);
			try {
				final Info info = Mockito.mock(Info.class);
				Mockito.when(info.memTotal()).thenReturn(1024L);
				Mockito.when(dockerClient.info()).thenReturn(info);
			} catch (DockerException | InterruptedException e) {
				// ignore while setting-up the mock instance
			}
		}
		
		public Builder image(final Image image) {
			if(image != null) {
				this.images.add(image);
			}
			return this;
		}
		
		public Builder image(final Image image, final ImageInfo imageInfo) {
			if(image != null ) {
				this.images.add(image);
			}
			try {
				Mockito.when(this.dockerClient.inspectImage(image.id())).thenReturn(imageInfo);
			} catch (DockerException | InterruptedException e) {
				// rest assured, nothing will happen while mocking the DockerClient
			}
			return this;
		}
		
		public Builder container(final Container container) {
			if(container != null) {
				this.containers.add(container);
			}
			return this;
		}

		public Builder container(final Container container, final ContainerInfo containerInfo)  {
			if(container != null) {
				this.containers.add(container);
			}
			try {
				Mockito.when(this.dockerClient.inspectContainer(container.id())).thenReturn(containerInfo);
			} catch (DockerException | InterruptedException e) {
				// rest assured, nothing will happen while mocking the DockerClient
			}
			return this;
		}
		
		public DockerClient build() {
			try {
				Mockito.when(this.dockerClient.listImages(Matchers.any())).thenReturn(this.images);
				Mockito.when(this.dockerClient.listContainers(Matchers.any())).thenReturn(this.containers);
			} catch (DockerException | InterruptedException e) {
				// nothing may happen when mocking the method call 
			}
			return this.dockerClient;
		}
	}

	/**
	 * Adds the given {@link IDockerContainer} and associated {@link IDockerContainerInfo} to the list of containers returned by the given {@link DockerClient}.
	 * @param client
	 * @param container
	 * @param containerInfo
	 */
	public static void addContainer(DockerClient dockerClient, Container container, ContainerInfo containerInfo) {
		try {
			final List<Container> containers = dockerClient.listContainers(new DockerClient.ListContainersParam[0]);
		containers.add(container);
		Mockito.when(dockerClient.inspectContainer(container.id())).thenReturn(containerInfo);
		Mockito.when(dockerClient.listContainers(Matchers.any())).thenReturn(containers);
		} catch (DockerException | InterruptedException e) {
			// nothing may happen when mocking the method call 
		}
	}
	
}
