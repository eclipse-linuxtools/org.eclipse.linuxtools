/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.validators;

import org.eclipse.linuxtools.internal.docker.ui.launch.BuildDockerImageUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Testing the {@link BuildDockerImageUtils}.
 */
@RunWith(Parameterized.class)
public class BuildDockerImageUtilsTest {

	private static Object[] match(final String imageName, final String expectedRepository, final String expectedName, final String expectedTag) {
		return new Object[] { imageName, expectedRepository, expectedName, expectedTag };
	}

	@Parameters(name = "{0} -> {1}/{2}:{3}")
	public static Object[][] data() {
		return new Object[][] { match("", null, null, null),
			match("£", null, null, null), // because £ is an invalid character
			match("wildfly", null, "wildfly", null),
			match("jboss/", null, null, null),
			match("jboss/wildfly", "jboss", "wildfly", null),
			match("jboss/wildfly:", null , null, null), // because ':' causes invalid value
			match("jboss/wildfly:latest", "jboss", "wildfly", "latest"),
			match("localhost/wildfly/", null, null, null), // because registry is missing port number
			match("localhost/jboss/wildfly", null, null, null), // because registry is missing port number
			match("localhost/jboss/wildfly:", null, null, null), // because registry is missing port number
			match("localhost/jboss/wildfly:latest", null, null, null), // because registry is missing port number
			match("localhost/jboss/wildfly:9", null, null, null), // because registry is missing port number
			match("localhost/jboss/wildfly:9.", null, null, null), // because registry is missing port number
			match("localhost/jboss/wildfly:9.0.1.Final", null, null, null), // because registry is missing port number
			match("localhost:", null, null, null), // because trailing ':' causes invalid value
			match("localhost:5000", null, "localhost", "5000"), // bc it matches the REPO:TAG pattern.
			match("localhost:5000/", null, null, null), // because trailing '/' causes invalid value
			match("localhost:5000/jboss/wildfly", "jboss", "wildfly", null),
			match("localhost:5000/jboss/wildfly/", null, null, null), // because trailing '/' causes invalid value
			match("localhost:5000/jboss/wildfly", "jboss", "wildfly", null),
			match("localhost:5000/jboss/wildfly:", null, null, null), // because ':' causes invalid value
			match("localhost:5000/jboss/wildfly:latest", "jboss", "wildfly", "latest"),
		};
	}

	@Parameter(value = 0)
	public String imageName;
	@Parameter(value = 1)
	public String expectedRepository;
	@Parameter(value = 2)
	public String expectedName;
	@Parameter(value = 3)
	public String expectedTag;

	@Test
	public void verifyRepository() {
		// when
		final String actualRepository = BuildDockerImageUtils.getRepository(imageName);
		// then
		Assert.assertEquals(expectedRepository, actualRepository);
	}

	@Test
	public void verifyName() {
		// when
		final String actualName = BuildDockerImageUtils.getName(imageName);
		// then
		Assert.assertEquals(expectedName, actualName);
	}
	
	@Test
	public void verifyTag() {
		// when
		final String actualTagName = BuildDockerImageUtils.getTag(imageName);
		// then
		Assert.assertEquals(expectedTag, actualTagName);
	}
	
}
