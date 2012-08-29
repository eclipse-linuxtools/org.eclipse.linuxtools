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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.table;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataEntry;



public class TableEntry implements IDataEntry {
	public TableEntry() {
		bodyContent = new ArrayList<Object[]>();
	}
	
	public int getRowCount() {
		return bodyContent.size();
	}
	
	public int getColCount() {
		if(getRowCount() > 0) {
			try {
				return bodyContent.get(0).length;
			} catch(Exception cce) {}
		}
		return 0;
	}
	
	public Object get(String key, int col) {
		if(col >= 0 && col < getColCount()) {
			Object[] row = getRow(key);
			if(null != row)
				return row[col];
		}
		return null;
	}
	
	public Object[][] getData() {
		Object[][] d = new Object[getRowCount()][getColCount()];
		for(int i=0; i<getRowCount(); i++) {
			d[i] = getRow(i);
		}
		return d;
	}

	public Object[] getRow(int row) {
		if(row < 0 || row >= getRowCount())
			return null;
		return bodyContent.get(row);
	}
	
	public Object[] getRow(String key) {
		Object[] row;
		for(int i=0; i<bodyContent.size(); i++) {
			row = bodyContent.get(i);
			if(row[0].toString().equals(key))
				return row;
		}
		return null;
	}
	
	public Object[] getColumn(int col) {
		return getColumn(col, 0, getRowCount());
	}
	
	public Object[] getColumn(int col, int start, int end) {
		if(0 <= col && getColCount() > col && start >=0 && end > start && end <= getRowCount()) {
			Object[] res = new Object[Math.min(end-start, getRowCount())];
			for(int i=0; i<res.length; i++)
				res[i] = bodyContent.get(i+start)[col];
			return res;
		}
		return null;
	}
	
	public void putRow(int row, Object[] data) {
		if(row >= bodyContent.size())
			add(data);
		else if(row >= 0) {
			bodyContent.add(row, data);
			bodyContent.remove(row+1);
		}
	}
	
	public void add(Object[] data) {
		if(null != data && (data.length == getColCount() || getRowCount() == 0))
			bodyContent.add(data);
	}
	
	public IDataEntry copy() {
		TableEntry entry = new TableEntry();
		for(int i=0; i<bodyContent.size(); i++)
			entry.add(bodyContent.get(i));
		
		return entry;
	}
	
	public boolean remove(int row) {
		return (null != bodyContent.remove(row));
	}
	
	private ArrayList<Object[]> bodyContent;	//ArrayList of arrays
}
