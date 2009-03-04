/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
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

import org.eclipse.linuxtools.rpm.ui.editor.tests.actions.ActionsAllTests;
import org.eclipse.linuxtools.rpm.ui.editor.tests.hyperlink.HyperlinkAllTests;
import org.eclipse.linuxtools.rpm.ui.editor.tests.parser.ParserAllTests;
import org.eclipse.linuxtools.rpm.ui.editor.tests.scanners.ScannersAllTests;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.rpm.ui.editor.tests");
		// $JUnit-BEGIN$
		suite.addTestSuite(EpochTagTest.class);
		suite.addTestSuite(ReleaseTagTests.class);
		suite.addTestSuite(NameTagTests.class);
		suite.addTestSuite(VersionTagTests.class);
		suite.addTestSuite(HeaderRecognitionTest.class);
		suite.addTestSuite(LicenseTagTest.class);
		suite.addTestSuite(PatchApplicationTest.class);
		suite.addTestSuite(LinePositionTests.class);
		suite.addTestSuite(RpmMacroProposalsListTest.class);
		suite.addTestSuite(RpmPackageProposalsListTest.class);
		suite.addTest(ActionsAllTests.suite());
		suite.addTest(ScannersAllTests.suite());
		suite.addTest(ParserAllTests.suite());
		suite.addTest(HyperlinkAllTests.suite());
		suite.addTestSuite(SpecfileCompletionProcessorTest.class);
		// $JUnit-END$
		return suite;
	}

}
