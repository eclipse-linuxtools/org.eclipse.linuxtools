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
package org.eclipse.linuxtools.systemtap.graphing.core.tests.structures;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowParser;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.ChartStreamDaemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChartStreamDaemonTest {

    @BeforeEach
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