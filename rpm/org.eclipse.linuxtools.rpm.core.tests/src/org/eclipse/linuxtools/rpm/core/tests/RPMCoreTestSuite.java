/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RPMCoreTestSuite extends TestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.rpm.core.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(RPMProjectNatureTest.class);
		suite.addTestSuite(RPMProjectFactoryTest.class);
		//$JUnit-END$
		return suite;
	}
}
