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
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CCodeFileFilterTest.class, CommandTest.class,
		CopierTest.class, IndexedObjectTest.class, KernelSourceTreeTest.class,
		LoggingStreamDaemonTest.class, SortTest.class, StreamGobblerTest.class,
		StringFormatterTest.class, TreeDefinitionNodeTest.class,
		TreeNodeTest.class,
		ZipArchiveTest.class,

		// structures.validators
		ConditionalExpressionValidatorTest.class, DirectoryValidatorTest.class })
public class AllTests {
}
