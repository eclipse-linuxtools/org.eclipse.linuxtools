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

package org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.table;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.core.GraphingAPINonUIPlugin;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;



public class FilteredTableDataSet extends TableDataSet implements IFilteredDataSet {
	public FilteredTableDataSet(TableDataSet data) {
		super(data.getTitles());
		original = data;
		this.data = data.data;
		filters = new ArrayList<>();

		filtersChanged = false;
		historical = false;
		topData = null;
		dataCount = data.getRowCount();
	}

	public FilteredTableDataSet(String[] titles) {
		this(new TableDataSet(titles));
	}

	//Overwrite methods to insure data is removed from the original DataSet
	@Override
	public void append(IDataEntry entry) {
		original.append(entry);
	}

	@Override
	public boolean remove(IDataEntry entry) {
		return original.remove(entry);
	}

	@Override
	public boolean remove(int entry) {
		return original.remove(entry);
	}
	//End overwrite methods to insure data is removed from the original DataSet

 	//Overwrite to ensure the data returned has all the filters applied
	@Override
	public Object[] getColumn(int col, int start, int end) {
		rebuildDataSet();
		return super.getColumn(col, start, end);
	}

	@Override
	public Object[] getRow(int row) {
		rebuildDataSet();
		return super.getRow(row);
	}

	@Override
	public int getRowCount() {
		rebuildDataSet();
		return super.getRowCount();
	}

	@Override
	public Object[] getHistoricalData(String key, int col, int start, int end) {
		return original.getHistoricalData(key, col, start, end);
	}

	@Override
	public int getEntryCount() {
		return original.getEntryCount();
	}

	@Override
	public IDataEntry getEntry(int entry) {
		return original.getEntry(entry);
	}

	@Override
	public Object[][] getData() {
		rebuildDataSet();
		return super.getData();
	}
 	//End overwrite to ensure the data returned has all the filters applied

	//IFilteredDataSet Methods
	@Override
	public void addFilter(IDataSetFilter filter) {
		filters.add(filter);
		filtersChanged = true;
	}

	@Override
	public boolean removeFilter(IDataSetFilter filter) {
		filtersChanged = filters.remove(filter);
		return filtersChanged;
	}

	@Override
	public void clearFilters() {
		filters.clear();
		filtersChanged = true;
	}

	@Override
	public IDataSetFilter[] getFilters() {
		IDataSetFilter[] f = new IDataSetFilter[filters.size()];
		filters.toArray(f);
		return f;
	}
	//End IFilteredDataSet Methods

	private void rebuildDataSet() {
		IDataEntry top = original.getEntry(original.getEntryCount()-1);

		if(filtersChanged || dataCount != original.getRowCount() || topData != top || historical) {
			dataCount = original.getRowCount();
			topData = top;
			historical = false;

 			ArrayList<Object>[] filterData = getFilterData();
			for(int i=0; i<filters.size(); i++)
				filterData = filters.get(i).filter(filterData);
			setFilteredData(filterData);
		}
	}

	private ArrayList<Object>[] getFilterData() {
		ArrayList<Object>[] data = GraphingAPINonUIPlugin.createArrayList(original.getColCount(), new Object());
		for(int i=0; i<data.length; i++) {
			data[i] = new ArrayList<>();
		}

		Object[][] table = original.getData();
		for(int j,i=0; i<original.getRowCount(); i++) {
			for(j=0; j<data.length; j++) {
				data[j].add(table[i][j]);
			}
		}

		return data;
	}

	private void setFilteredData(ArrayList<Object>[] data) {
		this.data = new ArrayList<>();

		TableEntry entry = new TableEntry();
		Object[] row;
		for(int j,i=0; i<data[0].size(); i++) {
			row = new Object[data.length];
			for(j=0; j<data.length; j++) {
				row[j] = data[j].get(i);
			}
			entry.add(row);
		}
		this.data.add(entry);
	}

	private TableDataSet original;
	private ArrayList<IDataSetFilter> filters;

	private boolean filtersChanged;
	private int dataCount;
	private Object topData;
	private boolean historical;
}
