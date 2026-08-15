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
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Testing the {@link ImageNameValidator} class
 */
public class ImageNameValidatorTest {

	private static Arguments match(final String imageName, final int expectedSeverity) {
		return Arguments.of(imageName, expectedSeverity);
	}

	public static Stream<Arguments> data() {
		return Stream.of(
			match("", IStatus.CANCEL),
			match("£", IStatus.WARNING),
			match("wildfly", IStatus.WARNING),
			match("jboss/", IStatus.WARNING),
			match("jboss/wildfly", IStatus.WARNING),
			match("jboss/wildfly:", IStatus.WARNING),
			match("jboss/wildfly:latest", IStatus.OK),
			match("localhost/wildfly/", IStatus.WARNING),
			match("localhost/jboss/wildfly", IStatus.WARNING),
			match("localhost/jboss/wildfly:", IStatus.WARNING),
			match("localhost/jboss/wildfly:latest", IStatus.OK),
			match("localhost/jboss/wildfly:9", IStatus.OK),
			match("localhost/jboss/wildfly:9.", IStatus.WARNING),
			match("localhost/jboss/wildfly:9.0.1.", IStatus.WARNING),
			match("localhost/jboss/wildfly:9.0.1.Final", IStatus.OK),
			match("localhost:", IStatus.WARNING),
			match("localhost:5000", IStatus.OK), // bc it matches the REPO:TAG pattern.
			match("localhost:5000/", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly/", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly:", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly:latest", IStatus.OK));
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@MethodSource("data")
	public void verifyData(final String imageName, final int expectedSeverity) {
		final IStatus status = new ImageNameValidator().validate(imageName);
		// then
		Assertions.assertEquals(expectedSeverity, status.getSeverity());
	}

}
