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

import java.util.Properties;

import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings.BindingType;
import org.junit.Test;

/**
 * Testing the {@link DefaultDockerConnectionSettingsFinder} class
 */
public class DefaultDockerConnectionSettingsFinderSWTBotTest {

	@Test
	public void shouldCreateSecuredConnectionSettingsFromProperties() {
		// given
		final Properties properties = new Properties();
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_HOST, "tcp://foo");
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH, "/path/to/certs");
		// actually, the DOCKER_TLS_VERIFY is not used. If DOCKER_CERT_PATH is set, the we assume that DOCKER_TLS_VERIFY is '1'
		//properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_TLS_VERIFY, "1");
		// when
		final IDockerConnectionSettings connectionSettings = new DefaultDockerConnectionSettingsFinder().createDockerConnectionSettings(properties);
		// then
		assertThat(connectionSettings.isSettingsResolved()).isFalse();
		assertThat(connectionSettings.getType()).isEqualTo(BindingType.TCP_CONNECTION);
		assertThat(((TCPConnectionSettings)connectionSettings).getHost()).isEqualTo("https://foo");
		assertThat(((TCPConnectionSettings)connectionSettings).getPathToCertificates()).isEqualTo("/path/to/certs");
		assertThat(((TCPConnectionSettings)connectionSettings).isTlsVerify()).isTrue();
	}

	@Test
	public void shouldNotCreateConnectionSettingsWhenMissingHost() {
		// given
		final Properties properties = new Properties();
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH, "/path/to/certs");
		// actually, the DOCKER_TLS_VERIFY is not used. If DOCKER_CERT_PATH is set, the we assume that DOCKER_TLS_VERIFY is '1'
		//properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_TLS_VERIFY, "1");
		// when
		final IDockerConnectionSettings connectionSettings = new DefaultDockerConnectionSettingsFinder().createDockerConnectionSettings(properties);
		// then
		assertThat(connectionSettings).isNull();
	}

}
