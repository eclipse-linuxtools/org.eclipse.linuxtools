/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - modified to use in Docker UI
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.consoles;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

public class RunConsolePageParticipant implements IConsolePageParticipant {

	private RunConsole fConsole;
	private IPageBookViewPage fPage;
	private RunConsoleRemove fRemove;

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		fPage = page;
		fConsole = (RunConsole) console;

		fRemove = new RunConsoleRemove(fConsole);
		// TODO Auto-generated method stub

		// contribute to toolbar
		IActionBars actionBars = fPage.getSite().getActionBars();
		configureToolBar(actionBars.getToolBarManager());

	}

	/**
	 * Contribute actions to the toolbar
	 */
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fRemove);
	}

	@Override
	public void dispose() {
		if (fRemove != null) {
			fRemove.dispose();
			fRemove = null;
		}
	}

	@Override
	public void activated() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivated() {
		// TODO Auto-generated method stub

	}

}
