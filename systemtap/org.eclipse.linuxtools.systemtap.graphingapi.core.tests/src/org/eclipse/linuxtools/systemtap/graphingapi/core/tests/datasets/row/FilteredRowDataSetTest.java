/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.core.tests.datasets.row;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.FilteredRowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.RangeFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.SortFilter;
import org.junit.Before;
import org.junit.Test;

public class FilteredRowDataSetTest  {

	@Before
	public void setUp() {
		data = new RowDataSet(new String[] {"a", "b", "c"});
		fdata = new FilteredRowDataSet(data);
		new FilteredRowDataSet(data.getTitles());
		
		entry0 = new RowEntry();
		entry0.putRow(0, new String[] {"1", "2", "3"});
		data.setData(entry0);
		RowEntry entry = new RowEntry();
		entry.putRow(0, new String[] {"4", "5", "6"});
		data.setData(entry);
	}
	
	//Overwrite methods to insure data is removed from the original DataSet
	@Test
	public void testAppend() {
		assertEquals(2, data.getEntryCount());
		RowEntry entry = new RowEntry();
		entry.putRow(0, new String[] {"1", "2", "3"});
		fdata.append(entry);
		assertEquals(3, data.getEntryCount());
	}
	
	@Test
	public void testRemove() {
		assertFalse(fdata.remove(null));
		assertFalse(fdata.remove(new RowEntry()));
		assertFalse(fdata.remove(-1));
		assertFalse(fdata.remove(10));
		assertEquals(2, fdata.getEntryCount());

		IDataEntry entry = data.getEntry(0);
		assertTrue(fdata.remove(entry));
		assertEquals(1, fdata.getEntryCount());
		assertFalse(fdata.remove(entry));
		assertTrue(fdata.remove(0));
	}
	//End overwrite methods to insure data is removed from the original DataSet
	
 	//Overwrite to ensure the data returned has all the filters applied
	@Test
	public void testGetColumn() {
		assertNull(fdata.getColumn(-3));
		assertNull(fdata.getColumn(10));

		assertNull(fdata.getColumn(-3, 0, 1));
		assertNull(fdata.getColumn(10, 0, 1));
		assertNull(fdata.getColumn(1, 3, 1));
		assertNull(fdata.getColumn(1, -2, 1));
		assertNull(fdata.getColumn(1, 0, 20));
		
		Object[] col = fdata.getColumn(0);
		assertEquals(2, col.length);
		assertSame("1", col[0]);
		assertSame("4", col[1]);
		
		col = fdata.getColumn(IDataSet.COL_ROW_NUM);
		assertEquals(2, col.length);
		assertEquals("1", col[0].toString());
		assertEquals("2", col[1].toString());
		
		col = fdata.getColumn(1, 0, 1);
		assertEquals(1, col.length);
		assertSame("2", col[0]);
	}

	@Test
	public void testGetRow() {
		assertNull(fdata.getRow(-3));
		assertNull(fdata.getRow(10));

		Object[] row = fdata.getRow(1);
		assertEquals(3, row.length);
		assertSame("5", row[1]);
	}
	
	@Test
	public void testGetHistoricalData() {
		assertNull(fdata.getHistoricalData(null, -3));
		assertNull(fdata.getHistoricalData(null, 10));

		assertNull(fdata.getHistoricalData(null, -3, 0, 1));
		assertNull(fdata.getHistoricalData(null, 10, 0, 1));
		assertNull(fdata.getHistoricalData(null, 1, 3, 1));
		assertNull(fdata.getHistoricalData(null, 1, -2, 1));
		assertNull(fdata.getHistoricalData(null, 1, 0, 20));
		
		Object[] col = fdata.getHistoricalData(null, 0);
		assertEquals(2, col.length);
		assertSame("1", col[0]);
		assertSame("4", col[1]);
		
		col = fdata.getHistoricalData(null, IDataSet.COL_ROW_NUM);
		assertEquals(2, col.length);
		assertEquals("1", col[0].toString());
		assertEquals("2", col[1].toString());
		
		col = fdata.getHistoricalData(null, 1, 0, 1);
		assertEquals(1, col.length);
		assertSame("2", col[0]);
	}
	
