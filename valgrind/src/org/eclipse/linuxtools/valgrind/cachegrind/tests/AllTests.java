package org.eclipse.linuxtools.valgrind.cachegrind.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Tests for org.eclipse.linuxtools.valgrind.cachegrind.tests"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(BasicCachegrindTest.class);
		suite.addTestSuite(CModelLabelsTest.class);
		//$JUnit-END$
		return suite;
	}

}
