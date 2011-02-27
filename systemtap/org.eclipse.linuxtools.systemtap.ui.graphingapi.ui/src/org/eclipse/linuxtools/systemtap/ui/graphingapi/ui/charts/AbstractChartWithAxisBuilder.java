
package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts;

import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;

/**
 * Builds the chart with axis.
 * 
 * @author Qi Liang
 */
public abstract class AbstractChartWithAxisBuilder extends AbstractChartBuilder {

    /**
     * Title of X axis.
     */
    protected String xTitle = null;

    /**
     * Title of Y axis.
     */
    protected String yTitle = null;

    /**
     * X axis.
     */
    protected Axis xAxis = null;

    /**
     * Y axis.
     */
    protected Axis yAxis = null;

    /**
     * Constructor.
     * 
     * @param dataSet
     *            data for chart
     */
    
    public AbstractChartWithAxisBuilder(IAdapter adapter, Composite parent, int style) {
    	 super(adapter, parent, style);
	}

	/*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.chart.AbstractChartBuilder#createChart()
     */
    protected void createChart() {
        chart = ChartWithAxesImpl.create();
    }
    
    public void updateDataSet()
    {
    	
    }
}
