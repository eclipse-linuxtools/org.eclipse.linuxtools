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

package org.eclipse.linuxtools.systemtap.graphing.core.datasets;

public interface IHistoricalDataSet extends IDataSet {
	void append(IDataEntry entry);
	Object[] getHistoricalData(String key, int col);
	Object[] getHistoricalData(String key, int col, int start, int end);
	int getEntryCount();
	IDataEntry getEntry(int entry);
	boolean remove(int entry);
}
