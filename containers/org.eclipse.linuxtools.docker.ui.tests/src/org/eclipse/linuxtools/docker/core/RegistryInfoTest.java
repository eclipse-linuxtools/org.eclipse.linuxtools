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

package org.eclipse.linuxtools.docker.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.linuxtools.internal.docker.core.RegistryAccountInfo;
import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Testing the {@link RegistryInfo} class
 */
@RunWith(Parameterized.class)
public class RegistryInfoTest {

	@Parameters
	public static Object[][] getData() {
		final Object[][] data = new Object[][] {
				new Object[] { new RegistryInfo("http://localhost", false), "localhost" },
				new Object[] { new RegistryInfo("http://localhost:5000", false), "localhost:5000" },
				new Object[] { new RegistryAccountInfo("http://localhost:5000", "user", "user@foo.com",
						"secret".toCharArray(), false), "localhost:5000" }, };
		return data;
	}

	@Parameter(0)
	public RegistryInfo registryInfo;

	@Parameter(1)
	public String expectedServerAddress;

	@Test
	public void shouldGetServerHost() {
		// when
		final String serverAddress = registryInfo.getServerHost();
		// then
		assertThat(serverAddress).isEqualTo(expectedServerAddress);
	}
}
