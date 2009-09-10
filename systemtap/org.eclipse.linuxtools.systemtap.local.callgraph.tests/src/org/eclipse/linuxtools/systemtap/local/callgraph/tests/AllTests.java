package org.eclipse.linuxtools.systemtap.local.callgraph.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.systemtap.local.callgraph.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(StapGraphParserTest.class);
		suite.addTestSuite(SystemTapGraphViewTest.class);
		suite.addTestSuite(SystemTapGraphTest.class);
		//$JUnit-END$
		return suite;
	}

}
