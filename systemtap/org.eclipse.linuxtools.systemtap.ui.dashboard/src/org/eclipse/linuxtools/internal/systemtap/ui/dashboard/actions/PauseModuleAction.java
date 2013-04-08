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
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.structures.listeners.IActionListener;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ActiveModuleData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModule;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * This action is used to pause the selected dashboard module.  This is only a fake pause.
 * The graph is left open, and the SystemTap script is stopped.  The user can then start
 * the script running again and have it output to the same graph.  This provides the illusion
 * of being paused.
 * @author Ryan Morse
 */
public class PauseModuleAction extends Action implements IViewActionDelegate, IWorkbenchWindowActionDelegate {
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
	 * This is the main method of the class. It handles the "pretend" pause of the module.  The stap process is
	 * stopped but the graph is left open.
	 */
	@Override
	public void run() {
		DashboardModule module = GetSelectedModule.getModule(view);
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
		ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
		if(ambv.isActive(module)) {
			ActiveModuleData amd = ambv.pause(module);
			if (amd.cmd.isRunning()) {
			amd.cmd.stop();
			amd.cmd.dispose(); }
			amd.paused = true;
		}
		fireActionEvent();
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
			StopModuleAction.addActionListener(stopListener);
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
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DashboardModuleBrowserView.ID);
		final DashboardModuleBrowserView dmbv = (DashboardModuleBrowserView)ivp;
		dmbv.getViewer().removeSelectionChangedListener(moduleListener);

		ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
		final ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
		ambv.getViewer().removeSelectionChangedListener(activeModuleListener);

		RunModuleAction.removeActionListener(runListener);
		StopModuleAction.removeActionListener(stopListener);

		view = null;
		act = null;
	}

	/**
	 * Adds a new listener to the button to inform others when the pause button
	 * is pressed.
	 * @param listener The class interested in knowing when scripts are paused
	 */
	public static void addActionListener(IActionListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener from getting pause events.
	 * @param listener The class that no longer should receive pause notices
	 */
	public static void removeActionListener(IActionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * This method fires the event when a module is paused to every listener
	 * that is registered.
	 */
	private static void fireActionEvent() {
		for(IActionListener listener:listeners) {
			listener.handleActionEvent();
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
	 * Disables this action everytime a module is stopped.
	 */
	private final IActionListener stopListener = new IActionListener() {
		@Override
		public void handleActionEvent() {
			setEnablement(false);
		}
	};

	/**
	 * This method checks to see if the newly selected item in the
	 * ActiveModuleBrowserView is paused or not.  It will then set the
	 * enablement based on whether or not it is already paused.
	 */
	private final ISelectionChangedListener activeModuleListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent e) {
			TreeNode node = (TreeNode)((StructuredSelection)(e.getSelection())).getFirstElement();
			if(((ActiveModuleData)node.getData()).paused) {
				setEnablement(false);
			} else {
				setEnablement(true);
			}
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
			if(0 == childcount && amdv.isActive((DashboardModule)node.getData()) && !amdv.isPaused((DashboardModule)node.getData())) {
				setEnablement(true);
			} else if(childcount > 0) {
				boolean pause = false;
				for(int j=0; j<childcount; j++) {
					if(amdv.isActive((DashboardModule)node.getChildAt(j).getData()) && !amdv.isPaused((DashboardModule)node.getChildAt(j).getData())) {
						pause = true;
						break;
					}
				}
				if (pause == true ) {
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
