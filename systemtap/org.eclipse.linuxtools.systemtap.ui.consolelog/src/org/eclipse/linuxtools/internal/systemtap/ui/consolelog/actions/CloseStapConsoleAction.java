/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole.ScriptConsoleObserver;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

public class CloseStapConsoleAction extends ConsoleAction implements ScriptConsoleObserver {

	@Override
	public void run() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if(null != console){
					ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{console});
				}
			}
		});
	}

	public CloseStapConsoleAction(ScriptConsole fConsole) {
		super(fConsole,
				ConsoleLogPlugin.getDefault().getBundle().getEntry("icons/actions/progress_rem.gif"), //$NON-NLS-1$
				Localization.getString("action.closeConsole.name"), //$NON-NLS-1$
				Localization.getString("action.closeConsole.desc")); //$NON-NLS-1$
		setEnabled(false);
		console.addScriptConsoleObserver(this);
	}

	@Override
	public void runningStateChanged(boolean started, boolean stopped) {
		setEnabled(stopped);
	}

}
