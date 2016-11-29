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

import java.util.Map;

import org.eclipse.linuxtools.internal.docker.core.OAuth2Utils;
import org.junit.Test;

/**
 * Testing the pattern to parse a <code>Www-Authenticate</code> response header
 */
public class OAuth2UtilsTest {

	@Test
	public void shouldParseHeader() {
		// given
		final String headerValue = "Bearer realm=\"https://auth.docker.io/token\",service=\"registry.docker.io\",scope=\"repository:jboss/wildfly:pull\"";
		// when
		final Map<String, String> result = OAuth2Utils.parseWwwAuthenticateHeader(headerValue);
		// then
		assertThat(result).hasSize(3).containsEntry("realm", "https://auth.docker.io/token")
				.containsEntry("service", "registry.docker.io").containsEntry("scope", "repository:jboss/wildfly:pull");
	}

}
