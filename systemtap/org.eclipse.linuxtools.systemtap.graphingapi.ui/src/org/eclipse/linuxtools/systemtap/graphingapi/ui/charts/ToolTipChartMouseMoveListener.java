package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;

/**
 * @since 3.0
 */
public class ToolTipChartMouseMoveListener extends ChartMouseMoveListener {
	private final int TIP_OFFSET = 20;
	protected final Shell tipShell;
	protected final Text tipText;

	public ToolTipChartMouseMoveListener(Chart chart, Composite parent) {
		super(chart, parent);
		tipShell = new Shell(Display.getCurrent().getActiveShell(), SWT.TOOL | SWT.ON_TOP);
		tipText = new Text(tipShell, SWT.MULTI | SWT.BOLD);
		tipShell.setVisible(false);
	}

	@Override
	public void exit() {
		super.exit();
		tipShell.setVisible(false);
	}

	protected void setTextTip(String message) {
		Point cursorLocation = Display.getCurrent().getCursorLocation();
		tipShell.setLocation(cursorLocation.x + TIP_OFFSET, cursorLocation.y + TIP_OFFSET);
		if (!message.equals(tipText.getText())) {
			tipText.setText(message);
			tipText.pack();
			tipShell.pack();
		}
		if (!tipShell.isVisible()) {
			tipShell.setVisible(true);
		}
	}
}