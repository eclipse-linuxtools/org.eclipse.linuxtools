/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

package org.eclipse.linuxtools.docker.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.eclipse.linuxtools.internal.docker.core.RegistryAccountInfo;
import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Testing the {@link RegistryInfo} class
 */
public class RegistryInfoTest {

	public static Stream<Arguments> getData() {
		return Stream.of(
				Arguments.of(new RegistryInfo("http://localhost", false), "localhost"),
				Arguments.of(new RegistryInfo("http://localhost:5000", false), "localhost:5000"),
				Arguments.of(new RegistryAccountInfo("http://localhost:5000", "user", "user@foo.com",
						"secret".toCharArray(), false), "localhost:5000"));
	}

	@ParameterizedTest
	@MethodSource("getData")
	public void shouldGetServerHost(final RegistryInfo registryInfo, final String expectedServerAddress) {
		// when
		final String serverAddress = registryInfo.getServerHost();
		// then
		assertThat(serverAddress).isEqualTo(expectedServerAddress);
	}
}
