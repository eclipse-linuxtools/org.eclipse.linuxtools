/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and ohters.
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
package org.eclipse.linuxtools.systemtap.structures.tests.runnable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.systemtap.structures.runnable.Command;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandTest {

    @BeforeEach
    public void setUp() {
        tc = new Command(new String[] {"ls", "/home/"});
    }

    @Test
    public void testCommand() {
        assertNotNull(tc, "Command not null");

        tc.dispose();
        tc = new Command(null);
        assertNotNull(tc, "Command not null");

        tc.dispose();
        tc = new Command(new String[] {});
        assertNotNull(tc, "Command not null");

        tc.dispose();
        tc = new Command(new String[] {""});
        assertNotNull(tc, "Command not null");

        tc.dispose();
        tc = new Command(new String[] {"a"});
        assertNotNull(tc, "Command not null");

        tc.dispose();
        tc = new Command(new String[] {"ls", "/"});
        assertNotNull(tc, "Command not null");
    }

    @Test
    public void testIsFinished() {
        assertTrue(tc.isRunning(), "Not finished");
        tc.stop();
        assertFalse(tc.isRunning(), "Finished");
    }

    @Test
    public void testGetReturnValue() {
        assertEquals(Integer.MAX_VALUE, tc.getReturnValue());
    }

    @Test
    public void testLoggedCommand() throws CoreException {
        tc.dispose();

        tc = new Command(new String[] {"ls", "/doesnotexist/"});
        tc.start();
        assertTrue(tc.isRunning());
        assertFalse(tc.isDisposed());
        tc.stop();
        assertFalse(tc.isRunning());
        assertFalse(tc.isDisposed());
        tc.dispose();

        tc = new Command(new String[] {"ls", "/doesnotexist/"});
        tc.start();
        assertTrue(tc.isRunning());
        assertFalse(tc.isDisposed());
        tc.stop();
        assertFalse(tc.isRunning());
        assertFalse(tc.isDisposed());
        tc.dispose();
    }

    @Test
    public void testStop() throws CoreException {
        tc.start();
        assertTrue(tc.isRunning());
        tc.stop();
        assertFalse(tc.isRunning());
    }

    @Test
    public void testDispose() {
        assertFalse(tc.isDisposed());
        tc.dispose();
        assertTrue(tc.isDisposed());
    }

    @AfterEach
    public void tearDown() {
        tc.dispose();
        assertTrue(tc.isDisposed());
    }

    Command tc;
}

