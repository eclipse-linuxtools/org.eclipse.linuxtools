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
