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
import org.junit.Ignore;
import org.junit.Test;

/**
 * Testing the {@link DefaultDockerConnectionSettingsFinder} class
 */
public class DefaultDockerConnectionSettingsFinderSWTBotTest {

	@Test
	@Ignore
	// ignored because System properties loaded in the background process shell take precedence over
	// the ones set in the test.
	public void shouldFindConnectionSettingsFromShellEnv() {
		// given
		System.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_HOST, "tcp://foo");
		System.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH, "/path/to/certs");
		System.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_TLS_VERIFY, "1");
		// when
		final IDockerConnectionSettings connectionSettings = new DefaultDockerConnectionSettingsFinder().defaultsWithShellEnv();
		// then
		assertThat(connectionSettings.isSettingsResolved()).isFalse();
		assertThat(connectionSettings.getType()).isEqualTo(BindingType.TCP_CONNECTION);
		assertThat(((TCPConnectionSettings)connectionSettings).getHost()).isEqualTo("tcp://foo");
		assertThat(((TCPConnectionSettings)connectionSettings).getPathToCertificates()).isEqualTo("/path/to/certs");
		assertThat(((TCPConnectionSettings)connectionSettings).isTlsVerify()).isTrue();
	}
	
	@Test
	public void shouldCreateSecuredConnectionSettingsFromProperties() {
		// given
		final Properties properties = new Properties();
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_HOST, "tcp://foo");
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH, "/path/to/certs");
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_TLS_VERIFY, "1");
		// when
		final IDockerConnectionSettings connectionSettings = new DefaultDockerConnectionSettingsFinder().createDockerConnectionSettings(properties);
		// then
		assertThat(connectionSettings.isSettingsResolved()).isFalse();
		assertThat(connectionSettings.getType()).isEqualTo(BindingType.TCP_CONNECTION);
		assertThat(((TCPConnectionSettings)connectionSettings).getHost()).isEqualTo("tcp://foo");
		assertThat(((TCPConnectionSettings)connectionSettings).getPathToCertificates()).isEqualTo("/path/to/certs");
		assertThat(((TCPConnectionSettings)connectionSettings).isTlsVerify()).isTrue();
	}

	@Test
	public void shouldCreateUnsecuredConnectionSettingsFromProperties() {
		// given
		final Properties properties = new Properties();
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_HOST, "tcp://foo");
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH, "/path/to/certs");
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_TLS_VERIFY, "0");
		// when
		final IDockerConnectionSettings connectionSettings = new DefaultDockerConnectionSettingsFinder().createDockerConnectionSettings(properties);
		// then
		assertThat(connectionSettings.isSettingsResolved()).isFalse();
		assertThat(connectionSettings.getType()).isEqualTo(BindingType.TCP_CONNECTION);
		assertThat(((TCPConnectionSettings)connectionSettings).getHost()).isEqualTo("tcp://foo");
		assertThat(((TCPConnectionSettings)connectionSettings).getPathToCertificates()).isEqualTo("/path/to/certs");
		assertThat(((TCPConnectionSettings)connectionSettings).isTlsVerify()).isFalse();
	}

	@Test
	public void shouldCreateAnotherUnsecuredConnectionSettingsFromProperties() {
		// given
		final Properties properties = new Properties();
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_HOST, "tcp://foo");
		properties.setProperty(DefaultDockerConnectionSettingsFinder.DOCKER_CERT_PATH, "/path/to/certs");
		// when
		final IDockerConnectionSettings connectionSettings = new DefaultDockerConnectionSettingsFinder().createDockerConnectionSettings(properties);
		// then
		assertThat(connectionSettings.isSettingsResolved()).isFalse();
		assertThat(connectionSettings.getType()).isEqualTo(BindingType.TCP_CONNECTION);
		assertThat(((TCPConnectionSettings)connectionSettings).getHost()).isEqualTo("tcp://foo");
		assertThat(((TCPConnectionSettings)connectionSettings).getPathToCertificates()).isEqualTo("/path/to/certs");
		assertThat(((TCPConnectionSettings)connectionSettings).isTlsVerify()).isFalse();
	}
}
