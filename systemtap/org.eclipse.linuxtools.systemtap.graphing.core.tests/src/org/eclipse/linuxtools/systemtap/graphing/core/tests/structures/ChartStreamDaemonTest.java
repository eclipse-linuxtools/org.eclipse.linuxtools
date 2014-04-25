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

package org.eclipse.linuxtools.systemtap.graphing.core.tests.structures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowParser;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.ChartStreamDaemon;
import org.junit.Before;
import org.junit.Test;

public class ChartStreamDaemonTest {

    @Before
    public void setUp() {
        csd = new ChartStreamDaemon(null, null);
        assertNotNull(csd);

        csd1 = new ChartStreamDaemon(new RowDataSet(new String[] {"a"}), new RowParser(new String[] {"\\w", "\\s"}));
        assertNotNull(csd1);
    }
    @Test
    public void testHandleEvent() {
        csd.handleDataEvent("a a a");
        csd1.handleDataEvent("a a a");
    }
    @Test
    public void testIsDisposed() {
        assertFalse(csd1.isDisposed());
    }
    @Test
    public void testDispose() {
        assertFalse(csd1.isDisposed());
        csd1.dispose();
        assertTrue(csd1.isDisposed());
    }

    private ChartStreamDaemon csd, csd1;
}