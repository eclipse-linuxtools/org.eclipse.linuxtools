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

import org.junit.Test;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link DockerClientFactory}
 */
public class DockerClientFactoryTest {

	@Test
	public void shouldNotFailWithNullTcpHost() throws DockerCertificateException {
		// when
		final DockerClient client = new DockerClientFactory().getClient(null, null, null);
		// then
		assertThat(client).isNull();
	}

	@Test
	public void shouldNotFailWithEmptyTcpHost() throws DockerCertificateException {
		// when
		final DockerClient client = new DockerClientFactory().getClient(null, "", null);
		// then
		assertThat(client).isNull();
	}
}
