package org.eclipse.linuxtools.valgrind.massif.birt;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ChartControl extends Canvas {

	public ChartControl(Composite parent, Chart chart, int style) {
		super(parent, SWT.BORDER);
		setLayoutData(new GridData(GridData.FILL_BOTH));
		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		ChartRenderer renderer = new ChartRenderer();
		addPaintListener(renderer);
		addControlListener(renderer);
		renderer.setCanvas(this);
		renderer.renderModel(chart);
	}
	
}
