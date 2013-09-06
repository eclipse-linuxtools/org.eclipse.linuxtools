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

package org.eclipse.linuxtools.systemtap.graphingapi.core.tests.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.SortFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.tests.MockDataSet;
import org.junit.Before;
import org.junit.Test;

public class SortFilterTest {
	@Before
	public void setUp() {
		filter = new SortFilter(0, SortFilter.ASCENDING);
	}
	@Test
	public void testSortFilter() {
		SortFilter filter = new SortFilter(-1, SortFilter.ASCENDING);
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
		ArrayList<Object>[] data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(height, data2[0].size());
		assertEquals("0", data2[0].get(0));
		assertEquals("0", data2[0].get(1));
		assertEquals("0", data2[0].get(2));
		assertEquals("0", data2[0].get(3));
		assertEquals("1", data2[0].get(4));
		assertEquals("2", data2[0].get(7));

		assertEquals(data[2].get(0), data2[2].get(0));
		assertEquals(data[2].get(3), data2[2].get(1));
		assertEquals(data[2].get(6), data2[2].get(2));
		assertEquals(data[2].get(9), data2[2].get(3));


		filter = new SortFilter(0, SortFilter.DESCENDING);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(height, data2[0].size());
		assertEquals("2", data2[0].get(0));
		assertEquals("2", data2[0].get(1));
		assertEquals("2", data2[0].get(2));
		assertEquals("1", data2[0].get(3));
		assertEquals("0", data2[0].get(7));

		assertEquals(data[2].get(2), data2[2].get(0));
		assertEquals(data[2].get(5), data2[2].get(1));
		assertEquals(data[2].get(8), data2[2].get(2));
		assertEquals(data[2].get(9), data2[2].get(7));

		filter = new SortFilter(-1, 0);
		assertNull(filter.filter(data));

		data[0].add("a");
		data[0].add("a");
		data[1].add("b");
		data[1].add("c");
		filter = new SortFilter(0, 0);
		assertNotNull(filter.filter(data));
	}
	@Test
	public void testGetID() {
		assertTrue(SortFilter.ID.equals(filter.getID()));
	}

	private SortFilter filter;
}
