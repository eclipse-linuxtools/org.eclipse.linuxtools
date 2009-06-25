/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.internal.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RPMCoreInternalTestSuite extends TestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.rpm.core.internal.tests");
		//$JUnit-BEGIN$
        suite.addTest(RPMProjectTest.suite());
		//$JUnit-END$
		return suite;
	}
}
