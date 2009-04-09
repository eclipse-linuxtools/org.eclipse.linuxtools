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

package org.eclipse.linuxtools.systemtapgui.consolelog.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.systemtapgui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;




/**
 * A basic core class that provides common methods that are needed by any
 * action that makes use of the Console.
 * @author Ryan Morse
 */
public abstract class ConsoleAction extends Action implements IWorkbenchWindowActionDelegate, IViewActionDelegate {
	public void init(IWorkbenchWindow window) {}
	
	public void init(IViewPart part) {}
	
	public void run(IAction action) {
		run();
	}
	
	public abstract void run();
	

	/**
	 * Finds and returns the active console.
	 * @return The active <code>ScriptConsole<code> in the ConsoleView
	 */
	protected ScriptConsole getActive() {
		IConsoleView ivp = (IConsoleView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IConsoleConstants.ID_CONSOLE_VIEW);
		return (ScriptConsole)ivp.getConsole();
	}
	
	/**
	 * Updates whether the action should be enabled when the user changes their ViewPart selection
	 */
	public void selectionChanged(IAction act, ISelection select) {
		this.setAct(act);
		buildEnablementChecks();
	}
	
	/**
	 * Builds the checks to see if the action should be enabled or not
	 */
	private void buildEnablementChecks() {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IConsoleConstants.ID_CONSOLE_VIEW);
		if(null != ivp) {
			setEnablement(isRunning((IConsoleView)ivp));
			ivp.addPropertyListener(listener);
		}
	}
	
	/**
	 * Checks to see if the active console is still running
	 */
	private boolean isRunning(IConsoleView cv) {
		ScriptConsole console = (ScriptConsole)cv.getConsole();
		return (console != null && console.isRunning());
	}
	
	/**
	 * Changes whether or not this action is enabled
	 */
	private void setEnablement(boolean enabled) {
		//act.setEnabled(enabled);	//TODO: This is disabled until we figure out how to get it to realize script is running.
	}

	/**
	 * Disposes of all internal references held by this class.  No method should be called after
	 * calling this.
	 */
	public void dispose() {
		setAct(null);
		//Unable to findView when disposed is called, as a result null pointer exception is thrown
		//PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IConsoleConstants.ID_CONSOLE_VIEW).removePropertyListener(listener);
		listener = null;
	}
	
	public void setAct(IAction act) {
		this.act = act;
	}

	public IAction getAct() {
		return act;
	}

	private IAction act;
	private IPropertyListener listener = new IPropertyListener() {
		public void propertyChanged(Object o, int i) {
			if(o instanceof IConsoleView)
				setEnablement(isRunning((IConsoleView)o));
		}
	};
}
