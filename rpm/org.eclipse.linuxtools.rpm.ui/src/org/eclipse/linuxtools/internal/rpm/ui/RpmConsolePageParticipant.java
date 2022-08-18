/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * This class is responsible for creating and initializing UI for a
 * {@link RpmConsole}.
 */
public class RpmConsolePageParticipant implements IConsolePageParticipant {

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		if (!(console instanceof RpmConsole rpmCon)) {
			return;
		}
		IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();

		StopBuildAction stopBuildAction = new StopBuildAction(rpmCon);
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, stopBuildAction);

		CloseConsoleAction closeConsoleAction = new CloseConsoleAction(rpmCon);
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, closeConsoleAction);
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
