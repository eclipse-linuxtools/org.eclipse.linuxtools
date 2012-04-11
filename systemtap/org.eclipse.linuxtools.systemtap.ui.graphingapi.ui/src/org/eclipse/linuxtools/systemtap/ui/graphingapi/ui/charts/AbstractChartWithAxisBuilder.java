package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;

import org.swtchart.IAxis;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;

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
    //protected Axis xAxis = null;

    /**
     * Y axis.
     */
    //protected Axis yAxis = null;

    /**
     * Constructor.
     * 
     * @param dataSet
     *            data for chart
     */
    
    public AbstractChartWithAxisBuilder(IAdapter adapter, Composite parent, int style, String title) {
    	 super(adapter, parent, style, title);
	}

	/**
	 * Builds X axis.
	 */
	protected void buildXAxis() {
		String labels[] = adapter.getLabels();
		IAxis xAxis = this.chart.getAxisSet().getXAxis(0);
		xAxis.getGrid().setStyle(LineStyle.NONE);
		xAxis.getTick().setForeground(BLACK);
		ITitle xTitle = xAxis.getTitle();
		xTitle.setForeground(BLACK);

		if (labels.length > 0)
			xTitle.setText(labels[0]);
		else
			xTitle.setText("");
	}

	/**
	 * Builds Y axis.
	 */
	protected void buildYAxis() {
		String labels[] = adapter.getLabels();
		IAxis yAxis = this.chart.getAxisSet().getYAxis(0);
		yAxis.getTitle().setText("");
		yAxis.getGrid().setStyle(LineStyle.SOLID);
		yAxis.getTick().setForeground(BLACK);
	}
}
