package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import java.text.MessageFormat;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.IAxis;
import org.swtchart.ISeries;

/**
 * This is a specialized mouse listener for displaying the value of a category (bar)
 * when the user hovers over it with the mouse. The value is displayed in a
 * tooltip popup near the mouse.
 * @author aferrazz
 * @since 3.0
 */
public class BarChartMouseMoveListener extends ToolTipChartMouseMoveListener {
	public BarChartMouseMoveListener(BarChart chart, Composite parent) {
		super(chart, parent);
	}

	@Override
	public void mouseMove(MouseEvent e) {
		super.mouseMove(e);
		ISeries[] allSeries = chart.getSeriesSet().getSeries();
		if (allSeries.length == 0) {
			return;
		}
		IAxis xAxis = chart.getAxisSet().getXAxis(0);
		String[] categorySeries = ((BarChart) chart).getCategorySeries();
		int barIndex = (int) xAxis.getDataCoordinate(e.x);
		if (0 <= barIndex && barIndex < categorySeries.length) {
			String textTip = ""; //$NON-NLS-1$
			for (int i = 0; i < allSeries.length; i++) {
				textTip = textTip.concat((i > 0 ? "\n" : "") + MessageFormat.format(Messages.BarChartBuilder_ToolTipCoords, //$NON-NLS-1$ //$NON-NLS-2$
						allSeries[i].getId(), allSeries[i].getYSeries()[barIndex]));
			}
			setTextTip(textTip);
		} else {
			tipShell.setVisible(false);
		}
		chart.redraw();
	}
}