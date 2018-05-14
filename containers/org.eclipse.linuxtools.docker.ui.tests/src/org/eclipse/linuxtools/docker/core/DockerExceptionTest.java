/*******************************************************************************
 * Copyright (c) 2016,2018 Red Hat.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Testing the {@link DockerException} class
 */
@RunWith(Parameterized.class)
public class DockerExceptionTest {

	@Parameters
	public static Object[][] getData() {
		final Object[][] data = new Object[][] {
				new Object[] { new DockerException("this is an error"), "this is an error" },
				new Object[] { new DockerException("error with message: 232"), "error with message: 232" },
				new Object[] {
						new DockerException(
								"{\"message\":\"invalid reference format: repository name must be lowercase\"}"),
						"invalid reference format: repository name must be lowercase" }, };
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
