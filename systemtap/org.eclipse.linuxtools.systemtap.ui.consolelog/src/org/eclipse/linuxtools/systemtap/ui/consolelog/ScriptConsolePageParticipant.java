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
import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions.SaveLogAction;
import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions.StopScriptAction;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * This class is responsible for creating and initializing UI for a {@link ScriptConsole}
 * @since 2.0
 */
public class ScriptConsolePageParticipant implements IConsolePageParticipant {


	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole iConsole) {
		if (!(iConsole instanceof ScriptConsole)){
			return;
		}

		ScriptConsole console = (ScriptConsole) iConsole;

		StopScriptAction stopScriptAction = new StopScriptAction(console);
		SaveLogAction saveLogAction = new SaveLogAction(console);

		// contribute to toolbar
		IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, stopScriptAction);
		manager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, saveLogAction);

		//TODO if {@link ModifyParsingAction} is restored, it is to be used here,
		//in the same way stopScriptAction and saveLogAction are used.
	}

	@Override
	public void dispose() {
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

}
