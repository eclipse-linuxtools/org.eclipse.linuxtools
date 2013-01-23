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

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole.ScriptConsoleObserver;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;



/**
 * A class that handles stopping the <code>ScriptConsole</code>.
 * @author Ryan Morse
 */
public class StopScriptAction extends Action implements ScriptConsoleObserver {

	private ScriptConsole console;

	/**
	 * This is the main method of the class. It handles stopping the
	 * currently active <code>ScriptConsole</code>.
	 */
	@Override
	public void run() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				if(null != console && console.isRunning()){
					console.stop();
				}
			}
		});
	}

	public StopScriptAction(IWorkbenchWindow workbenchWindow,
			ScriptConsole fConsole) {

		this.console = fConsole;
		console.addScriptConsoleObserver(this);

		URL location = ConsoleLogPlugin.getDefault().getBundle().getEntry("icons/actions/stop_script.gif"); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromURL(location));
		setText(Localization.getString("action.stopScript.name")); //$NON-NLS-1$
		setToolTipText(Localization.getString("action.stopScript.desc")); //$NON-NLS-1$

	}

	public void runningStateChanged(boolean running) {
		setEnabled(running);
	}

}
