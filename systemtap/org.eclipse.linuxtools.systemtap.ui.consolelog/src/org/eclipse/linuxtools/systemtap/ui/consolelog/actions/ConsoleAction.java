/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P j
 *     Roland Grunberg
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.internal.console.ConsoleView;



/**
 * A basic core class that provides common methods that are needed by any
 * action that makes use of the Console.
 * @author Ryan Morse
 */
@SuppressWarnings("restriction")
public abstract class ConsoleAction extends Action implements IWorkbenchWindowActionDelegate, IViewActionDelegate {
	public void init(IWorkbenchWindow window) {}
	
	public void init(IViewPart part) {}
	
	public void run(IAction action) {
		run();
	}
	
	@Override
	public abstract void run();
	

	/**
	 * Finds and returns the active console.
	 * @return The active <code>ScriptConsole<code> in the ConsoleView
	 */
	protected ScriptConsole getActive() {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IConsoleConstants.ID_CONSOLE_VIEW);
		IConsole activeConsole = ((ConsoleView)ivp).getConsole();
		if (activeConsole instanceof ScriptConsole){
			return (ScriptConsole)activeConsole;
		}else{
			return null;
		}
	}
	
	/**
	 * Updates whether the action should be enabled when the user changes their ViewPart selection
	 */
	public void selectionChanged(IAction act, ISelection select) {
		this.act = act;
		buildEnablementChecks();
	}
	
	/**
	 * Builds the checks to see if the action should be enabled or not
	 */
	private void buildEnablementChecks() {
		IWorkbenchPart wbp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
		if (wbp instanceof ConsoleView){
			IViewPart ivp = (IViewPart) wbp;
			setEnablement(isRunning((ConsoleView)ivp));
			ivp.addPropertyListener(listener);
		}
	}
	
	/**
	 * Checks to see if the active console is still running
	 */
	private boolean isRunning(ConsoleView cv) {
		if (cv.getConsole() instanceof ScriptConsole){
			ScriptConsole console = (ScriptConsole)cv.getConsole();
			return (console != null && console.isRunning());
		}else{
			return false;
		}
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
		act = null;
		//Unable to findView when disposed is called, as a result null pointer exception is thrown
		//PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IConsoleConstants.ID_CONSOLE_VIEW).removePropertyListener(listener);
		listener = null;
	}
	
	@SuppressWarnings("unused")
	private IAction act;
	private IPropertyListener listener = new IPropertyListener() {
		public void propertyChanged(Object o, int i) {
			if(o instanceof ConsoleView)
				setEnablement(isRunning((ConsoleView)o));
		}
	};
}
