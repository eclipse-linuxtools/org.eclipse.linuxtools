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

package org.eclipse.linuxtools.systemtap.ui.consolelog.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;



/**
 * A class that handles stopping the <code>ScriptConsole</code>.
 * @author Ryan Morse
 */
public class StopScriptAction extends ConsoleAction {
	/**
	 * This is the main method of the class. It handles stopping the
	 * currently active <code>ScriptConsole</code>.
	 */
	@Override
	public void run() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ScriptConsole console = getActive();
				if(null != console && console.isRunning()){
					console.stop();
				}
			}
		});
	}

	@Override
	public void selectionChanged(IAction a, ISelection s) {
				a.setEnabled(anyRunning());
			}


	/**
	 * This method will stop the i'th <code>ScriptConsole</code> if it is running.
	 * @param i The index value of the console that will be stopped.
	 */
	public void run(int i) {
		IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		if (ic[i] instanceof ScriptConsole){
			ScriptConsole console = (ScriptConsole)ic[i];

			if(console.isRunning())
				console.stop();
		}
	}

	/**
	 * This method will stop all consoles that are running.
	 */
	public void stopAll() {
		IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		ScriptConsole console;

		for(int i=0; i<ic.length; i++) {
			if (ic[i] instanceof ScriptConsole){
				console = (ScriptConsole)ic[i];
				if(console.isRunning())
					console.stop();
			}
		}
	}

	/**
	 * This method will check to see if any scripts are currently running.
	 * @return - boolean indicating whether any scripts are running
	 */
	public boolean anyRunning() {
		IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		ScriptConsole console;

		for(int i=0; i<ic.length; i++) {
			if (ic[i] instanceof ScriptConsole){
				console = (ScriptConsole)ic[i];
				if(console.isRunning())
					return true;
			}
		}
		return false;
	}
}
