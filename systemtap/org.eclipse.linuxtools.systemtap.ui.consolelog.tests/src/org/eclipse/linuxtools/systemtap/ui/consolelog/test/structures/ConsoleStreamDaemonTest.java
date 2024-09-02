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
package org.eclipse.linuxtools.systemtap.ui.consolelog.test.structures;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.linuxtools.systemtap.structures.runnable.StreamGobbler;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ConsoleStreamDaemon;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConsoleStreamDaemonTest {

	@BeforeEach
    public void setUp() {
        StreamGobbler gobbler = new StreamGobbler(System.in);
        gobbler.start();
        daemon = new ConsoleStreamDaemon(null);
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

    private ConsoleStreamDaemon daemon;
}