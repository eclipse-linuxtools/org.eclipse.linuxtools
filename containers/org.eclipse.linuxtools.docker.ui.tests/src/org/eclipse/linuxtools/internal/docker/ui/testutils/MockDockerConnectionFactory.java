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

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerClientFactory;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;

/**
 * Factory for mocked {@link IDockerConnection}
 */
public class MockDockerConnectionFactory {

	public static Builder from(final String name, final DockerClient dockerClient) {
		return new Builder(name, dockerClient);
	}

	public static class Builder {
		
		private final DockerConnection connection;
		
		private Builder(final String name, final DockerClient dockerClient) {
			this.connection = new DockerConnection.Builder().name(name).build();
			final DockerClientFactory dockerClientFactory = Mockito.mock(DockerClientFactory.class);
			this.connection.setDockerClientFactory(dockerClientFactory);
			try {
				Mockito.when(dockerClientFactory.getClient(Matchers.anyString(), Matchers.anyString(),  Matchers.anyString())).thenReturn(dockerClient);
			} catch (DockerCertificateException e) {
				// rest assured, nothing will happen while mocking the DockerClientFactory   
			}
		}
		
		public DockerConnection get() {
			return connection;
		}

	}
	
}
