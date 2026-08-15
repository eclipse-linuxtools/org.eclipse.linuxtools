/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.validators;

import java.util.stream.Stream;

import org.eclipse.linuxtools.internal.docker.ui.launch.BuildDockerImageUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Testing the {@link BuildDockerImageUtils}.
 */
public class BuildDockerImageUtilsTest {

	private static Arguments match(final String imageName, final String expectedRepository, final String expectedName, final String expectedTag) {
		return Arguments.of(imageName, expectedRepository, expectedName, expectedTag);
	}

	public static Stream<Arguments> data() {
		return Stream.of(match("", null, null, null),
			match("£", null, null, null), // because £ is an invalid character
			match("wildfly", null, "wildfly", null),
			match("jboss/", null, null, null),
			match("jboss/wildfly", "jboss", "wildfly", null),
			match("jboss/wildfly:", null , null, null), // because ':' causes invalid value
			match("jboss/wildfly:latest", "jboss", "wildfly", "latest"),
			match("localhost/wildfly/", null, null, null), // because registry is missing port number
			match("localhost/jboss/wildfly", "localhost/jboss", "wildfly", null),
			match("localhost/jboss/wildfly:", null, null, null), // because registry is missing port number
			match("localhost/jboss/wildfly:latest", "localhost/jboss", "wildfly", "latest"),
			match("localhost/jboss/wildfly:9", "localhost/jboss", "wildfly", "9"),
			match("localhost/jboss/wildfly:9.", null, null, null), // because . is invalid for tag end
			match("localhost/jboss/wildfly:9.0.1.Final", "localhost/jboss", "wildfly", "9.0.1.Final"),
			match("localhost:", null, null, null), // because trailing ':' causes invalid value
			match("localhost:5000", null, "localhost", "5000"), // bc it matches the REPO:TAG pattern.
			match("localhost:5000/", null, null, null), // because trailing '/' causes invalid value
			match("localhost:5000/jboss/wildfly", "jboss", "wildfly", null),
			match("localhost:5000/jboss/wildfly/", null, null, null), // because trailing '/' causes invalid value
			match("localhost:5000/jboss/wildfly", "jboss", "wildfly", null),
			match("localhost:5000/jboss/wildfly:", null, null, null), // because ':' causes invalid value
			match("localhost:5000/jboss/wildfly:latest", "jboss", "wildfly", "latest"));
	}

	@ParameterizedTest(name = "{0} -> {1}/{2}:{3}")
	@MethodSource("data")
	public void verifyRepository(final String imageName, final String expectedRepository, final String expectedName,
			final String expectedTag) {
		// when
		final String actualRepository = BuildDockerImageUtils.getRepository(imageName);
		// then
		Assertions.assertEquals(expectedRepository, actualRepository);
	}

	@ParameterizedTest(name = "{0} -> {1}/{2}:{3}")
	@MethodSource("data")
	public void verifyName(final String imageName, final String expectedRepository, final String expectedName,
			final String expectedTag) {
		// when
		final String actualName = BuildDockerImageUtils.getName(imageName);
		// then
		Assertions.assertEquals(expectedName, actualName);
	}

	@ParameterizedTest(name = "{0} -> {1}/{2}:{3}")
	@MethodSource("data")
	public void verifyTag(final String imageName, final String expectedRepository, final String expectedName,
			final String expectedTag) {
		// when
		final String actualTagName = BuildDockerImageUtils.getTag(imageName);
		// then
		Assertions.assertEquals(expectedTag, actualTagName);
	}

}
