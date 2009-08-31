package org.eclipse.linuxtools.systemtap.localgui.graphing.graphlisteners;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class StapGraphMouseExitListener implements Listener{
	private StapGraphMouseMoveListener listener;
	
	public StapGraphMouseExitListener(StapGraphMouseMoveListener l) {
		this.listener = l;
	}

	@Override
	public void handleEvent(Event event) {
		listener.setStop(true);
	}

}
