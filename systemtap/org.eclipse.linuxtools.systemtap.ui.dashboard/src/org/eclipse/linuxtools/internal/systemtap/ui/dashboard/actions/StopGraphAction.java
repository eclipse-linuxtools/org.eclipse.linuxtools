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
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * This action is responsible for stopping an active graph.  This is used when a module
 * has already been started, but the user no longer wants to see one of the graphs.
 * This will make the selected graph inactive, with the option for bringing it back up
 * at a future time.
 * @author Ryan Morse
 */
public class StopGraphAction extends Action implements IViewActionDelegate {
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
	 * disposes of the graph from the display.
	 * @param selected The graph item that is currently selected and needs to be deactivated
	 */
	public void run(GraphTreeNode selected) {
		DashboardGraphData data = ((DashboardGraphData)selected.getData());
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DashboardView.ID);
		DashboardView dv = (DashboardView)ivp;
		dv.removeGraph(data.adapter,data.adapter.getmodulename());
		data.adapter = null;

		ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
		ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
		ambv.getViewer().refresh();
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
					action.setEnabled(false);
					return;
				}
			}
		}
		action.setEnabled(true);
	}

	private GraphTreeNode selectedItem;
}
