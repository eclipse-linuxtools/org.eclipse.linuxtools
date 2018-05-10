/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.core.adapters;

import java.util.Arrays;

import org.eclipse.linuxtools.internal.systemtap.graphing.core.Localization;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IBlockDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSet;

public class BlockAdapter implements IAdapter {
    public BlockAdapter(IBlockDataSet data, int xSeries, int[] ySeries) {
        this.data = data;
        this.xSeries = xSeries;
        this.ySeries = Arrays.copyOf(ySeries, ySeries.length);
    }

    @Override
    public Number getYSeriesMax(int y, int start, int end) {
        return getSeriesMax(ySeries[y], start, end);
    }

    @Override
    public Number getSeriesMax(int series, int start, int end) {
        if(start < 0 || end > data.getRowCount() || start > end)
            return null;

        Number max = Double.NEGATIVE_INFINITY;
        Number cur;

        Object[] dataColumn = data.getColumn(series, start, end);
        for(int i=0; i<dataColumn.length; i++) {
            try {
                cur = Double.parseDouble(dataColumn[i].toString());
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
        labels2[0] = (IDataSet.COL_ROW_NUM == xSeries) ? Localization.getString("BlockAdapter.RowNum") : labels[xSeries]; //$NON-NLS-1$

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
        return data.getRowCount();
    }

    @Override
    public Object[][] getData() {
        return getData(0, getRecordCount());
    }

    //[Row][Column]
    @Override
    public Object[][] getData(int start, int end) {
        Object[][] o = new Object[Math.min(end-start,getRecordCount())][ySeries.length+1];

        Object[] row;
        for(int j,i=0; i<o.length; i++) {
            row = data.getRow(i+start);
            o[i][0] = (IDataSet.COL_ROW_NUM == xSeries) ? Integer.valueOf(i) : row[xSeries];

            for(j=0; j<ySeries.length; j++)
                o[i][j+1] = row[ySeries[j]];
        }

        return o;
    }

    private IBlockDataSet data;
    private int xSeries;
    private int[] ySeries;
}
