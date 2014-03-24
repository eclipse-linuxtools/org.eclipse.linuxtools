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

package org.eclipse.linuxtools.systemtap.graphing.core.datasets.row;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.linuxtools.internal.systemtap.graphing.core.Localization;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IHistoricalDataSet;



public class RowDataSet implements IHistoricalDataSet {
	//IDataSet Methods
	public RowDataSet(String[] titles) {
		if (titles != null){
			this.titles = Arrays.copyOf(titles, titles.length);
		}
		data = new ArrayList<>();
	}

	@Override
	public void setData(IDataEntry entry) {
		append(entry);
	}

	@Override
	public void append(IDataEntry entry) {
		RowEntry dataBlock = (RowEntry)entry;
		if(dataBlock.getRow(null).length != this.getColCount())
			throw new ArrayIndexOutOfBoundsException(Localization.getString("RowDataSet.ArraySizeMismatch") + dataBlock.getRow(null).length + " != " + this.getColCount()); //$NON-NLS-1$ //$NON-NLS-2$

		data.add(entry);
	}

	@Override
	public String[] getTitles() {
		return titles;
	}

	@Override
	public Object[] getColumn(int col) {
		return getColumn(col, 0, getRowCount());
	}

	@Override
	public Object[] getColumn(int col, int start, int end) {
		return getHistoricalData(null, col, start, end);
	}

	@Override
	public Object[] getRow(int row) {
		IDataEntry entry = getEntry(row);
		if(null != entry)
			return entry.getRow(null);
		return null;
	}

	@Override
	public int getRowCount() {
		return getEntryCount();
	}

	@Override
	public int getColCount() {
		if(null == titles) {
			return -1;
		}
		return titles.length;
	}

	@Override
	public boolean readFromFile(File file) {
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr)){
			br.readLine();	//Skip the ID
			br.readLine();	//Skip the Titles
			String line;
			RowEntry entry;
			while(null != (line = br.readLine())) {
				entry = new RowEntry();
				entry.putRow(0, line.split(", ")); //$NON-NLS-1$
				append(entry);
			}
			return true;
		} catch(IOException|ArrayIndexOutOfBoundsException e) {
		}
		return false;
	}

	@Override
	public boolean writeToFile(File file) {
		try {
			file.createNewFile();
			try (FileOutputStream fos = new FileOutputStream(file);
					PrintStream ps = new PrintStream(fos)) {

				String line = ""; //$NON-NLS-1$
				Object[] dataRow;

				// ID
				ps.print(ID + "\n"); //$NON-NLS-1$

				// Labels
				int i, j;
				for (i = 0; i < titles.length; i++)
					line += titles[i] + ", "; //$NON-NLS-1$
				ps.print(line + "\n"); //$NON-NLS-1$

				// Data
				for (i = 0; i < getRowCount(); i++) {
					dataRow = getRow(i);
					line = ""; //$NON-NLS-1$
					for (j = 0; j < dataRow.length; j++)
						line += dataRow[j].toString() + ", "; //$NON-NLS-1$
					ps.print(line + "\n"); //$NON-NLS-1$
				}
			}
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	@Override
	public String getID() {
		return ID;
	}
	//End IDataSet Methods

	//IHistoricalDataSet Methods
	@Override
	public Object[] getHistoricalData(String key, int col) {
		return getHistoricalData(key, col, 0, getRowCount());
	}

	@Override
	public Object[] getHistoricalData(String key, int col, int start, int end) {
		if(start > end || start < 0 || end > getRowCount() || col < COL_ROW_NUM || col >= this.getColCount()) {
			return null;
		}

		if(COL_ROW_NUM == col) {
			Integer[] rows = new Integer[Math.min(end-start, data.size())];
			for(int i=0;i<rows.length; i++) {
				rows[i] = Integer.valueOf(start+i+1);
			}
			return rows;
		}

		Object[] d = new Object[Math.min(end-start, data.size())];

		for(int i=0; i<d.length; i++) {
			d[i] = data.get(start+i).getColumn(col)[0];
		}
		return d;
	}

	@Override
	public int getEntryCount() {
		return data.size();
	}

	@Override
	public boolean remove(IDataEntry entry) {
		return data.remove(entry);
	}

	@Override
	public boolean remove(int entry) {
		if(entry < 0 || entry >= data.size()) {
			return false;
		}
		return (null != data.remove(entry));
	}

	@Override
	public IDataEntry getEntry(int entry) {
		if(entry < 0 || entry >= getEntryCount()) {
			return null;
		} else {
			return data.get(entry);
		}
	}
	//End IHistoricalDataSet Methods

	protected ArrayList<IDataEntry> data;
	private String[] titles;
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphing.core.datasets.rowdataset"; //$NON-NLS-1$
}
