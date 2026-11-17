/*******************************************************************************
 * Copyright (c) 2016, 2026 Red Hat.
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Testing the logic to retrieve the list of tags from a given Docker image name
 * (or repository) against a local server that behaves like Docker Hub: tag
 * requests without a valid bearer token are answered with a 401 response whose
 * <code>Www-Authenticate</code> header points to the token service.
 */
public class ImageTagsListTest {

	private static final String TOKEN = "test-token"; //$NON-NLS-1$

	private HttpServer server;
	private String address;
	private RegistryInfo dockerHubRegistry;

	@BeforeEach
	public void startRegistry() throws IOException {
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		address = "http://127.0.0.1:" + server.getAddress().getPort(); //$NON-NLS-1$
		server.createContext("/v2", this::handleRegistry); //$NON-NLS-1$
		server.createContext("/token", //$NON-NLS-1$
				exchange -> respond(exchange, 200, "{\"token\":\"" + TOKEN + "\"}")); //$NON-NLS-1$ //$NON-NLS-2$
		server.start();
		dockerHubRegistry = new RegistryInfo(address, true);
	}

	@AfterEach
	public void stopRegistry() {
		server.stop(0);
	}

	private void handleRegistry(final HttpExchange exchange) throws IOException {
		final String path = exchange.getRequestURI().getPath();
		final String prefix = "/v2/"; //$NON-NLS-1$
		final String suffix = "/tags/list"; //$NON-NLS-1$
		if (!path.startsWith(prefix) || !path.endsWith(suffix)) {
			// API version check and anything else: Docker Hub requires
			// authentication
			respond(exchange, 401, "{}"); //$NON-NLS-1$
			return;
		}
		final String repository = path.substring(prefix.length(), path.length() - suffix.length());
		if (repository.endsWith("/unknown")) { //$NON-NLS-1$
			respond(exchange, 404, "{\"errors\":[{\"code\":\"NAME_UNKNOWN\"}]}"); //$NON-NLS-1$
		} else if (!("Bearer " + TOKEN).equals(exchange.getRequestHeaders().getFirst("Authorization"))) { //$NON-NLS-1$ //$NON-NLS-2$
			exchange.getResponseHeaders().add("Www-Authenticate", //$NON-NLS-1$
					"Bearer realm=\"" + address + "/token\",service=\"registry.test\",scope=\"repository:" //$NON-NLS-1$ //$NON-NLS-2$
							+ repository + ":pull\""); //$NON-NLS-1$
			respond(exchange, 401, "{}"); //$NON-NLS-1$
		} else {
			respond(exchange, 200, "{\"name\":\"" + repository + "\",\"tags\":[\"1.0\",\"latest\"]}"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static void respond(final HttpExchange exchange, final int status, final String body)
			throws IOException {
		final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	@Test
	public void shouldRetrieveTagsForOfficialImage() throws DockerException {
		// when
		final List<IRepositoryTag> tags = dockerHubRegistry.getTags("php"); //$NON-NLS-1$
		// then official images live in the 'library' namespace
		assertThat(tags).extracting(IRepositoryTag::getName).containsExactly("1.0", "latest"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void shouldRetrieveTagsForRegularImage() throws DockerException {
		// when
		final List<IRepositoryTag> tags = dockerHubRegistry.getTags("jboss/wildfly"); //$NON-NLS-1$
		// then
		assertThat(tags).extracting(IRepositoryTag::getName).containsExactly("1.0", "latest"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void shouldFailForUnknownImage() {
		assertThatThrownBy(() -> dockerHubRegistry.getTags("jboss/unknown")) //$NON-NLS-1$
				.isInstanceOf(DockerException.class);
	}

}
