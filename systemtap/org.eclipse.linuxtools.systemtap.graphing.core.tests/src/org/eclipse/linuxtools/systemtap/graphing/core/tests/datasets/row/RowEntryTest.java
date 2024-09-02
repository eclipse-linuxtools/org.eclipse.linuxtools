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
package org.eclipse.linuxtools.systemtap.graphing.core.tests.datasets.row;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RowEntryTest {

    @BeforeEach
    public void setUp() {
        entry = new RowEntry();
        data = new Integer[] {2, 5, 4};
        entry.putRow(0, data);
    }

    @Test
    public void testGetRowCount() {
        RowEntry entry2 = new RowEntry();
        assertEquals(0, entry2.getRowCount());

        entry2 = new RowEntry();
        entry2.putRow(0, null);
        assertEquals(0, entry2.getRowCount());

        assertEquals(1, entry.getRowCount());
    }

    @Test
    public void testGetColCount() {
        RowEntry entry2 = new RowEntry();
        assertEquals(0, entry2.getColCount());

        entry2 = new RowEntry();
        entry2.putRow(0, null);
        assertEquals(0, entry2.getColCount());

        assertEquals(3, entry.getColCount());
    }

    @Test
    public void testGet() {
        assertEquals(data[1], entry.get(null, 1));
        assertEquals(data[1], entry.get("asdf", 1));
        assertNull(entry.get(null, 10));
        assertNull(entry.get(null, -1));
    }

    @Test
    public void testGetRow() {
        assertArrayEquals(data, entry.getRow(0));
        assertNull(entry.getRow(10));
        assertNull(entry.getRow(-1));

        assertArrayEquals(data, entry.getRow(null));
        assertArrayEquals(data, entry.getRow("asdf"));
    }

    @Test
    public void testGetColumn() {
        assertEquals(data[1], entry.getColumn(1)[0]);
        assertNull(entry.getColumn(10));
        assertNull(entry.getColumn(-1));
    }

    @Test
    public void testGetData() {
        assertEquals(data[0], entry.getData()[0][0]);
        assertEquals(data[1], entry.getData()[0][1]);
    }

    @Test
    public void testCopy() {
        IDataEntry entry2 = entry.copy();
        assertEquals(entry2.getRowCount(), entry.getRowCount());
        assertEquals(entry2.getColCount(), entry.getColCount());
        assertSame(entry2.getRow(0)[1], entry.getRow(0)[1]);
    }

    @Test
    public void testPutRow() {
        Integer[] data2 = new Integer[] {2, 5};

        //Can't add to -1 position
        entry.putRow(-1, data2);
        assertEquals(3, entry.getColCount());

        //Cant add to non 0 position
        entry.putRow(10, data2);
        assertEquals(3, entry.getColCount());

        //Add successful
        entry.putRow(0, data2);
        assertEquals(2, entry.getColCount());
    }

    @Test
    public void testRemove() {
        assertFalse(entry.remove(-1));
        assertEquals(3, entry.getColCount());
        assertFalse(entry.remove(10));
        assertEquals(3, entry.getColCount());

        assertTrue(entry.remove(0));
        assertEquals(0, entry.getColCount());
    }

    private RowEntry entry;
    private Integer[] data;
}
