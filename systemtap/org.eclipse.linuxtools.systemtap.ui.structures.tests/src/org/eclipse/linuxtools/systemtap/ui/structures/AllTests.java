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

package org.eclipse.linuxtools.systemtap.ui.structures;

import org.eclipse.linuxtools.systemtap.ui.structures.runnable.CommandTest;
import org.eclipse.linuxtools.systemtap.ui.structures.runnable.StreamGobblerTest;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.ConditionalExpressionValidatorTest;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.DirectoryValidatorTest;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.IntegerValidatorTest;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.MultiValidatorTest;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.NumberValidatorTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.linuxtools.systemtap.ui.structures");

		//Structures
		suite.addTestSuite(CCodeFileFilterTest.class);
		suite.addTestSuite(CommandTest.class);
		suite.addTestSuite(CopierTest.class);
		suite.addTestSuite(IndexedObjectTest.class);
		suite.addTestSuite(JarArchiveTest.class);
		suite.addTestSuite(KernelSourceTreeTest.class);
		suite.addTestSuite(LoggingStreamDaemonTest.class);
		suite.addTestSuite(SortTest.class);
		suite.addTestSuite(StreamGobblerTest.class);
		suite.addTestSuite(StringFormatterTest.class);
		suite.addTestSuite(TreeDefinitionNodeTest.class);
		suite.addTestSuite(TreeNodeTest.class);
		suite.addTestSuite(ZipArchiveTest.class);

		//structures.validators
		suite.addTestSuite(ConditionalExpressionValidatorTest.class);
		suite.addTestSuite(DirectoryValidatorTest.class);
		suite.addTestSuite(IntegerValidatorTest.class);
		suite.addTestSuite(MultiValidatorTest.class);
		suite.addTestSuite(NumberValidatorTest.class);
		
		return suite;
	}
}
