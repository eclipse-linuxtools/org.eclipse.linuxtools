/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.dashboard.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.views.DashboardView;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardGraphData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.GraphTreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * This action is responsible for starting up a closed graph.  This is used when a module
 * has already been started, but one of the graphs is not being displayed for what ever reason.
 * This will make the selected graph active again.
 * @author Ryan Morse
 */
public class ActivateGraphAction extends Action implements IViewActionDelegate {
	@Override
	public void init(IViewPart view) {
		selectedItem = null;
	}

	@Override
	public void run(IAction act) {
		run();
	}

	@Override
	public void run() {
		run(selectedItem);
	}

	/**
	 * This method retreives the graph information for the selected item and then
	 * creates the graph for display.
	 * @param selected The graph item that is currently selected and needs to be activated
	 */
	public void run(GraphTreeNode selected) {
		Display disp = PlatformUI.getWorkbench().getDisplay();
		disp.getActiveShell().setCursor(disp.getSystemCursor(SWT.CURSOR_WAIT));

		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(DashboardView.ID);

		DashboardGraphData data = ((DashboardGraphData) selected.getData());
		data.adapter = ((DashboardView) ivp).createGraph(data.graph, data.data,
				data.moduleName);

		ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(ActiveModuleBrowserView.ID);
		ActiveModuleBrowserView ambv = (ActiveModuleBrowserView) ivp;
		ambv.getViewer().refresh();
		disp.getActiveShell().setCursor(null);
	}

	/**
	 * This method updates what item is currently selected.
	 * @param action The action that started this method.
	 * @param selection The newly selected item
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection selected = (IStructuredSelection)selection;
			Object o = selected.getFirstElement();
			if(o instanceof GraphTreeNode) {
				selectedItem = (GraphTreeNode)o;
				o = selectedItem.getData();
				if(null == ((DashboardGraphData)o).adapter) {
					action.setEnabled(true);
					return;
				}
			}
		}
		action.setEnabled(false);
	}

	private GraphTreeNode selectedItem;

	public void run(GraphTreeNode selected, String module) {
		Display disp = PlatformUI.getWorkbench().getDisplay();
		disp.getActiveShell().setCursor(disp.getSystemCursor(SWT.CURSOR_WAIT));

		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(DashboardView.ID);

		DashboardGraphData data = ((DashboardGraphData) selected.getData());
		data.adapter = ((DashboardView) ivp).createGraph(data.graph, data.data,
				module);

		ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(ActiveModuleBrowserView.ID);
		ActiveModuleBrowserView ambv = (ActiveModuleBrowserView) ivp;
		ambv.getViewer().refresh();
		disp.getActiveShell().setCursor(null);
	}
}
