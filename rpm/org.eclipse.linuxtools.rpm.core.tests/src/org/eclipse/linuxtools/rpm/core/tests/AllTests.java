/*******************************************************************************
 * Copyright (c) 2004, 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite{

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.linuxtools.rpm.core.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(RPMProjectTest.class);
		suite.addTestSuite(RPMProjectNatureTest.class);
		//$JUnit-END$
		return suite;
	}
}
