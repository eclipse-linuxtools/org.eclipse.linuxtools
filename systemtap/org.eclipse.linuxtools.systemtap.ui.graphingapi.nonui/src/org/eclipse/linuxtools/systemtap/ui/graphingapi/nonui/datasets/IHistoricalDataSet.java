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

public interface IHistoricalDataSet extends IDataSet {
	public void append(IDataEntry entry);
	public Object[] getHistoricalData(String key, int col);
	public Object[] getHistoricalData(String key, int col, int start, int end);
	public int getEntryCount();
	public IDataEntry getEntry(int entry);
	public boolean remove(int entry);
}
