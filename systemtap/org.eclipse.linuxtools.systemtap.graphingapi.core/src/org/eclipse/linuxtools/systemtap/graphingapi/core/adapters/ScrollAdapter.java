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

package org.eclipse.linuxtools.systemtap.graphingapi.core.adapters;

import java.util.Arrays;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.core.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IHistoricalDataSet;

public class ScrollAdapter implements IAdapter {
	public ScrollAdapter(IHistoricalDataSet data, int xSeries, int[] ySeries, String key) {
		this.data = data;
		this.xSeries = xSeries;
		this.ySeries = Arrays.copyOf(ySeries, ySeries.length);
		this.key = key;
	}

	@Override
	public Number getYSeriesMax(int y, int start, int end) {
		return getSeriesMax(ySeries[y], start, end);
	}

	@Override
	public Number getSeriesMax(int series, int start, int end) {
		if(start < 0 || end > data.getRowCount() || start > end)
			return null;

		Number max = new Double(Double.NEGATIVE_INFINITY);
		Number cur;

		Object[] dataColumn = data.getHistoricalData(key, series, start, end);
		for(int i=0; i<dataColumn.length; i++) {
			try {
				cur = new Double(Double.parseDouble(dataColumn[i].toString()));
				if(max.doubleValue() < cur.doubleValue())
					max = cur;
			} catch (NumberFormatException e) {}
		}
		return max;
	}

	@Override
	public String[] getLabels() {
		String[] labels = data.getTitles();

		String[] labels2 = new String[ySeries.length + 1];
		labels2[0] = (IDataSet.COL_ROW_NUM == xSeries) ? Localization.getString("ScrollAdapter.RowNum") : labels[xSeries]; //$NON-NLS-1$

		for(int i=0; i<ySeries.length; i++)
			labels2[i+1] = labels[ySeries[i]];

		return labels2;
	}

	@Override
	public int getSeriesCount() {
		return ySeries.length;
	}

	@Override
	public int getRecordCount() {
		return data.getEntryCount();
	}

	@Override
	public Object[][] getData() {
		return getData(0, getRecordCount());
	}

	//[Row][Column]
	@Override
	public Object[][] getData(int start, int end) {
		Object[][] o = new Object[Math.min(end-start,getRecordCount())][ySeries.length+1];

		Object[] x = data.getHistoricalData(key, xSeries, start, end);
		Object[][] y = new Object[ySeries.length][data.getEntryCount()];

		for(int i=0; i<ySeries.length; i++)
			y[i] = data.getHistoricalData(key, ySeries[i], start, end);

		for(int j,i=0; i<o.length; i++) {
			o[i][0] = x[i];
			for(j=0; j<ySeries.length; j++)
				o[i][j+1] = y[j][i];
		}

		return o;
	}

	private IHistoricalDataSet data;
	private int xSeries;
	private int[] ySeries;
	private String key;
}
