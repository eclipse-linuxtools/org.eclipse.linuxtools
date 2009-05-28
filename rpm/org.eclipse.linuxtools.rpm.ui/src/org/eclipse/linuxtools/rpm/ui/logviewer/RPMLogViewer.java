/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

package org.eclipse.linuxtools.rpm.ui.logviewer;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * A view to display the oprofied log file.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class RPMLogViewer extends ViewPart {

	private TextViewer _viewer;
	private LogReader _reader;
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
			public String getColumnText(Object obj, int index) {
				return getText(obj);
			}
			public Image getColumnImage(Object obj, int index) {
				return getImage(obj);
			}
			@Override
			public Image getImage(Object obj) {
				return PlatformUI.getWorkbench().
						getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			}
		}
		
		public RPMLogViewer() {
		}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite top) {
		_viewer = new TextViewer(top, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		_defineLayout(top);
		
		// Create log reader runnable
		_reader = new LogReader(_viewer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		_reader.dispose();
	}
	
	// Defines the UI layout for this view
	private void _defineLayout(Composite top) {
		
		_viewer.setDocument(new Document());
		_viewer.setEditable(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// Start the reader -- can I invoke the run method directly?
		Display.getCurrent().timerExec(2000, _reader);
//		Display.getCurrent().asyncExec(_reader);
	}
}
