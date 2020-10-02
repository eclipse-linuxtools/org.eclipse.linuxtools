/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat and others.
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

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mandas.docker.client.exceptions.DockerRequestException;

/**
 * Testing the {@link DockerException} class
 */
@RunWith(Parameterized.class)
public class DockerExceptionTest {

	@Parameters
	public static Object[][] getData() throws URISyntaxException {
		final Object[][] data = new Object[][] {
				// 0: Normal exceptions
				new Object[] { new DockerException("this is an error"), "this is an error" },
				// 1:
				new Object[] { new DockerException("error with message: 232"), "error with message: 232" },
				// 2: Use passed message or message from original exception - Behave like
				// Throwable
				new Object[] { new DockerException(null, new RuntimeException("This is a test")),
						"java.lang.RuntimeException: This is a test" },
				// 3:
				new Object[] { new DockerException("First", new RuntimeException("This is a test")), "First" },
				// 4: Do not parse passed message
				new Object[] {
						new DockerException(
								"{\"message\":\"invalid reference format: repository name must be lowercase\"}"),
						"{\"message\":\"invalid reference format: repository name must be lowercase\"}" },
				// 5: Decode DockerRequestException
				new Object[] { new DockerException(null, new DockerRequestException("m", new URI("http://none"), 404,
						"{\"message\":\"invalid reference format: repository name must be lowercase\"}", null)),
						"invalid reference format: repository name must be lowercase"
				},
				// 6: DockerRequestException and prefix message
				new Object[] { new DockerException("Additional info", new DockerRequestException("m", new URI("http://none"), 404,
						"{\"message\":\"invalid reference format: repository name must be lowercase\"}", null)),
						"Additional info; invalid reference format: repository name must be lowercase"
				},
				// 7: DockerRequestException Without Json-message - use toString
				new Object[] {
						new DockerException(
								new DockerRequestException("Hello", new URI("http://none"), 404, "Response", null)),
						"org.mandas.docker.client.exceptions.DockerRequestException: Request error: Hello http://none: 404, body: Response" },
				// 8:
				new Object[] {
						new DockerException("Additional info",
								new DockerRequestException("Hello", new URI("http://none"), 404, "Response", null)),
						"Additional info" },
				// 9: Search DockerRequestException
				new Object[] { new DockerException(null,
						new RuntimeException(new DockerRequestException("m", new URI("http://none"), 404,
								"{\"message\":\"invalid reference format: repository name must be lowercase\"}",
								null))),
						"invalid reference format: repository name must be lowercase" },
				// 10: Wrapped in exception with message
				new Object[] { new DockerException(null, new RuntimeException("Hello", new DockerRequestException("m",
						new URI("http://none"), 404,
								"{\"message\":\"invalid reference format: repository name must be lowercase\"}",
								null))),
						"invalid reference format: repository name must be lowercase" },
				// 11: Wrapped twice
				new Object[] {
						new DockerException(null,
								new RuntimeException(new RuntimeException(new DockerRequestException("m",
										new URI("http://none"), 404,
										"{\"message\":\"invalid reference format: repository name must be lowercase\"}",
										null)))),
						"invalid reference format: repository name must be lowercase" },
				// 12: Wrapped without message
				new Object[] { new DockerException(null, new RuntimeException("Hello",
						new DockerRequestException("m", new URI("http://none"), 404, "Do not show this", null))),
						"java.lang.RuntimeException: Hello" },
		};
		return data;
	}

	@Parameter(0)
	public DockerException dockerException;

	@Parameter(1)
	public String expectedMessage;

	@Test
	public void shouldGetCorrectExceptionMessage() {
		// when
		final String message = dockerException.getMessage();
		// then
		assertThat(message).isEqualTo(expectedMessage);
	}
}
