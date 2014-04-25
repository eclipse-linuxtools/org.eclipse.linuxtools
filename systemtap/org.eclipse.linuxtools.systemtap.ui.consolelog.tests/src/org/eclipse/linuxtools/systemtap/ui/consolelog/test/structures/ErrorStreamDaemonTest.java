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

package org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.structures.runnable.StreamGobbler;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ConsoleStreamDaemon;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ErrorStreamDaemon;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.junit.Before;
import org.junit.Test;

public class ErrorStreamDaemonTest {

    @Before
    public void setUp() {
        StreamGobbler gobbler = new StreamGobbler(System.in);
        gobbler.start();
        daemon = new ErrorStreamDaemon(null, null, null);
    }

    @Test
    public void testConsoleStreamDaemon() {
        assertNotNull(daemon);

        ConsoleStreamDaemon csd = new ConsoleStreamDaemon(null);
        assertNotNull(csd);

        csd = new ConsoleStreamDaemon(ScriptConsole.getInstance("test"));
        assertNotNull(csd);
    }

    @Test
    public void testHandleDataEvent() {
        daemon.handleDataEvent("");
        assertNotNull(daemon);
    }
    @Test
    public void testIsDisposed() {
        ConsoleStreamDaemon csd = new ConsoleStreamDaemon(null);
        assertFalse(csd.isDisposed());
        csd.dispose();
        assertTrue(csd.isDisposed());
    }
    @Test
    public void testDispose() {
        daemon.dispose();
        assertNotNull(daemon);
    }

    private ErrorStreamDaemon daemon;
}
