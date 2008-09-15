package org.eclipse.linuxtools.cdt.autotools.tests;

import org.eclipse.linuxtools.cdt.autotools.tests.editors.AutomakeColourizationTests;
import org.eclipse.linuxtools.cdt.autotools.tests.editors.AutomakeEditorTests;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.cdt.autotools.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(AutotoolsProjectTest0.class);
		suite.addTestSuite(AutotoolsProjectTest1.class);
		suite.addTestSuite(AutotoolsProjectTest2.class);
		suite.addTestSuite(AutotoolsProjectNatureTest.class);
		suite.addTestSuite(AutomakeEditorTests.class);
		suite.addTestSuite(AutomakeColourizationTests.class);
		suite.addTestSuite(org.eclipse.linuxtools.cdt.autotools.tests.autoconf.AllTests.class);
		//$JUnit-END$
		return suite;
	}

}
