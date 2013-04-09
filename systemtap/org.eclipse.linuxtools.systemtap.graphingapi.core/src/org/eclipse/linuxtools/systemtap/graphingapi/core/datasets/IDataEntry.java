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

package org.eclipse.linuxtools.systemtap.graphingapi.core.datasets;

public interface IDataEntry {
	int getRowCount();
	int getColCount();
	Object get(String key, int col);
	Object[] getRow(int row);
	Object[] getRow(String key);
	Object[] getColumn(int col);
	Object[][] getData();
	IDataEntry copy();
	void putRow(int row, Object[] data);
	boolean remove(int row);
}
