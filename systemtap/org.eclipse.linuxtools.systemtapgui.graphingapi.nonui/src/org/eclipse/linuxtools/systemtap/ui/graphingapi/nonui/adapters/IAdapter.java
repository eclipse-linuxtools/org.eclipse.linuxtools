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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters;

public interface IAdapter {
	public Number getXMax();
	public Number getXMax(int start, int end);
	public Number getYMax();
	public Number getYMax(int start, int end);
	public Number getYSeriesMax(int series);
	public Number getYSeriesMax(int series, int start, int end);
	public Number getSeriesMax(int series);
	public Number getSeriesMax(int series, int start, int end);

	public Number getXMin();
	public Number getXMin(int start, int end);
	public Number getYMin();
	public Number getYMin(int start, int end);
	public Number getYSeriesMin(int series);
	public Number getYSeriesMin(int series, int start, int end);
	public Number getSeriesMin(int series);
	public Number getSeriesMin(int series, int start, int end);

	public String[] getLabels();
	public int getRecordCount();
	public int getSeriesCount();
	public Object[][] getData();
	public Object[][] getData(int start, int end);
}
