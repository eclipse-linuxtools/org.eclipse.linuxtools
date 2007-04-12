package org.eclipse.linuxtools.rpm.ui.editor.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.rpm.ui.editor.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(EpochTagTest.class);
		suite.addTestSuite(DefineTests.class);
		suite.addTestSuite(ReleaseTagTests.class);
		suite.addTestSuite(NameTagTests.class);
		suite.addTestSuite(VersionTagTests.class);
		suite.addTestSuite(HeaderRecognitionTest.class);
		suite.addTestSuite(LicenseTagTest.class);
//		suite.addTestSuite(ParserTests.class);
		suite.addTestSuite(PatchApplicationTest.class);
		suite.addTestSuite(LinePositionTests.class);
		suite.addTestSuite(SourceComparatorTests.class);
		suite.addTestSuite(RefactoringTests.class);
		suite.addTestSuite(RpmMacroProposalsListTests.class);
		suite.addTestSuite(RpmPackageProposalsListTests.class);
		//$JUnit-END$
		return suite;
	}

}
