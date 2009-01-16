package org.eclipse.linuxtools.oprofile.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCoreTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.linuxtools.oprofile.core.tests"); //$NON-NLS-1$
		
		suite.addTestSuite(TestModelDataParse.class);
		
		return suite;
	}

}
