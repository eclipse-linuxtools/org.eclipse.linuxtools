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

package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole.ScriptConsoleObserver;
import org.eclipse.ui.PlatformUI;



/**
 * A class that handles stopping the <code>ScriptConsole</code>.
 * @author Ryan Morse
 * @since 2.0
 */
public class StopScriptAction extends ConsoleAction implements ScriptConsoleObserver {

	/**
	 * This is the main method of the class. It handles stopping the
	 * currently active <code>ScriptConsole</code>.
	 */
	@Override
	public void run() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if(null != console){
					console.stop();
				}
			}
		});
	}

	/**
	 * @since 2.0
	 */
	public StopScriptAction(ScriptConsole fConsole) {
		super(fConsole,
				ConsoleLogPlugin.getDefault().getBundle().getEntry("icons/actions/stop_script.gif"), //$NON-NLS-1$
				Localization.getString("action.stopScript.name"), //$NON-NLS-1$
				Localization.getString("action.stopScript.desc")); //$NON-NLS-1$
		console.addScriptConsoleObserver(this);
	}

	/**
	 * @since 2.0
	 */
	@Override
	public void runningStateChanged(boolean started, boolean stopped) {
		setEnabled(!stopped);
	}

}
