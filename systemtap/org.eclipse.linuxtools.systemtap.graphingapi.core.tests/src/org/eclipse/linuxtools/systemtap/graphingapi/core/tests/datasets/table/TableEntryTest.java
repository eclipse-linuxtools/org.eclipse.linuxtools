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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table.TableEntry;
import org.junit.Before;
import org.junit.Test;

public class TableEntryTest {

	@Before
	public void setUp() {
		entry = new TableEntry();
		data = new Integer[] {2, 5, 4};
		entry.add(data);
		data = new Integer[] {5, 1, 3};
		entry.add(data);
	}

	@Test
	public void testGetRowCount() {
		TableEntry entry2 = new TableEntry();
		assertEquals(0, entry2.getRowCount());

		entry2 = new TableEntry();
		entry2.putRow(0, null);
		assertEquals(0, entry2.getRowCount());

		assertEquals(2, entry.getRowCount());
	}
	@Test
	public void testGetColCount() {
		TableEntry entry2 = new TableEntry();
		assertEquals(0, entry2.getColCount());

		entry2 = new TableEntry();
		entry2.putRow(0, null);
		assertEquals(0, entry2.getColCount());

		assertEquals(3, entry.getColCount());
	}
	@Test
	public void testGet() {
		assertNull(entry.get(null, 1));
		assertNull(entry.get("asdf", 1));
		assertNull(entry.get(null, 10));
		assertNull(entry.get(null, -1));
		assertEquals("5", entry.get("2", 1).toString());
	}
	@Test
	public void testGetData() {
		assertEquals(data[0], entry.getData()[1][0]);
		assertEquals(data[1], entry.getData()[1][1]);
	}
	@Test
	public void testGetRow() {
		assertNull(entry.getRow(10));
		assertNull(entry.getRow(-1));
		assertNotNull(entry.getRow(0));
		assertArrayEquals(data, entry.getRow(1));

		assertNull(entry.getRow(null));
		assertNull(entry.getRow("asdf"));
		assertNotNull(entry.getRow("2"));
		assertArrayEquals(data, entry.getRow("5"));
	}
	@Test
	public void testGetColumn() {
		assertEquals(data[1], entry.getColumn(1)[1]);
		assertNull(entry.getColumn(10));
		assertNull(entry.getColumn(-1));
	}
	@Test
	public void testPutRow() {
		Integer[] data2 = new Integer[] {2, 5};

		//Can't add to -1 position
		entry.putRow(-1, data2);
		assertEquals(2, entry.getRowCount());

		//Cant add wrong sized array
		entry.putRow(10, data2);
		assertEquals(2, entry.getRowCount());

		entry.putRow(0, data2);
		assertEquals(2, entry.getRowCount());

		//Add successful
		data2 = new Integer[] {2, 5, 6};
		entry.putRow(0, data2);
		assertEquals(2, entry.getRowCount());
	}
	@Test
	public void testAdd() {
		Integer[] data2 = new Integer[] {2, 5};
		entry.add(data2);
		assertEquals(2, entry.getRowCount());

		//Add successful
		data2 = new Integer[] {2, 5, 6};
		entry.add(data2);
		assertEquals(3, entry.getRowCount());
	}
	@Test
	public void testCopy() {
		IDataEntry entry2 = entry.copy();
		assertEquals(entry2.getRowCount(), entry.getRowCount());
		assertEquals(entry2.getColCount(), entry.getColCount());
		assertSame(entry2.getRow(0)[1], entry.getRow(0)[1]);
	}
	@Test
	public void testRemove() {
		assertTrue(entry.remove(0));
		assertEquals(1, entry.getRowCount());
	}

	private TableEntry entry;
	private Integer[] data;
}
