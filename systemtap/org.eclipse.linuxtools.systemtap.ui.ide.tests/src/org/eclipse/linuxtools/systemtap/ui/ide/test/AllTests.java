/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
