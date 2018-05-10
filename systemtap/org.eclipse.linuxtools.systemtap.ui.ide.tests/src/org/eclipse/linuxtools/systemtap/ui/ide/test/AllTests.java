/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.ide.test;

import org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp.STPCompletionProcessorTest;
import org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp.STPFormattingTest;
import org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp.STPIndenterTest;
import org.eclipse.linuxtools.systemtap.ui.ide.test.editors.stp.STPToggleCommentTest;
import org.eclipse.linuxtools.systemtap.ui.ide.test.structures.StapErrorParserTest;
import org.eclipse.linuxtools.systemtap.ui.ide.test.structures.TreeSettingsTest;
import org.eclipse.linuxtools.systemtap.ui.ide.test.swtbot.TestCreateSystemtapScript;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ StapErrorParserTest.class, TreeSettingsTest.class,
        STPCompletionProcessorTest.class, STPToggleCommentTest.class,
        TestCreateSystemtapScript.class, ConditionalExpressionValidatorTest.class,
        DirectoryValidatorTest.class, STPFormattingTest.class, STPIndenterTest.class })
public class AllTests {
}
