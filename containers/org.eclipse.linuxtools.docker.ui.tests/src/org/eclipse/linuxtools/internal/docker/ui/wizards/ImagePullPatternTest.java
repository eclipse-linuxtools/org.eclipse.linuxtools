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
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.internal.docker.ui.validators.ImageNameValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ImagePullPatternTest { 
	
	private static Object[] match(final String imageName, final int expectedSeverity) {
		return new Object[]{imageName, expectedSeverity};
	}
	
	@Parameters(name="{0} -> {1}")
	public static Object[][] data() {
		return new Object[][] {
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
			match("localhost/jboss/wildfly:latest", IStatus.WARNING),
			match("localhost/jboss/wildfly:9", IStatus.WARNING),
			match("localhost/jboss/wildfly:9.", IStatus.WARNING),
			match("localhost/jboss/wildfly:9.0.1.", IStatus.WARNING),
			match("localhost/jboss/wildfly:9.0.1.Final", IStatus.WARNING),
			match("localhost:", IStatus.WARNING),
			match("localhost:5000", IStatus.OK), // bc it matches the REPO:TAG pattern.
			match("localhost:5000/", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly/", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly:", IStatus.WARNING),
			match("localhost:5000/jboss/wildfly:latest", IStatus.OK),
		};
	}
	
	@Parameter(value=0)
	public String imageName;
	@Parameter(value=1)
	public int expectedSeverity;
	
	
	@Test
	public void verifyData() {
		final IStatus status = new ImageNameValidator().validate(imageName);
		// then
		Assert.assertEquals(expectedSeverity, status.getSeverity());
	}

}
