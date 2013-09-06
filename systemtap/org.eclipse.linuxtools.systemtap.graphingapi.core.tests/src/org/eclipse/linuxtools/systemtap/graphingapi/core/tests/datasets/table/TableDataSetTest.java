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

package org.eclipse.linuxtools.systemtap.graphingapi.core.tests.datasets.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableEntry;
import org.junit.Before;
import org.junit.Test;

public class TableDataSetTest {

	@Before
	public void setUp() {
		data = new TableDataSet(new String[] {"a", "b", "c"});
		entry0 = new TableEntry();
		entry0.putRow(0, new String[] {"1", "2", "3"});
		data.setData(entry0);
		TableEntry entry = new TableEntry();
		entry.putRow(0, new String[] {"4", "5", "6"});
		data.setData(entry);
	}

	@Test
	public void testSetData() {
		assertEquals(2, data.getEntryCount());
		TableEntry entry = new TableEntry();
		entry.putRow(0, new String[] {"1", "2", "3"});
		data.setData(entry);
		assertEquals(3, data.getEntryCount());
	}

	@Test
	public void testAppend() {
		assertEquals(2, data.getEntryCount());
		TableEntry entry = new TableEntry();
		entry.putRow(0, new String[] {"1", "2", "3"});
		data.append(entry);
		assertEquals(3, data.getEntryCount());
	}
	@Test
	public void testGetTitles() {
		String[] titles = data.getTitles();
		assertEquals(3, titles.length);
		assertSame("a", titles[0]);
	}
	@Test
	public void testGetColumn() {
		assertNull(data.getColumn(-3));
		assertNull(data.getColumn(10));

		assertNull(data.getColumn(-3, 0, 1));
		assertNull(data.getColumn(10, 0, 1));
		assertNull(data.getColumn(1, 3, 1));
		assertNull(data.getColumn(1, -2, 1));
		assertNull(data.getColumn(1, 0, 20));

		Object[] col = data.getColumn(0);
		assertEquals(1, col.length);
		assertSame("4", col[0]);

		col = data.getColumn(IDataSet.COL_ROW_NUM);
		assertEquals(1, col.length);
		assertEquals("1", col[0].toString());

		col = data.getColumn(1, 0, 1);
		assertEquals(1, col.length);
		assertSame("5", col[0]);
	}
	@Test
	public void testGetRow() {
		assertNull(data.getRow(-3));
		assertNull(data.getRow(10));

		assertNull(data.getRow(1));

		Object[] row = data.getRow(0);
		assertEquals(3, row.length);
		assertSame("5", row[1]);
	}
	@Test
	public void testGetRowCount() {
		assertEquals(1, data.getRowCount());
	}
	@Test
	public void testGetColCount() {
		assertEquals(3, data.getColCount());

		assertEquals(0, new TableDataSet(null).getColCount());
	}
	@Test
	public void testReadFromFile() {}
	@Test
	public void testWriteToFile() {}
	@Test
	public void testGetID() {
		assertEquals(TableDataSet.ID, data.getID());
	}
	//End IDataSet Methods

	//IHistoricalDataSet Methods
	@Test
	public void testGetHistoricalData() {
		assertNull(data.getHistoricalData(null, -3));
		assertNull(data.getHistoricalData(null, 10));

		assertNull(data.getHistoricalData(null, -3, 0, 1));
		assertNull(data.getHistoricalData(null, 10, 0, 1));
		assertNull(data.getHistoricalData(null, 1, 3, 1));
		assertNull(data.getHistoricalData(null, 1, -2, 1));
		assertNull(data.getHistoricalData(null, 1, 0, 20));

		Object[] col = data.getHistoricalData(null, 0);
		assertEquals(2, col.length);
		assertEquals("0", col[0].toString());
		assertEquals("0", col[1].toString());

		col = data.getHistoricalData(null, IDataSet.COL_ROW_NUM);
		assertEquals(2, col.length);
		assertEquals("1", col[0].toString());
		assertEquals("2", col[1].toString());

		col = data.getHistoricalData("4", 1, 1, 2);
		assertEquals(1, col.length);
		assertSame("5", col[0]);
	}
	@Test
	public void testGetEntryCount() {
		assertEquals(2, data.getEntryCount());
	}
	@Test
	public void testRemove() {
		assertFalse(data.remove(null));
		assertFalse(data.remove(new TableEntry()));
		assertFalse(data.remove(-1));
		assertFalse(data.remove(10));
		assertEquals(2, data.getEntryCount());

		IDataEntry entry = data.getEntry(0);
		assertTrue(data.remove(entry));
		assertEquals(1, data.getEntryCount());
		assertFalse(data.remove(entry));
		assertTrue(data.remove(0));
	}
	@Test
	public void testGetEntry() {
		assertNull(data.getEntry(-1));
		assertNull(data.getEntry(20));
		assertEquals(entry0, data.getEntry(0));
	}
	@Test
	public void testGetData() {
		Object[][] d = data.getData();
		assertNotNull(d);
		assertEquals(1, d.length);
		assertEquals("6", d[0][2]);
	}

	private TableDataSet data;
	private TableEntry entry0;
}
