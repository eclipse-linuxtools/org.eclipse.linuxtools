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

package org.eclipse.linuxtools.systemtap.graphing.core.tests.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.systemtap.graphing.core.filters.RangeFilter;
import org.eclipse.linuxtools.systemtap.graphing.core.tests.MockDataSet;
import org.junit.Before;
import org.junit.Test;

public class RangeFilterTest {

	@Before
	public void setUp() {
		filter = new RangeFilter(0, 1, 2, RangeFilter.INSIDE_BOUNDS | RangeFilter.INCLUSIVE);
	}
	@Test
	public void testRangeFilter() {
		RangeFilter filter = new RangeFilter(-1, 3, 5, RangeFilter.INSIDE_BOUNDS);
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
		assertEquals(6, data2[0].size());
		assertEquals("1", data2[0].get(0));
		assertEquals("2", data2[0].get(1));
		assertEquals("1", data2[0].get(2));
		assertEquals("2", data2[0].get(3));

		assertEquals(data[2].get(1), data2[2].get(0));
		assertEquals(data[2].get(2), data2[2].get(1));
		assertEquals(data[2].get(4), data2[2].get(2));
		assertEquals(data[2].get(5), data2[2].get(3));


		filter = new RangeFilter(0, 0, 2, RangeFilter.INSIDE_BOUNDS);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(3, data2[0].size());
		assertEquals("1", data2[0].get(0));
		assertEquals("1", data2[0].get(1));
		assertEquals("1", data2[0].get(2));

		assertEquals(data[2].get(1), data2[2].get(0));
		assertEquals(data[2].get(4), data2[2].get(1));
		assertEquals(data[2].get(7), data2[2].get(2));


		filter = new RangeFilter(0, 0, 2, RangeFilter.OUTSIDE_BOUNDS | RangeFilter.INCLUSIVE);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(7, data2[0].size());
		assertEquals("0", data2[0].get(0));
		assertEquals("2", data2[0].get(1));
		assertEquals("0", data2[0].get(2));
		assertEquals("2", data2[0].get(3));

		assertEquals(data[2].get(0), data2[2].get(0));
		assertEquals(data[2].get(2), data2[2].get(1));
		assertEquals(data[2].get(3), data2[2].get(2));
		assertEquals(data[2].get(5), data2[2].get(3));


		filter = new RangeFilter(0, 0, 2, RangeFilter.INSIDE_BOUNDS);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(3, data2[0].size());
		assertEquals("1", data2[0].get(0));
		assertEquals("1", data2[0].get(1));
		assertEquals("1", data2[0].get(2));

		assertEquals(data[2].get(1), data2[2].get(0));
		assertEquals(data[2].get(4), data2[2].get(1));
		assertEquals(data[2].get(7), data2[2].get(2));


		filter = new RangeFilter(0, 0, 2, RangeFilter.OUTSIDE_BOUNDS);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(0, data2[0].size());


		filter = new RangeFilter(-1, 1, 3, 0);
		assertNull(filter.filter(data));
	}
	@Test
	public void testUnboundedFilters() {
		int width = 4;
		int height = 10;
		int wrap = height / 3;
		ArrayList<Object>[] data = MockDataSet.buildArray(width, height, wrap);

		filter = new RangeFilter(1, 1, null, RangeFilter.INCLUSIVE);
		List<Object>[] data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[1].size());
		assertEquals(width, data2.length);
		assertEquals(7, data2[1].size());
		assertEquals("1", data2[1].get(0));
		assertEquals("2", data2[1].get(1));
		assertEquals("1", data2[1].get(2));
		assertEquals("2", data2[1].get(3));

		assertEquals(data[2].get(0), data2[2].get(0));
		assertEquals(data[2].get(1), data2[2].get(1));
		assertEquals(data[2].get(3), data2[2].get(2));
		assertEquals(data[2].get(4), data2[2].get(3));


		filter = new RangeFilter(1, 1, null, 0);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[1].size());
		assertEquals(width, data2.length);
		assertEquals(3, data2[1].size());
		assertEquals("2", data2[1].get(0));
		assertEquals("2", data2[1].get(1));
		assertEquals("2", data2[1].get(2));

		assertEquals(data[2].get(1), data2[2].get(0));
		assertEquals(data[2].get(4), data2[2].get(1));
		assertEquals(data[2].get(7), data2[2].get(2));


		filter = new RangeFilter(1, null, 1, RangeFilter.INCLUSIVE);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[1].size());
		assertEquals(width, data2.length);
		assertEquals(7, data2[1].size());
		assertEquals("1", data2[1].get(0));
		assertEquals("0", data2[1].get(1));
		assertEquals("1", data2[1].get(2));
		assertEquals("0", data2[1].get(3));

		assertEquals(data[2].get(0), data2[2].get(0));
		assertEquals(data[2].get(2), data2[2].get(1));
		assertEquals(data[2].get(3), data2[2].get(2));
		assertEquals(data[2].get(5), data2[2].get(3));


		filter = new RangeFilter(1, null, 1, 0);
		data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[1].size());
		assertEquals(width, data2.length);
		assertEquals(3, data2[1].size());
		assertEquals("0", data2[1].get(0));
		assertEquals("0", data2[1].get(1));
		assertEquals("0", data2[1].get(2));

		assertEquals(data[2].get(2), data2[2].get(0));
		assertEquals(data[2].get(5), data2[2].get(1));
		assertEquals(data[2].get(8), data2[2].get(2));
	}
	@Test
	public void testGetID() {
		assertEquals(RangeFilter.ID, filter.getID());
	}

	private RangeFilter filter;
}
