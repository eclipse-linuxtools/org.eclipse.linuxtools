/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

package org.eclipse.linuxtools.rpm.ui.logviewer;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;

/**
 * A view to display the oprofied log file.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class RPMLogViewer extends ViewPart {

	private TextViewer _viewer;
	private LogReader _reader;
	private String _logfile;
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
			public String getColumnText(Object obj, int index) {
				return getText(obj);
			}
			public Image getColumnImage(Object obj, int index) {
				return getImage(obj);
			}
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
	public void createPartControl(Composite top) {
		_viewer = new TextViewer(top, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		_defineLayout(top);
		_defineActions(top);
		
		// Create log reader runnable
		_reader = new LogReader(_viewer);
	}
	
	private void hookContextMenu() {
			MenuManager menuMgr = new MenuManager("#PopupMenu");
			menuMgr.setRemoveAllWhenShown(true);
//			menuMgr.addMenuListener(new IMenuListener() {
//				public void menuAboutToShow(IMenuManager manager) {
//					RPMLogViewer.this.fillContextMenu(manager);
//				}
//			});
			Menu menu = menuMgr.createContextMenu(_viewer.getControl());
			_viewer.getControl().setMenu(menu);
			getSite().registerContextMenu(menuMgr, _viewer);
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
		
		_viewer.setDocument(new Document());
		_viewer.setEditable(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// Start the reader -- can I invoke the run method directly?
		Display.getCurrent().timerExec(2000, _reader);
//		Display.getCurrent().asyncExec(_reader);
	}
}
