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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IBlockDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IHistoricalDataSet;



public class TableDataSet implements IHistoricalDataSet, IBlockDataSet {
	public TableDataSet(String[] labels) {
		if(null == labels){
			this.titles = new String[0];
		} else {
			this.titles = Arrays.copyOf(labels, labels.length);
		}
		data = new ArrayList<>();
	}

	//IDataSet Methods
	@Override
	public String[] getTitles() {
		return titles;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public boolean readFromFile(File file) {
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr)){

			br.readLine();	//Skip the ID
			br.readLine();	//Skip the Titles
			String line;
			TableEntry entry = new TableEntry();
			while(null != (line = br.readLine())) {
				if(line.isEmpty()) {
					append(entry);
					entry = new TableEntry();
				} else {
					entry.add(line.split(", ")); //$NON-NLS-1$
				}
			}
			br.close();
			return true;
		} catch(IOException|ArrayIndexOutOfBoundsException e) {}
		return false;
	}

	@Override
	public boolean writeToFile(File file) {
		try {
			file.createNewFile();
			try (FileOutputStream fos = new FileOutputStream(file);
					PrintStream ps = new PrintStream(fos)) {
				StringBuilder b = new StringBuilder();

				// ID
				b.append(ID + "\n"); //$NON-NLS-1$

				// Labels
				int i, j, k;
				for (i = 0; i < titles.length; i++) {
					b.append(titles[i] + ", "); //$NON-NLS-1$
				}
				b.append("\n"); //$NON-NLS-1$

				// Data
				TableEntry e;
				Object[] o;
				for (i = 0; i < data.size(); i++) {
					e = data.get(i);
					for (j = 0; j < e.getRowCount(); j++) {
						o = e.getRow(j);
						for (k = 0; k < o.length; k++) {
							b.append(o[k].toString() + ", "); //$NON-NLS-1$
						}
						b.append("\n"); //$NON-NLS-1$
					}
					b.append("\n"); //$NON-NLS-1$
				}
				ps.print(b.toString());
			}
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	@Override
	public int getRowCount() {
		if(data.size() > 0) {
			return data.get(data.size()-1).getRowCount();
		} else {
			return 0;
		}
	}

	@Override
	public int getColCount() {
		return titles.length;
	}

	@Override
	public Object[] getRow(int row) {
		return data.get(data.size()-1).getRow(row);
	}

	@Override
	public Object[] getColumn(int col) {
		return getColumn(col, 0, getRowCount());
	}

	@Override
	public Object[] getColumn(int col, int start, int end) {
		if(start > end || start < 0 || end > getEntryCount() || col < COL_ROW_NUM || col >= this.getColCount()) {
			return null;
		}
		if(COL_ROW_NUM == col) {
			Integer[] rows = new Integer[Math.min(end-start, getRowCount())];
			for(int i=0;i<rows.length; i++) {
				rows[i] = Integer.valueOf(start+i+1);
			}
			return rows;
		}
		return data.get(data.size()-1).getColumn(col, start, end);
	}

	@Override
	public void setData(IDataEntry data) {
		append(data);
	}

	@Override
	public boolean remove(IDataEntry entry) {
		return data.remove(entry);
	}
	//End IDataSet Methods

	//IHistoricalDataSet Methods
	@Override
	public void append(IDataEntry data) {
		if(data instanceof TableEntry) {
			this.data.add((TableEntry)data);
		}
	}

	@Override
	public Object[] getHistoricalData(String key, int col) {
		return getHistoricalData(key, col, 0, getEntryCount());
	}

	@Override
	public Object[] getHistoricalData(String key, int col, int start, int end) {
		if(start > end || start < 0 || end > getEntryCount() || col < COL_ROW_NUM || col >= this.getColCount()) {
			return null;
		}

		if(COL_ROW_NUM == col) {
			Integer[] rows = new Integer[Math.min(end-start, data.size())];
			for(int i=0;i<rows.length; i++) {
				rows[i] = Integer.valueOf(start+i+1);
			}
			return rows;
		}

		Object[] d = new Object[Math.min(end-start, getEntryCount())];

		for(int i=0; i<d.length; i++) {
			d[i] = getEntry(i+start).get(key, col);
			if(null == d[i]) {
				d[i] = Integer.valueOf(0);
			}
		}
		return d;
	}

	@Override
	public int getEntryCount() {
		return data.size();
	}

	@Override
	public IDataEntry getEntry(int entry) {
		if(entry >=0 && entry < getEntryCount()) {
			return data.get(entry);
		}
		return null;
	}

	@Override
	public boolean remove(int entry) {
		if(entry < 0 || entry >= data.size()) {
			return false;
		}
		return (null != data.remove(entry));
	}
	//End IHistoricalDataSet Methods

	//IBlockDataSet Methods
	@Override
	public Object[][] getData() {
		return data.get(getEntryCount()-1).getData();
	}
	//End IBlockDataSet Methods

	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.tabledataset"; //$NON-NLS-1$
	protected ArrayList<TableEntry> data;
	private String[] titles;
}
