/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

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
		suite.addTestSuite(PatchApplicationTest.class);
		suite.addTestSuite(LinePositionTests.class);
		suite.addTestSuite(SourceComparatorTests.class);
		suite.addTestSuite(RefactoringTests.class);
		suite.addTestSuite(RpmMacroProposalsListTests.class);
		suite.addTestSuite(RpmPackageProposalsListTests.class);
		suite.addTestSuite(HyperlinkWithMacroTests.class);
		suite.addTestSuite(SpecfilePackagesScannerTests.class);
		suite.addTestSuite(SpecfileScannerTests.class);
		suite.addTestSuite(SpecfileChangelogScannerTests.class);
		suite.addTestSuite(SpecfileCompletionProcessorTest.class);
		//$JUnit-END$
		return suite;
	}

}
