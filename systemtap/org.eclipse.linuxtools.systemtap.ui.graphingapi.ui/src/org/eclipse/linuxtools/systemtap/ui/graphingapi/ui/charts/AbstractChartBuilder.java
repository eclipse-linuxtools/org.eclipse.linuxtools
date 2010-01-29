/****************************************************************
 * Licensed Material - Property of IBM
 *
 * ****-*** 
 *
 * (c) Copyright IBM Corp. 2006.  All rights reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 *
 ****************************************************************
 */
package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts;



import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IUpdateListener;
/**
 * Provides the common members and the framework to build one chart.
 * 
 * @author Qi Liang
 */
public abstract class AbstractChartBuilder extends Canvas implements IUpdateListener{

    /**
     * Font name for all titles, labels, and values.
     */
    protected final static String FONT_NAME = "MS Sans Serif";

    /**
     * Provides data for chart.
     */
    protected IAdapter adapter = null;
    protected int xseries;
    protected int[] yseries;

    /**
     * Chart instance.
     */
    protected Chart chart = null;

    /**
     * Chart title.
     */
    protected String title = null;

    /**
     * Constructs one chart builder and associate it to one data set.
     * 
     * @param dataSet
     *            data set
     */
    
    public AbstractChartBuilder(IAdapter adapter, Composite parent, int style) {
    	super(parent, style);
		// TODO Auto-generated constructor stub
    	this.adapter = adapter;
	}

	/**
     * Builds one chart.
     */
    public void build() {
        createChart();
        buildPlot();
        buildLegend();
        buildTitle();
        buildXAxis();
        buildYAxis();
        buildXSeries();
        buildYSeries();
        updateDataSet();
    }

    /**
     * Creates chart instance.
     */
    protected abstract void createChart();

    /**
     * Builds plot.
     */
    protected void buildPlot() {

    }

    /**
     * Builds X axis.
     */
    protected void buildXAxis() {

    }

    /**
     * Builds Y axis.
     */
    protected void buildYAxis() {

    }

    /**
     * Builds X series.
     */
    protected void buildXSeries() {

    }

    /**
     * Builds Y series.
     */
    protected void buildYSeries() {

    }

    /**
     * Builds legend.
     * 
     */
    protected void buildLegend() {

    }

    /**
     * Builds the chart title.
     */
    protected void buildTitle() {
    }

    /**
     * Returns the chart instance.
     * 
     * @return the chart instance
     */
    public Chart getChart() {
        return chart;
    }
    
    public void updateDataSet()
    {
    	
    }

	public void setScale(double scale) {
		
	}

}