	@Test
	public void testGetEntryCount() {
		assertEquals(2, fdata.getEntryCount());
	}
	
	@Test
	public void testGetEntry() {
		assertNull(fdata.getEntry(-1));
		assertNull(fdata.getEntry(20));
		assertEquals(entry0, data.getEntry(0));
	}
 	//End overwrite to ensure the data returned has all the filters applied

	//IFilteredDataSet Methods
	@Test
	public void testAddFilter() {
		data.remove(0);
		data.remove(0);
		
		RowEntry entry;

		entry = new RowEntry();
		entry.putRow(0, new Integer[] {3, 2, 5});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {4, 2, 3});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {7, 2, 9});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {2, 2, 6});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {5, 2, 2});
		data.append(entry);
		
		fdata.addFilter(new RangeFilter(0, 3, 5, RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS));

		assertEquals(3, fdata.getRowCount());
		Object[] row = fdata.getRow(1);
		assertEquals(4, ((Integer)row[0]).intValue());
		assertEquals(3, ((Integer)row[2]).intValue());

		row = fdata.getRow(2);
		assertEquals(5, ((Integer)row[0]).intValue());
		assertEquals(2, ((Integer)row[1]).intValue());


		fdata.addFilter(new SortFilter(2, SortFilter.ASCENDING));

		assertEquals(3, fdata.getRowCount());
		row = fdata.getRow(0);
		assertEquals(5, ((Integer)row[0]).intValue());
		assertEquals(2, ((Integer)row[2]).intValue());

		row = fdata.getRow(1);
		assertEquals(4, ((Integer)row[0]).intValue());
		assertEquals(3, ((Integer)row[2]).intValue());
	}
	
	@Test
	public void testRemoveFilter() {
		data.remove(0);
		data.remove(0);
		
		RowEntry entry;
		
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {3, 2, 5});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {4, 2, 3});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {7, 2, 9});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {2, 2, 6});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {5, 2, 2});
		data.append(entry);
		
		RangeFilter filter = new RangeFilter(0, 3, 5, RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS);
		fdata.addFilter(filter);
		fdata.addFilter(new SortFilter(2, SortFilter.ASCENDING));
		fdata.removeFilter(filter);

		
		assertEquals(5, fdata.getRowCount());
		Object[] row = fdata.getRow(0);
		assertEquals(5, ((Integer)row[0]).intValue());
		assertEquals(2, ((Integer)row[2]).intValue());

		row = fdata.getRow(1);
		assertEquals(4, ((Integer)row[0]).intValue());
		assertEquals(3, ((Integer)row[2]).intValue());
	}
	
	@Test
	public void testClearFilters() {
		data.remove(0);
		data.remove(0);
		
		RowEntry entry;
		
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {3, 2, 5});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {4, 2, 3});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {7, 2, 9});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {2, 2, 6});
		data.append(entry);
		entry = new RowEntry();
		entry.putRow(0, new Integer[] {5, 2, 2});
		data.append(entry);
		
		RangeFilter filter = new RangeFilter(0, 3, 5, RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS);
		fdata.addFilter(filter);
		fdata.addFilter(new SortFilter(2, SortFilter.ASCENDING));

		fdata.clearFilters();
		
		assertEquals(5, fdata.getRowCount());
		Object[] row = fdata.getRow(0);
		assertEquals(3, ((Integer)row[0]).intValue());
		assertEquals(5, ((Integer)row[2]).intValue());

		row = fdata.getRow(1);
		assertEquals(4, ((Integer)row[0]).intValue());
		assertEquals(3, ((Integer)row[2]).intValue());
	}
	@Test
	public void testGetFilters() {
		assertEquals(0, fdata.getFilters().length);

		RangeFilter filter1 = new RangeFilter(0, 3, 5, RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS);
		SortFilter filter2 = new SortFilter(2, SortFilter.ASCENDING);
		
		fdata.addFilter(filter1);
		fdata.addFilter(filter2);
		
		IDataSetFilter[] filters = fdata.getFilters();
		assertEquals(2, filters.length);
	}
	
	private RowDataSet data;
	private FilteredRowDataSet fdata;
	private RowEntry entry0;
}
