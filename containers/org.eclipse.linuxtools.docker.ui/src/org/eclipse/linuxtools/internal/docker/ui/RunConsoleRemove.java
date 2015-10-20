/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - modified to use in Docker UI
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;

public class RunConsoleRemove extends Action
		implements IViewActionDelegate, IConsoleListener {

	private RunConsole console;

	public RunConsoleRemove(RunConsole console) {
		super(ConsoleMessages.getString("RunConsoleRemove.action")); //$NON-NLS-1$
		this.console = console;
		setToolTipText(ConsoleMessages.getString("RunConsoleRemove.tooltip")); //$NON-NLS-1$
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
		// IDebugHelpContextIds.CONSOLE_REMOVE_LAUNCH);
		setImageDescriptor(SWTImagesFactory.DESC_REMOVE);
		ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(this);
	}

	public void dispose() {
		ConsolePlugin.getDefault().getConsoleManager()
				.removeConsoleListener(this);
	}

	@Override
	public void run() {
		RunConsole.removeConsole(console);
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// nothing as of yet

	}

	@Override
	public void consolesAdded(IConsole[] consoles) {
		// nothing as of yet

	}

	@Override
	public void consolesRemoved(IConsole[] consoles) {
		// nothing as of yet

	}

	@Override
	public void init(IViewPart view) {
		// nothing as of yet

	}

}
