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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.graphing.core.filters.MatchFilter;
import org.eclipse.linuxtools.systemtap.graphing.core.tests.MockDataSet;
import org.junit.Test;

public class MatchFilterTest  {

	@Test
	public void testMatchFilter() {
		MatchFilter filter = new MatchFilter(-1, null, MatchFilter.KEEP_MATCHING);
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
		MatchFilter filter = new MatchFilter(0, data[0].get(0), MatchFilter.KEEP_MATCHING);
		ArrayList<Object>[] data2 = filter.filter(data);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals((int)Math.ceil(height/((double)wrap)), data2[0].size());
		assertEquals("0", data2[0].get(0));
		assertEquals("0", data2[0].get(1));
		assertEquals("0", data2[0].get(2));
		assertEquals("0", data2[0].get(3));

		assertEquals(data[2].get(0), data2[2].get(0));
		assertEquals(data[2].get(3), data2[2].get(1));
		assertEquals(data[2].get(6), data2[2].get(2));
		assertEquals(data[2].get(9), data2[2].get(3));

		filter = new MatchFilter(-1, "", MatchFilter.KEEP_MATCHING);
		assertNull(filter.filter(data));
	}
	@Test
	public void testGetID() {
		MatchFilter filter = new MatchFilter(0, "", MatchFilter.KEEP_MATCHING);
		assertTrue(MatchFilter.ID.equals(filter.getID()));
	}

}
