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

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.views.DashboardView;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.structures.listeners.IActionListener;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ActiveModuleData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ActiveModuleTreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardGraphData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModule;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.GraphTreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * This action is used to stop the selected dashboard module.  The graphs are all
 * closed, the process is killed, and the item is removed from tha ActiveModuleBrowserView.
 * @author Ryan Morse
 */
public class StopModuleAction extends Action implements IViewActionDelegate, IWorkbenchWindowActionDelegate {
	@Override
	public void init(IViewPart view) {
		this.view = view;
	}

	@Override
	public void init(IWorkbenchWindow window) {}

	@Override
	public void run(IAction act) {
		run();
	}

	/**
	 * This is the main method of the class. It handles stopping of the module.
	 * All of the graphs are closed, the process is killed, and the item
	 * is removed from the ActiveModuleBrowserView.
	 */
	@Override
	public void run() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				TreeNode treenode = GetSelectedModule.getNode(view);
				DashboardModule module = null;
				if (treenode.getChildCount() == 0) {
					module = (DashboardModule) treenode.getData();
					stopmodule(module);
				} else {
					for (int j = 0; j < treenode.getChildCount(); j++) {
						module = (DashboardModule) treenode.getChildAt(j)
								.getData();
						stopmodule(module);
					}
				}
			}
		});
	}

	private void stopmodule(DashboardModule module) {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
		ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
		GraphTreeNode graphNode;
		DashboardGraphData graphData;

		if (ambv.isActive(module)) {
			ActiveModuleTreeNode node = ambv.remove(module);
			ActiveModuleData amd = (ActiveModuleData) node.getData();

			ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(DashboardView.ID);
			DashboardView dv = (DashboardView) ivp;

			for (int i = 0; i < node.getChildCount(); i++) {
				graphNode = (GraphTreeNode) node.getChildAt(i);
				graphData = (DashboardGraphData) graphNode.getData();
				if (null != graphData.adapter) {
					dv.removeGraph(graphData.adapter, module.getcategory());
					graphData.adapter = null;
				}
			}

			if (!amd.paused) {
				if (amd.cmd.isRunning()) {
					amd.cmd.stop();
				}
				amd.data = null;
			}
			dv.closeComposite(amd.module.category);
			amd = null;
			fireActionEvent();
		}

		setEnablement(false);
		buildEnablementChecks();
	}

	/**
	 * This updates the enablement of the action based on the newly selected item
	 * @param act The action that called this method
	 * @param select The newly selected item.
	 */
	@Override
	public void selectionChanged(IAction act, ISelection select) {
		this.act = act;
		setEnablement(false);
		buildEnablementChecks();
	}

	/**
	 * This method handles creating the checks that are used to determine if
	 * the action should be enabled for use.
	 */
	private void buildEnablementChecks() {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DashboardModuleBrowserView.ID);
		if(null != ivp) {
			final DashboardModuleBrowserView dmbv = (DashboardModuleBrowserView)ivp;
			dmbv.getViewer().addSelectionChangedListener(moduleListener);

			ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
			final ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
			ambv.getViewer().addSelectionChangedListener(activeModuleListener);

			RunModuleAction.addActionListener(runListener);
		}
	}

	/**
	 * Toggles whether or not the action is enabled
	 * @param enabled boolean flag representing whether the action is enabled or not
	 */
	private void setEnablement(boolean enabled) {
		act.setEnabled(enabled);
	}

	/**
	 * Removes all internal references provided by this action.  Nothing should be
	 * called or referenced after the dispose method.
	 */
	@Override
	public void dispose() {
		RunModuleAction.removeActionListener(runListener);

		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DashboardModuleBrowserView.ID);
		final DashboardModuleBrowserView dmbv = (DashboardModuleBrowserView)ivp;
		dmbv.getViewer().removeSelectionChangedListener(moduleListener);

		ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
		final ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
		ambv.getViewer().removeSelectionChangedListener(activeModuleListener);

		view = null;
		act = null;
	}

	/**
	 * Adds a new listener to the button to inform others when the stop button
	 * is pressed.
	 * @param listener The class interested in knowing when scripts are stopped
	 */
	public static void addActionListener(IActionListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener from getting stop events.
	 * @param listener The class that no longer should receive stop notices
	 */
	public static void removeActionListener(IActionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * This method fires the event when a module is run to every listener
	 * that is registered.
	 */
	private static void fireActionEvent() {
		for(int i=0; i<listeners.size(); i++) {
			listeners.get(i).handleActionEvent();
		}
	}

	/**
	 * Enables this action everytime a new module is run.
	 */
	private final IActionListener runListener = new IActionListener() {
		@Override
		public void handleActionEvent() {
			setEnablement(true);
		}
	};

	/**
	 * Enables this action everytime an item in the ActiveModuleBrowserView
	 * is selected.
	 */
	private final ISelectionChangedListener activeModuleListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent e) {
			setEnablement(true);
		}
	};

	/**
	 * This method checks to see if the newly selected item in the
	 * DashboardModuleBrowserView is running or not.  It will then set the
	 * enablement based on whether or not it is running.
	 */
	private final ISelectionChangedListener moduleListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent e) {
			TreeNode node = (TreeNode)((StructuredSelection)(e.getSelection())).getFirstElement();
			IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
			ActiveModuleBrowserView amdv = (ActiveModuleBrowserView)ivp;
			int childcount = node.getChildCount();
			if(0 == childcount && amdv.isActive((DashboardModule)node.getData())) {
				setEnablement(true);
			} else if(childcount > 0) {
				boolean active = false;

				for(int j=0; j<childcount; j++) {
					if(amdv.isActive((DashboardModule)node.getChildAt(j).getData())) {
						active = true;
						break; }
				}
				if (active == true ) {
					setEnablement(true);
				}
			} else {
				setEnablement(false);
			}
		}
	};

	private IViewPart view;
	private IAction act;
	private static ArrayList<IActionListener> listeners = new ArrayList<IActionListener>();
}
