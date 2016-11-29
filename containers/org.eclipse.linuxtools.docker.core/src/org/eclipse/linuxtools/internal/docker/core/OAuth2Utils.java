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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Utility methods related to the OAuth2 Autorization Framework
 */
public class OAuth2Utils {

	/**
	 * Parses the given {@code value} similar to
	 * <code>Bearer realm="https://auth.docker.io/token",service="registry.docker.io",scope="repository:jboss/wildfly:pull"</code>
	 * to extract the <code>realm</code>, <code>service</code> and
	 * <code>scope</code> items.
	 * 
	 * @param value
	 *            the value to parse
	 * @return a map of the extracted items, or <code>null</code> if the given
	 *         {@code value} did not match the expected pattern.
	 */
	public static Map<String, String> parseWwwAuthenticateHeader(
			final String value) {
		final Pattern authHeaderPattern = Pattern.compile(
				"Bearer realm=\"(?<realm>.+)\",service=\"(?<service>.+)\",scope=\"(?<scope>.+)\""); //$NON-NLS-1$
		final Matcher matcher = authHeaderPattern.matcher(value);
		if (!matcher.matches()) {
			return null;
		}
		final Map<String, String> result = new HashMap<>();
		result.put("realm", matcher.group("realm")); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("service", matcher.group("service")); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("scope", matcher.group("scope")); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}

	@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
	public static class BearerTokenResponse {

		@JsonProperty("token") //$NON-NLS-1$
		private String token;

		public String getToken() {
			return this.token;
		}

		public void setToken(final String token) {
			this.token = token;
		}

	}

}
