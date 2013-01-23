/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P j
 *     Roland Grunberg
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;



/**
 * A basic core class that provides common methods that are needed by any
 * action that makes use of the Console.
 * @author Ryan Morse
 */
public abstract class ConsoleAction extends Action implements IWorkbenchWindowActionDelegate, IViewActionDelegate {

	public void init(IWorkbenchWindow window) {}
	public void init(IViewPart part) {}

	public void run(IAction action) {
		run();
	}

	@Override
	public abstract void run();

	public void selectionChanged(IAction action, ISelection selection) {

	}

	/**
	 * Disposes of all internal references held by this class.  No method should be called after
	 * calling this.
	 */
	public void dispose() {

	}

}
