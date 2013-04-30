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

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetFactory;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.structures.listeners.IActionListener;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ChartStreamDaemon2;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ScpClient;
import org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs.SelectServerDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ActiveModuleData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModule;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.jcraft.jsch.JSchException;


/**
 * This action is used to run the selected dashboard module.
 * The command is setup and started.  Any graphs associated with the module
 * are also added to the main window the the user to monitor.
 * @author Ryan Morse
 */

public class RunModuleAction extends Action implements IViewActionDelegate, IWorkbenchWindowActionDelegate {

	@Override
	public void init(IViewPart view) {
		this.view = view;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	@Override
	public void run(IAction act) {
		run();
	}

	/**
	 * This is the main method of the class. It handles running of the module.
	 * The command is setup and started.  Any graphs associated with the module
	 * are also added to the main window the the user to monitor.
	 */
	@Override
	public void run() {
		// Get the treeViewer
		Display disp = PlatformUI.getWorkbench().getDisplay();
		disp.getActiveShell().setCursor(disp.getSystemCursor(SWT.CURSOR_WAIT));

		DashboardModule module = null;
		TreeNode node = GetSelectedModule.getNode(view);
		if (node.getChildCount() == 0) {
			module = (DashboardModule) node.getData();
			if (null != module) {
				runScript(module);
				setEnablement(false);
				buildEnablementChecks();
				fireActionEvent();

			}
		} else {
			for (int j = 0; j < node.getChildCount(); j++) {
				module = (DashboardModule) node.getChildAt(j).getData();
				runScript(module);
				setEnablement(false);
				buildEnablementChecks();
				fireActionEvent();
			}
		}
		disp.getActiveShell().setCursor(null);

	}

	/**
	 * Adds the newly run module to the ActiveModuleBrowserView.
	 * @param display The name used to display this module
	 * @param amd The data used in the running module.
	 */
	private void addActive(String display, ActiveModuleData amd) {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
		((ActiveModuleBrowserView)ivp).add(display, amd);
	}

	private void runScript(DashboardModule module) {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
		ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
		boolean paused = ambv.isPaused(module);
		String fileName = module.script.getAbsolutePath();
		String tmpfileName = fileName;
		String serverfileName = fileName.substring(fileName.lastIndexOf('/')+1);
		if(ConsoleLogPlugin.getDefault().getPreferenceStore().getBoolean(ConsoleLogPreferenceConstants.REMEMBER_SERVER)!=true &&
			new SelectServerDialog(fWindow.getShell()).open() == false) {
			return;
		}

		if (module.location.equalsIgnoreCase("local")) { //$NON-NLS-1$
			try {
				ScpClient scpclient = new ScpClient();
				tmpfileName = null;
				tmpfileName = "/tmp/" + serverfileName; //$NON-NLS-1$
				scpclient.transfer(fileName, tmpfileName);
			} catch (JSchException e) {
				ExceptionErrorDialog.openError(Messages.RunModuleAction_connectionError, e);
			} catch (IOException e) {
				ExceptionErrorDialog.openError(Messages.RunModuleAction_ioError, e);
			}
		}
		String modname = serverfileName.substring(0, serverfileName.indexOf('.'));
		if (modname.indexOf('-') != -1) {
			modname = modname.substring(0, modname.indexOf('-'));
		}

		//TODO: Check for prebuilt module first
		String[] cmd = new String[] {
				"stap", //$NON-NLS-1$
				"-g", //$NON-NLS-1$
				"-m", //$NON-NLS-1$
				modname,
				tmpfileName};

			   	ScriptConsole console = ScriptConsole.getInstance(serverfileName);
            console.run(cmd, null, null);
		IDataSet ds;
		if(paused) {
			ds = ambv.pause(module).data;
		} else {
			ds = DataSetFactory.createDataSet(module.dataSetID, module.labels);
		}

		console.getCommand().addInputStreamListener(new ChartStreamDaemon2(console, ds, module.parser));

			ActiveModuleData amd = new ActiveModuleData();
			amd.module = module;
			amd.cmd = console;
			amd.data = ds;
			amd.paused = false;
			addActive(module.category + "." + module.display, amd); //$NON-NLS-1$

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
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(DashboardModuleBrowserView.ID);
		if (null != ivp) {
			final DashboardModuleBrowserView dmbv = (DashboardModuleBrowserView) ivp;
			dmbv.getViewer().addSelectionChangedListener(moduleListener);

			ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(ActiveModuleBrowserView.ID);
			final ActiveModuleBrowserView ambv = (ActiveModuleBrowserView) ivp;
			ambv.getViewer().addSelectionChangedListener(activeModuleListener);

			StopModuleAction.addActionListener(stopListener);
			PauseModuleAction.addActionListener(pauseListener);
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
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(DashboardModuleBrowserView.ID);
		final DashboardModuleBrowserView dmbv = (DashboardModuleBrowserView) ivp;
		dmbv.getViewer().removeSelectionChangedListener(moduleListener);

		ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(ActiveModuleBrowserView.ID);
		final ActiveModuleBrowserView ambv = (ActiveModuleBrowserView) ivp;
		ambv.getViewer().removeSelectionChangedListener(activeModuleListener);

		StopModuleAction.removeActionListener(stopListener);
		PauseModuleAction.removeActionListener(pauseListener);
		view = null;
		act = null;
		fWindow = null;
	}

	/**
	 * Adds a new listener to the button to inform others when the run button
	 * is pressed.
	 * @param listener The class interested in knowing when scripts are run
	 */
	public static void addActionListener(IActionListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener from getting run events.
	 * @param listener The class that no longer should receive run notices
	 */
	public static void removeActionListener(IActionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * This method fires the event when a module is run to every listener
	 * that is registered.
	 */
	private static void fireActionEvent() {
		for(IActionListener listener:listeners) {
			listener.handleActionEvent();
		}
	}

	/**
	 * Enables this action everytime a module is stopped.
	 */
	private final IActionListener stopListener = new IActionListener() {
		@Override
		public void handleActionEvent() {
			setEnablement(true);
		}
	};

	/**
	 * Enables this action everytime a module is paused.
	 */
	private final IActionListener pauseListener = new IActionListener() {
		@Override
		public void handleActionEvent() {
			setEnablement(true);
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
			TreeNode node = (TreeNode) ((StructuredSelection) (e.getSelection()))
					.getFirstElement();
			if (((ActiveModuleData) node.getData()).paused) {
				setEnablement(true);
			} else {
				setEnablement(false);
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
			TreeNode node = (TreeNode) ((StructuredSelection) (e.getSelection()))
					.getFirstElement();
			IViewPart ivp = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findView(ActiveModuleBrowserView.ID);
			ActiveModuleBrowserView amdv = (ActiveModuleBrowserView) ivp;
			DashboardModule module = (node.getData() instanceof DashboardModule) ? (DashboardModule) node
					.getData() : null;
			int childcount = node.getChildCount();
			if (0 == childcount && !amdv.isActive(module)) {
				setEnablement(true);
			} else if (amdv.isActive(module) && amdv.isPaused(module)) {
				setEnablement(true);
			} else if (childcount > 0) {
				boolean active = false;

				for (int j = 0; j < childcount; j++) {
					if (amdv.isActive((DashboardModule) node.getChildAt(j)
							.getData())) {
						active = true;
						break;
					}
				}
				if (active == false) {
					setEnablement(true);
				}

			} else {
				setEnablement(false);
			}

		}
	};

	private IViewPart view;
	private static ArrayList<IActionListener> listeners = new ArrayList<IActionListener>();
	protected IWorkbenchWindow fWindow = null;
	private IAction act;

	protected int SCRIPT_ID;
	protected ScriptConsole console;
}
