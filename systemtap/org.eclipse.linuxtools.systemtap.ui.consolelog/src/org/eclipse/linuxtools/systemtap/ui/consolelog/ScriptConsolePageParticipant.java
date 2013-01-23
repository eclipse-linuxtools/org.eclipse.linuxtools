/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.linuxtools.systemtap.ui.consolelog.actions.StopScriptAction;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * This class is responsible for creating and initializing UI for a {@link ScriptConsole}
 */
public class ScriptConsolePageParticipant implements IConsolePageParticipant {


	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public void init(IPageBookViewPage page, IConsole console) {
		if (!(console instanceof ScriptConsole)){
			return;
		}

		StopScriptAction stopScriptAction = new StopScriptAction(page
				.getSite().getWorkbenchWindow(), (ScriptConsole) console);

		// contribute to toolbar
		IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, stopScriptAction);
	}

	public void dispose() {
	}

	public void activated() {
	}

	public void deactivated() {
	}

}
