package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;

/**
 * Clients may override this listener class to display data values of the chart as they
 * are hovered over by the mouse.
 * @author aferrazz
 * @since 3.0
 */
public abstract class ChartMouseMoveListener implements MouseMoveListener {
	protected final Chart chart;
	protected MouseEvent lastMouseEvent = null;

	/**
	 * Constructor.
	 * @param chart The chart that this listener is watching.
	 * @param hoverArea The plot area of the chart this listener is applied to.
	 */
	public ChartMouseMoveListener(Chart chart, final Composite hoverArea) {
		this.chart = chart;
		final MouseMoveListener thisListener = this;
		hoverArea.addMouseTrackListener(new MouseTrackAdapter() {

			@Override
			public void mouseExit(MouseEvent e) {
				hoverArea.removeMouseMoveListener(thisListener);
				exit();
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				hoverArea.addMouseMoveListener(thisListener);
				enter();
			}
		});
	}

	/**
	 * This method is called whenever the mouse enter the plot area of the chart.
	 * It may be overridden to add extra functionality.
	 */
	public void enter() { }

	/**
	 * This method is called whenever the mouse exits the plot area of the chart.
	 * It may be overridden to add extra functionality (but always include a super call).
	 */
	public void exit() {
		lastMouseEvent = null;
	}

	/**
	 * Call this method whenever the chart gets updated, so that another mouse event can be
	 * fired with the new chart contents without having to explicitly move the mouse.
	 */
	public final void update() {
		if (lastMouseEvent != null) {
			mouseMove(lastMouseEvent);
		}
	}

	/**
	 * Clients must override this method to perform appropriate actions whenever the
	 * mouse is moved while inside the chart's plot area (but always include a super call).
	 */
	@Override
	public void mouseMove(MouseEvent e) {
		lastMouseEvent = e;
	}
}