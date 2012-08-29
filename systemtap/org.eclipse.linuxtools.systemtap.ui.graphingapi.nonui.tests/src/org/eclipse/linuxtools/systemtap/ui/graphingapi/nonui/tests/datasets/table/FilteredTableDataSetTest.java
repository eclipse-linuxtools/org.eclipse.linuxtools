package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table;

import junit.framework.TestCase;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row.RowEntry;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.table.FilteredTableDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.table.TableEntry;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.RangeFilter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters.SortFilter;

public class FilteredTableDataSetTest extends TestCase {
	public FilteredTableDataSetTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		data = new TableDataSet(new String[] {"a", "b", "c"});
		dataSet = new FilteredTableDataSet(data);
	}

	public void testFilteredDataSet() {
		FilteredTableDataSet fds = new FilteredTableDataSet(new String[] {"a", "b", "c"});
		assertNotNull(fds);
		assertNotNull(fds.getTitles());
	}
	
	public void testAppend() {
		TableEntry entry;
		
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(3), new Integer(2), new Integer(5)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(4), new Integer(2), new Integer(3)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(7), new Integer(2), new Integer(9)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(2), new Integer(2), new Integer(6)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(5), new Integer(2), new Integer(2)});
		dataSet.append(entry);

		Object[] row = dataSet.getRow(0);
		assertEquals(5, ((Integer)row[0]).intValue());
		assertEquals(2, ((Integer)row[2]).intValue());

		assertNull(dataSet.getRow(2));
	}

	public void testRemove() {
		assertFalse(dataSet.remove(null));
		assertFalse(dataSet.remove(new RowEntry()));
		assertFalse(dataSet.remove(-1));
		assertFalse(dataSet.remove(10));
		assertEquals(0, dataSet.getEntryCount());

		TableEntry entry= new TableEntry();
		entry.add(new String[] {"1", "2", "3"});
		data.append(entry);
		
		entry = (TableEntry)data.getEntry(0);
		assertEquals(1, dataSet.getEntryCount());
		assertTrue(dataSet.remove(entry));
		assertEquals(0, dataSet.getEntryCount());
		assertFalse(dataSet.remove(entry));
		assertFalse(dataSet.remove(0));
	}
	//End overwrite methods to insure data is removed from the original DataSet
	public void testGetHistoricalData() {
		TableEntry entry = new TableEntry();
		entry.putRow(0, new String[] {"1", "2", "3"});
		dataSet.setData(entry);
		entry = new TableEntry();
		entry.putRow(0, new String[] {"4", "5", "6"});
		dataSet.setData(entry);
		
		assertNull(dataSet.getHistoricalData(null, -3));
		assertNull(dataSet.getHistoricalData(null, 10));

		assertNull(dataSet.getHistoricalData(null, -3, 0, 1));
		assertNull(dataSet.getHistoricalData(null, 10, 0, 1));
		assertNull(dataSet.getHistoricalData(null, 1, 3, 1));
		assertNull(dataSet.getHistoricalData(null, 1, -2, 1));
		assertNull(dataSet.getHistoricalData(null, 1, 0, 20));
		
		Object[] col = dataSet.getHistoricalData(null, 0);
		assertEquals(2, col.length);
		assertEquals("0", col[0].toString());
		assertEquals("0", col[1].toString());
		
		col = dataSet.getHistoricalData(null, IDataSet.COL_ROW_NUM);
		assertEquals(2, col.length);
		assertEquals("1", col[0].toString());
		assertEquals("2", col[1].toString());
		
		col = dataSet.getHistoricalData("4", 1, 1, 2);
		assertEquals(1, col.length);
		assertSame("5", col[0]);
	}
	
	public void testGetEntryCount() {
		assertEquals(0, dataSet.getEntryCount());
	}
	
	public void testGetEntry() {
		assertNull(dataSet.getEntry(-1));
		assertNull(dataSet.getEntry(20));
	}
	
	public void testGetData() {
		TableEntry entry= new TableEntry();
		entry.add(new String[] {"1", "2", "3"});
		data.append(entry);
		
		Object[][] d = dataSet.getData();
		assertNotNull(d);
		assertEquals(1, d.length);
		assertEquals("3", d[0][2]);
	}
	
 	//Overwrite to ensure the data returned has all the filters applied
	public void testGetColumn() {
		TableEntry entry= new TableEntry();
		entry.add(new String[] {"1", "2", "3"});
		data.append(entry);

		assertNull(dataSet.getColumn(-3));
		assertNull(dataSet.getColumn(10));

		assertNull(dataSet.getColumn(-3, 0, 1));
		assertNull(dataSet.getColumn(10, 0, 1));
		assertNull(dataSet.getColumn(1, 3, 1));
		assertNull(dataSet.getColumn(1, -2, 1));
		assertNull(dataSet.getColumn(1, 0, 20));
		
		Object[] col = dataSet.getColumn(0);
		assertEquals(1, col.length);
		assertSame("1", col[0]);
		
		col = dataSet.getColumn(IDataSet.COL_ROW_NUM);
		assertEquals(1, col.length);
		assertEquals("1", col[0].toString());
		
		col = dataSet.getColumn(1, 0, 1);
		assertEquals(1, col.length);
		assertSame("2", col[0]);
	}
	
	public void testAddFilter() {
		TableEntry entry;
		
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(3), new Integer(2), new Integer(5)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(4), new Integer(2), new Integer(3)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(7), new Integer(2), new Integer(9)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(2), new Integer(2), new Integer(6)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(5), new Integer(2), new Integer(2)});
		dataSet.append(entry);
		
		dataSet.addFilter(new RangeFilter(0, new Integer(3), new Integer(4), RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS));
		assertEquals(0, dataSet.getRowCount());
		dataSet.clearFilters();
		dataSet.addFilter(new RangeFilter(0, new Integer(3), new Integer(5), RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS));
		
		assertEquals(1, dataSet.getRowCount());
		Object[] row = dataSet.getRow(0);
		assertEquals(5, ((Integer)row[0]).intValue());
		assertEquals(2, ((Integer)row[2]).intValue());
	}
	
	public void testRemoveFilter() {
		TableEntry entry;
		
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(3), new Integer(2), new Integer(5)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(4), new Integer(2), new Integer(3)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(7), new Integer(2), new Integer(9)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(2), new Integer(2), new Integer(6)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(5), new Integer(2), new Integer(2)});
		dataSet.append(entry);
		
		RangeFilter filter = new RangeFilter(0, new Integer(3), new Integer(5), RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS);
		dataSet.addFilter(filter);
		dataSet.addFilter(new SortFilter(2, SortFilter.ASCENDING));
		dataSet.removeFilter(filter);

		assertEquals(1, dataSet.getRowCount());
		Object[] row = dataSet.getRow(0);
		assertEquals(5, ((Integer)row[0]).intValue());
		assertEquals(2, ((Integer)row[2]).intValue());
	}
	
	public void testClearFilters() {
		TableEntry entry;
		
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(3), new Integer(2), new Integer(5)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(4), new Integer(2), new Integer(3)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(7), new Integer(2), new Integer(9)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(2), new Integer(2), new Integer(6)});
		dataSet.append(entry);
		entry = new TableEntry();
		entry.add(new Integer[] {new Integer(5), new Integer(2), new Integer(2)});
		dataSet.append(entry);
		
		dataSet.addFilter(new RangeFilter(0, new Integer(3), new Integer(5), RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS));
		dataSet.addFilter(new SortFilter(2, SortFilter.ASCENDING));

		assertEquals(2, dataSet.getFilters().length);
		dataSet.clearFilters();
		
		assertEquals(1, dataSet.getRowCount());
		Object[] row = dataSet.getRow(0);
		assertEquals(5, ((Integer)row[0]).intValue());
		assertEquals(2, ((Integer)row[2]).intValue());
		assertEquals(0, dataSet.getFilters().length);
	}
	
	public void testGetFilters() {
		assertEquals(0, dataSet.getFilters().length);

		RangeFilter filter1 = new RangeFilter(0, new Integer(3), new Integer(5), RangeFilter.INCLUSIVE | RangeFilter.INSIDE_BOUNDS);
		SortFilter filter2 = new SortFilter(2, SortFilter.ASCENDING);
		
		dataSet.addFilter(filter1);
		dataSet.addFilter(filter2);
		
		IDataSetFilter[] filters = dataSet.getFilters();
		assertEquals(2, filters.length);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	FilteredTableDataSet dataSet;
	TableDataSet data;
}
