/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Renato Stoffalette Joao <rsjoao@br.ibm.com>
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.piechart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;

public class PieChart extends Chart {

	protected List<RGB> colorList = new ArrayList<RGB>();

	/**
	 * A PieChart with no titles given to its pies.
	 * @param parent
	 * @param style
	 * @deprecated See {@link #PieChart(Composite, int, String[])}
	 */
	@Deprecated
    public PieChart(Composite parent, int style) {
        this(parent, style, new String[0]);
    }

    /**
     * A PieChart with titles given to each pie it draws.
     * @param parent The parent composite.
     * @param style The style of the parent composite.
     * @param labels An array containing the legend title (index 0) and
     * the title of each pie chart that is to be drawn (index >=1).
     * A null / not present title indicates no title.
     * @since 1.1
     */
    public PieChart(Composite parent, int style, String labels[]) {
        super(parent, style);
        Control plotArea = null;
        Control legendArea = null;
        for (Control child : getChildren()) {
            if (child.getClass().getName().equals("org.swtchart.internal.axis.AxisTitle")) { //$NON-NLS-1$
				child.setVisible(false); // Don't show original Plot Area and axis
			} else if (child.getClass().getName().equals("org.swtchart.internal.PlotArea")) { //$NON-NLS-1$
                child.setVisible(false); // Don't show original Plot Area and axis
                plotArea = child;
            } else if (child.getClass().getName().equals("org.swtchart.internal.Legend")) { //$NON-NLS-1$
                legendArea = child;
            }
        }
        this.addPaintListener(new PieChartPaintListener(this, plotArea, legendArea, labels));
    }

    @Override
    public void addPaintListener(PaintListener listener) {
        if (!listener.getClass().getName().startsWith("org.swtchart.internal.axis")) { //$NON-NLS-1$
			super.addPaintListener(listener);
		}
    }

    /**
     * Add data to this Pie Chart. We'll build one pie chart for each value in the array provided. The val matrix must
     * have an array of an array of values. Ex. labels = {'a', 'b'} val = {{1,2,3}, {4,5,6}} This will create 3 pie
     * charts. For the first one, 'a' will be 1 and 'b' will be 4. For the second chart 'a' will be 2 and 'b' will be 5.
     * For the third 'a' will be 3 and 'b' will be 6.
     * @param labels The titles of each series. (These are not the same as titles given to pies.)
     */
    public void addPieChartSeries(String labels[], double val[][]) {
        for (ISeries s : this.getSeriesSet().getSeries()) {
			this.getSeriesSet().deleteSeries(s.getId());
		}

        int size = Math.min(labels.length, val.length);
        for (int i = 0; i < size; i++) {
            IBarSeries s = (IBarSeries) this.getSeriesSet().createSeries(ISeries.SeriesType.BAR, labels[i]);
            double d[] = new double[val[i].length];
            for (int j = 0; j < val[i].length; j++) {
				d[j] = val[i][j];
			}
            s.setXSeries(d);
            s.setBarColor(new Color(this.getDisplay(), sliceColor(i)));
        }
    }

    protected RGB sliceColor(int i) {
    	if (colorList.size() > i) {
    		return colorList.get(i);
    	}

    	RGB next;

    	if (colorList.size() < IColorsConstants.COLORS.length) {
    		next = IColorsConstants.COLORS[i];
    	}
    	else {
    		RGB prev = colorList.get(colorList.size()-1);
    		int mod = 192;
    		int red = (int) (mod * Math.random());
    		int green = (int) ((mod - red) * Math.random());
    		int blue = mod - red - green;
    		next = new RGB(0, 0, 0);
    		next.red = (prev.red + red) % 256;
    		next.green = (prev.green + green) % 256;
    		next.blue = (prev.blue + blue) % 256;
    	}

    	colorList.add(next);
    	return next;
    }
}
