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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets;

public interface IDataEntry {
	public int getRowCount();
	public int getColCount();
	public Object get(String key, int col);
	public Object[] getRow(int row);
	public Object[] getRow(String key);
	public Object[] getColumn(int col);
	public Object[][] getData();
	public IDataEntry copy();
	public void putRow(int row, Object[] data);
	public boolean remove(int row);
}
