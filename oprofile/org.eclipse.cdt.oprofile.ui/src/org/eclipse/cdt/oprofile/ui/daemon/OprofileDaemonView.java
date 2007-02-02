/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.daemon;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

/**
 * A view to display the oprofied log file.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class OprofileDaemonView extends ViewPart {
	private TextViewer _viewer;
	private LogReader _reader;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite top) {
		_defineLayout(top);
		_defineActions(top);
		
		// Create log reader runnable
		_reader = new LogReader(_viewer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		_reader.dispose();
	}
	
	// Defines the actions associated with this view
	private void _defineActions(Composite top) {
		//DumpAction dumpAction = new DumpAction("label");
		//IActionBars actionBars = getViewSite().getActionBars();
		
		/* THIS ADDS TO OUR LOCAL TOOLBAR/MENU
		// Add menu contributions
		IMenuManager menubar = actionBars.getMenuManager();
		menubar.add(dumpAction);
		//menubar.add(new StopAction());
		
		// Add toolbar contributions
		IToolBarManager toolbar = actionBars.getToolBarManager();
		toolbar.add(dumpAction);
		//toolbar.add(new StopAction());
		 */
	}
	
	// Defines the UI layout for this view
	private void _defineLayout(Composite top) {
		_viewer = new TextViewer(top, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		_viewer.setDocument(new Document());
		_viewer.setEditable(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// Start the reader -- can I invoke the run method directly?
		Display.getCurrent().timerExec(100, _reader);
	}
}
