/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.junit.Before;
import org.junit.Test;

public class ScriptConsoleTest {

    @Before
    public void setUp() {
        console = ScriptConsole.getInstance("test");
    }
    @Test
    public void testGetInstance() {
        assertNotNull(console);
        assertSame(console, ScriptConsole.getInstance("test"));
        ScriptConsole console2 = ScriptConsole.getInstance("a");
        assertNotNull(console2);
        assertNotSame(console, console2);
    }

    private ScriptConsole console;
}
