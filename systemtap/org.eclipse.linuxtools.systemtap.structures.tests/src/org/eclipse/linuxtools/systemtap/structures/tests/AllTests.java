/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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

package org.eclipse.linuxtools.systemtap.structures.tests;

import org.eclipse.linuxtools.systemtap.structures.tests.runnable.CommandTest;
import org.eclipse.linuxtools.systemtap.structures.tests.runnable.StreamGobblerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CCodeFileFilterTest.class, CommandTest.class,
        CopierTest.class, IndexedObjectTest.class, KernelSourceTreeTest.class,
        LoggingStreamDaemonTest.class, SortTest.class, StreamGobblerTest.class,
        StringFormatterTest.class, TreeDefinitionNodeTest.class,
        TreeNodeTest.class})
public class AllTests {
}
