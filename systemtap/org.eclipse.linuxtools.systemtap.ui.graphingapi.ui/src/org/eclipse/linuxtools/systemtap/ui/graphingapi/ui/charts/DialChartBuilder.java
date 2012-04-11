
package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts;

import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.impl.DialChartImpl;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;

/**
 * Builds the chart with axis.
 * 
 * @author Qi Liang
 */
public abstract class DialChartBuilder extends AbstractChartBuilder {

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
    
    public DialChartBuilder(IAdapter adapter, Composite parent, int style, String title) {
    	 super(adapter, parent, style, title);
	}

	/*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.chart.AbstractChartBuilder#createChart()
     */
    protected void createChart() {
        //chart = DialChartImpl.create();
    }
    
    public void updateDataSet()
    {
    	
    }
    
  
}
