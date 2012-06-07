/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.BrowserView;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;


/**
 * This <code>Action</code> expands or collapses the Viewer to the level of the element that the
 * user selected.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class TreeExpandCollapseAction extends Action  implements ISelectionListener,IWorkbenchAction {
	private final IWorkbenchWindow fWindow;
	private IStructuredSelection selection;
	private final Class<?> cl;
	
	/**
	 * The default constructor. Takes a <code>Class</code> representing the viewer that it is to expand
	 * or collapse, as there is only one in the workbench at a time.
	 * @param cls	<code>Class</code> of the viewer to expand/collapse
	 */
	public TreeExpandCollapseAction(Class<?> cls) {
		super();
		LogManager.logDebug("Start TreeExpandCollapseAction: cls-" + cls, this); //$NON-NLS-1$
		fWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		fWindow.getSelectionService().addSelectionListener(this);
		cl = cls;
		LogManager.logDebug("End TreeExpandCollapseAction:", this); //$NON-NLS-1$
	}

	/**
	 * Updates <code>selection</code> with the current selection whenever the user changes
	 * the current selection.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		LogManager.logDebug("Start selectionChanged: part-" + part + ", incoming-" + incoming, this); //$NON-NLS-1$ //$NON-NLS-2$
		if (incoming instanceof IStructuredSelection) {
			selection = (IStructuredSelection) incoming;
			setEnabled(selection.size() == 1);
		} else {
			// Other selections, for example containing text or of other kinds.
			setEnabled(false);
		}
		LogManager.logDebug("End selectionChanged:", this); //$NON-NLS-1$
	}

	public void dispose() {
		LogManager.logDebug("Start dispose:", this); //$NON-NLS-1$
		fWindow.getSelectionService().removeSelectionListener(this);
		LogManager.logDebug("End dispose:", this); //$NON-NLS-1$
	}

	/**
	 * The main body of the action. Expands or Collapses the viewer specified at construction to
	 * the level of the current selection.
	 */
	public void run() {
		LogManager.logDebug("Start run:", this); //$NON-NLS-1$
		if(!(cl.equals(FunctionBrowserView.class) || cl.equals(ProbeAliasBrowserView.class) || cl.equals(KernelBrowserView.class))) {
			LogManager.logDebug("End run:", this); //$NON-NLS-1$
			return;
		}
		IViewReference[] references = fWindow.getActivePage().getViewReferences();
		IViewPart part = null;
		boolean found = false;
		for(int i = 0; i < references.length; i++) {
			part = references[i].getView(false);
			if(part == null)
				continue;
			if(part.getClass().equals(cl)) {
				found = true;
				break;
			}
		}
		if(!found) {
			LogManager.logDebug("End run:", this); //$NON-NLS-1$
			return;
		}
		if(part == null) {
			LogManager.logDebug("End run:", this); //$NON-NLS-1$
			return;
		}
		BrowserView viewer = (BrowserView) part;
		ISelection incoming = viewer.getViewer().getSelection();
		IStructuredSelection selection = (IStructuredSelection)incoming;
		Object o = selection.getFirstElement();

		if(o == null) {
			LogManager.logDebug("End run:", this); //$NON-NLS-1$
			return;
		}

		Object[] objs = viewer.getViewer().getVisibleExpandedElements();
		boolean doExpand = true;

		for(int i = 0; i < objs.length; i++)
			if(objs[i] == o)
				doExpand = false;

		if(doExpand) {
			//System.out.println("Attempting to expand at node "+ o.toString());
			viewer.getViewer().expandToLevel(o,1);
		} else {
			//System.out.println("Attempting to collapse at node " + o.toString());
			viewer.getViewer().collapseToLevel(o,1);	
		}
		LogManager.logDebug("End run:", this); //$NON-NLS-1$
	}
}
