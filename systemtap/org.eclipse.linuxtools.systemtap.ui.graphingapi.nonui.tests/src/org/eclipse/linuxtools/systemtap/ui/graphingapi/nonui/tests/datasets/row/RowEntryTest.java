package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.RowEntry;

import junit.framework.TestCase;

public class RowEntryTest extends TestCase {
	public RowEntryTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		entry = new RowEntry();
		data = new Integer[] {new Integer(2), new Integer(5), new Integer(4)};
		entry.putRow(0, data);
	}

	public void testGetRowCount() {
		RowEntry entry2 = new RowEntry();
		assertEquals(0, entry2.getRowCount());
		
		entry2 = new RowEntry();
		entry2.putRow(0, null);
		assertEquals(0, entry2.getRowCount());
		
		assertEquals(1, entry.getRowCount());
	}
	
	public void testGetColCount() {
		RowEntry entry2 = new RowEntry();
		assertEquals(0, entry2.getColCount());
		
		entry2 = new RowEntry();
		entry2.putRow(0, null);
		assertEquals(0, entry2.getColCount());
		
		assertEquals(3, entry.getColCount());
	}
	
	public void testGet() {
		assertEquals(data[1], entry.get(null, 1));
		assertEquals(data[1], entry.get("asdf", 1));
		assertNull(entry.get(null, 10));
		assertNull(entry.get(null, -1));
	}
	
	public void testGetRow() {
		assertEquals(data, entry.getRow(0));
		assertNull(entry.getRow(10));
		assertNull(entry.getRow(-1));

		assertEquals(data, entry.getRow(null));
		assertEquals(data, entry.getRow("asdf"));
	}
	
	public void testGetColumn() {
		assertEquals(data[1], entry.getColumn(1)[0]);
		assertNull(entry.getColumn(10));
		assertNull(entry.getColumn(-1));
	}
	
	public void testGetData() {
		assertEquals(data[0], entry.getData()[0][0]);
		assertEquals(data[1], entry.getData()[0][1]);
	}
	
	public void testCopy() {
		IDataEntry entry2 = entry.copy();
		assertEquals(entry2.getRowCount(), entry.getRowCount());
		assertEquals(entry2.getColCount(), entry.getColCount());
		assertSame(entry2.getRow(0)[1], entry.getRow(0)[1]);
	}
	
	public void testPutRow() {
		Integer[] data2 = new Integer[] {new Integer(2), new Integer(5)};
		
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
	
	public void testRemove() {
		assertFalse(entry.remove(-1));
		assertEquals(3, entry.getColCount());
		assertFalse(entry.remove(10));
		assertEquals(3, entry.getColCount());

		assertTrue(entry.remove(0));
		assertEquals(0, entry.getColCount());
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	RowEntry entry;
	Integer[] data;
}