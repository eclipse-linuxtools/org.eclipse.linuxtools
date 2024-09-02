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

package org.eclipse.linuxtools.systemtap.structures.tests.runnable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.eclipse.linuxtools.systemtap.structures.runnable.StreamGobbler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StreamGobblerTest{

    private static class TestStream extends InputStream{
        int i = 10;
        @Override
        public int read() {
            if (i < 0)
                return -1;

            return i--;
        }
    }

    @BeforeEach
    public void setUp() {
        sg = new StreamGobbler(new TestStream());
        sg.start();
    }

    @Test
    public void testStreamGobbler() {
        assertNotNull(sg, "StreamGobbler not null");

        sg = new StreamGobbler(null);
        assertNotNull(sg, "StreamGobbler not null");

        sg = new StreamGobbler(new TestStream());
        assertNotNull(sg, "StreamGobbler not null");
    }

    @Test
    public void testIsRunning() {
        assertTrue(sg.isRunning(), "StreamGobbler running");
        sg.stop();
        assertFalse(sg.isRunning(), "StreamGobbler stopped");
    }

    @Test
    public void testStop() {
        assertTrue(sg.isRunning(), "StreamGobbler running");
        sg.stop();
        assertFalse(sg.isRunning(), "StreamGobbler stopped");
    }

    @Test
    public void testDispose() {
        sg.dispose();
        assertFalse(sg.isRunning());
    }

    StreamGobbler sg;
}
