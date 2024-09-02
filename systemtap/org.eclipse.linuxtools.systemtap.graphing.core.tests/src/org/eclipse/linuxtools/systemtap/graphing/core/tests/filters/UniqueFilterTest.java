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
package org.eclipse.linuxtools.systemtap.graphing.core.tests.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.MaxAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.aggregates.SumAggregate;
import org.eclipse.linuxtools.systemtap.graphing.core.filters.UniqueFilter;
import org.eclipse.linuxtools.systemtap.graphing.core.tests.MockDataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UniqueFilterTest  {
    @BeforeEach
    public void setUp() {
        filter = new UniqueFilter(0, new SumAggregate());
    }
    @Test
    public void testUniqueFilter() {
        filter = new UniqueFilter(-1, new MaxAggregate());
        assertNotNull(filter);
    }
    @Test
    public void testFilter() {
        int width = 4;
        int height = 10;
        int wrap = height / 3;
        ArrayList<Object>[] data = MockDataSet.buildArray(width, height, wrap);

        assertEquals(width, data.length);
        assertEquals(height, data[0].size());

        List<Object>[] data2 = filter.filter(data);

        assertEquals(width, data.length);
        assertEquals(height, data[0].size());
        assertEquals(width, data2.length);
        assertEquals(wrap, data2[0].size());
        assertEquals("1", data2[0].get(0));
        assertEquals("2", data2[0].get(1));
        assertEquals("0", data2[0].get(2));

        assertEquals(0, ((Number)data2[2].get(0)).intValue());
        assertEquals(3, ((Number)data2[2].get(1)).intValue());
        assertEquals(8, ((Number)data2[2].get(2)).intValue());

        filter = new UniqueFilter(-1, new SumAggregate());
        assertNull(filter.filter(null));

        data = MockDataSet.createArrayList(2, new Object());
        data[0] = new ArrayList<>();
        data[1] = new ArrayList<>();

        data[0].add("a");
        data[0].add("a");
        data[1].add("b");
        data[1].add("c");
        filter = new UniqueFilter(0, new SumAggregate());
        assertNotNull(filter.filter(data));
    }
    @Test
    public void testGetID() {
        assertEquals(UniqueFilter.ID, filter.getID());
    }

    private UniqueFilter filter;
}
