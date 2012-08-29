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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.nonui.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IHistoricalDataSet;



public class RowDataSet implements IHistoricalDataSet {
	//IDataSet Methods
	public RowDataSet(String[] titles) {
		this.titles = titles;
		data = new ArrayList<IDataEntry>();
	}

	public void setData(IDataEntry entry) {
		append(entry);
	}
	
	public void append(IDataEntry entry) throws ArrayIndexOutOfBoundsException {
		RowEntry dataBlock = (RowEntry)entry;
		if(dataBlock.getRow(null).length != this.getColCount())
			throw new ArrayIndexOutOfBoundsException(Localization.getString("RowDataSet.ArraySizeMismatch") + dataBlock.getRow(null).length + " != " + this.getColCount());

		data.add(entry);
	}
	
	public String[] getTitles() {
		return titles;
	}
	
	public Object[] getColumn(int col) {
		return getColumn(col, 0, getRowCount());
	}
	
	public Object[] getColumn(int col, int start, int end) {
		return getHistoricalData(null, col, start, end);
	}
	
	public Object[] getRow(int row) {
		IDataEntry entry = getEntry(row);
		if(null != entry)
			return entry.getRow(null);
		return null;
	}
	
	public int getRowCount() {
		return getEntryCount();
	}
	
	public int getColCount() {
		if(null == titles)
			return -1;
		return titles.length;
	}
	
	public boolean readFromFile(File file) {
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			br.readLine();	//Skip the ID
			br.readLine();	//Skip the Titles
			String line;
			RowEntry entry;
			while(null != (line = br.readLine())) {
				entry = new RowEntry();
				entry.putRow(0, line.split(", "));
				append(entry);
			}
			br.close();
			return true;
		} catch(FileNotFoundException fnfe) {
		} catch(IOException ioe) {
		} catch(ArrayIndexOutOfBoundsException aioobe) {}
		return false;
	}
	
	public boolean writeToFile(File file) {
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);

			String line = "";
			Object[] dataRow;

			//ID
			ps.print(ID + "\n");
			
			//Labels
			int i, j;
			for(i=0; i<titles.length; i++) 
				line += titles[i] + ", ";
			ps.print(line + "\n");
			
			//Data
			for(i=0; i<getRowCount(); i++) {
				dataRow = getRow(i);
				line = "";
				for(j=0; j<dataRow.length; j++) 
					line += dataRow[j].toString() + ", ";
				ps.print(line + "\n");
			}
			ps.close();
			return true;
		} catch(FileNotFoundException e) {
		} catch(IOException e) {}
		return false;
	}
	
	public String getID() {
		return ID;
	}
	//End IDataSet Methods
	
	//IHistoricalDataSet Methods
	public Object[] getHistoricalData(String key, int col) {
		return getHistoricalData(key, col, 0, getRowCount());
	}
	
	public Object[] getHistoricalData(String key, int col, int start, int end) {
		if(start > end || start < 0 || end > getRowCount() || col < COL_ROW_NUM || col >= this.getColCount())
			return null;

		if(COL_ROW_NUM == col) {
			Integer[] rows = new Integer[Math.min(end-start, data.size())];
			for(int i=0;i<rows.length; i++)
				rows[i] = new Integer(start+i+1);
			return rows;
		}

		Object[] d = new Object[Math.min(end-start, data.size())];
		
		for(int i=0; i<d.length; i++)
			d[i] = data.get(start+i).getColumn(col)[0];
		return d;
	}
	
	public int getEntryCount() {
		return data.size();
	}
	
	public boolean remove(IDataEntry entry) {
		return data.remove(entry);
	}
	
	public boolean remove(int entry) {
		if(entry < 0 || entry >= data.size())
			return false;
		return (null != data.remove(entry));
	}
	
	public IDataEntry getEntry(int entry) {
		if(entry < 0 || entry >= getEntryCount())
			return null;
		else
			return data.get(entry);
	}
	//End IHistoricalDataSet Methods
	
	protected ArrayList<IDataEntry> data;
	private String[] titles;
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.rowdataset";
}
