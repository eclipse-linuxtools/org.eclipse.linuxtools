/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions.CloseStapConsoleAction;
import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions.SaveLogAction;
import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions.StopScriptAction;
import org.eclipse.linuxtools.systemtap.structures.process.SystemTapRuntimeProcessFactory;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * This class is responsible for creating and initializing UI for a {@link ScriptConsole}
 * @since 2.0
 */
public class ScriptConsolePageParticipant implements IConsolePageParticipant, IDebugContextListener {

	private IPageBookViewPage fPage;
	private IConsoleView fView;
	private ScriptConsole fConsole;

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

		fPage = page;
		fConsole = (ScriptConsole) iConsole;
		fView = (IConsoleView) fPage.getSite().getPage().findView(IConsoleConstants.ID_CONSOLE_VIEW);

		StopScriptAction stopScriptAction = new StopScriptAction(fConsole);
		CloseStapConsoleAction closeConsoleAction = new CloseStapConsoleAction(fConsole);
		SaveLogAction saveLogAction = new SaveLogAction(fConsole);

		// contribute to toolbar
		IToolBarManager manager = fPage.getSite().getActionBars().getToolBarManager();
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, stopScriptAction);
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, closeConsoleAction);
		manager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, saveLogAction);

		//TODO if {@link ModifyParsingAction} is restored, it is to be used here,
		//in the same way stopScriptAction and saveLogAction are used.

		DebugUITools.getDebugContextManager().getContextService(fPage.getSite().getWorkbenchWindow()).addDebugContextListener(this);
	}

	@Override
	public void dispose() {
		DebugUITools.getDebugContextManager().getContextService(fPage.getSite().getWorkbenchWindow()).removeDebugContextListener(this);
		fConsole = null;
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

	/**
	 * @since 3.0
	 */
	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			if (fView != null && fConsole != null) {
				IProcess process = DebugUITools.getCurrentProcess();
				if (process instanceof SystemTapRuntimeProcessFactory.SystemTapRuntimeProcess
						&& ((SystemTapRuntimeProcessFactory.SystemTapRuntimeProcess) process)
								.matchesProcess(fConsole.getProcess())) {
					fView.display(fConsole);
				}
			}
		}
	}

}
