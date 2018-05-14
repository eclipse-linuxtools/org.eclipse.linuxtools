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

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.rpm.ui.RpmConsole.RpmConsoleObserver;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

/**
 * A console toolbar button for closing the console when it is inactive.
 */
public class CloseConsoleAction extends Action implements RpmConsoleObserver {

	RpmConsole fConsole;

	/**
	 * Creates a new stop button for the given console.
	 * 
	 * @param console The console that this button will control.
	 */
	public CloseConsoleAction(RpmConsole console) {
		fConsole = console;
		setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE));
		setToolTipText(Messages.getString("RPMConsoleAction.Close")); //$NON-NLS-1$
		fConsole.addConsoleObserver(this);
	}

	/**
	 * Closes the console.
	 */
	@Override
	public void run() {
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			if (fConsole != null) {
				ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { fConsole });
			}
		});
	}

	@Override
	public void runningStateChanged(boolean running) {
		setEnabled(!running);
	}

}
