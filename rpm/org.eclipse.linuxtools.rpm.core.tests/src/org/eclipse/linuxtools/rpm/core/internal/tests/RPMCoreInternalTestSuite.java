/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.linuxtools.rpm.core.internal.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RPMCoreInternalTestSuite extends TestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.rpm.core.internal.tests");
		//$JUnit-BEGIN$
        suite.addTest(RPMProjectTest.suite());
        suite.addTest(SpecFileParserTest.suite());
		//$JUnit-END$
		return suite;
	}
}
