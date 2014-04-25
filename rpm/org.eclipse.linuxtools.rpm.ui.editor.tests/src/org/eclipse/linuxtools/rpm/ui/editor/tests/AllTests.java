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

import org.eclipse.linuxtools.internal.rpm.ui.editor.actions.tests.ActionsAllTests;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.tests.HyperlinkAllTests;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.tests.ScannersAllTests;
import org.eclipse.linuxtools.rpm.ui.editor.tests.parser.ParserAllTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ EpochTagTest.class, ReleaseTagTest.class,
        NameTagTest.class, VersionTagTest.class, HeaderRecognitionTest.class,
        LicenseTagTest.class, PatchApplicationTest.class,
        LinePositionTest.class, RpmMacroProposalsListTest.class,
        RpmPackageProposalsListTest.class, ActionsAllTests.class,
        ScannersAllTests.class, ParserAllTests.class, HyperlinkAllTests.class,
        SpecfileCompletionProcessorTest.class, LabelProviderTest.class })
public class AllTests {
}
