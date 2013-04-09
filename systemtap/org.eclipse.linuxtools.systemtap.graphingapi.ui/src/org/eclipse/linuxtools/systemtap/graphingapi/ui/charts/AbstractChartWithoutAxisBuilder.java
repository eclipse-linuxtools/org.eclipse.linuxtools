package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;


/**
 * Builds the chart with axis.
 * 
 * @author Qi Liang
 */
public abstract class AbstractChartWithoutAxisBuilder extends AbstractChartBuilder {

    /**
     * Constructor.
     * 
     * @param dataSet
     *            data for chart
     */
    
    public AbstractChartWithoutAxisBuilder(IAdapter adapter, Composite parent, int style, String title) {
    	 super(adapter, parent, style, title);
	}
}
