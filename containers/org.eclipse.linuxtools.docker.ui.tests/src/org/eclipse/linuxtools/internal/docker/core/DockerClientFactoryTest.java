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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.exceptions.DockerCertificateException;

/**
 * Testing the {@link DockerClientFactory}
 */
public class DockerClientFactoryTest {

	@Test
	public void shouldNotFailWithNullTcpHost() throws DockerCertificateException {
		// when
		final DockerClient client = new DockerClientFactory().getClient(new TCPConnectionSettings(null, null));
		// then
		assertThat(client).isNull();
	}

	@Test
	public void shouldNotFailWithEmptyTcpHost() throws DockerCertificateException {
		// when
		final DockerClient client = new DockerClientFactory().getClient(new TCPConnectionSettings("", null));
		// then
		assertThat(client).isNull();
	}
}
