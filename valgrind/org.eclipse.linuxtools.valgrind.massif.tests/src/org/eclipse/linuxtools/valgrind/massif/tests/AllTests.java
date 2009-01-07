package org.eclipse.linuxtools.valgrind.massif.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.valgrind.massif.tests"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(DoubleClickTest.class);
		suite.addTestSuite(TreeTest.class);
		suite.addTestSuite(BasicMassifTest.class);
		//$JUnit-END$
		return suite;
	}

}
