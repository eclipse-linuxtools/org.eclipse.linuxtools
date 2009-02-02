/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.rpmlint.actions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.rpm.rpmlint.RpmlintLog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class RunRpmlintAction implements IObjectActionDelegate {
	private ISelection selection;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
	}

	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			for (Object element : ((IStructuredSelection) selection).toList()) {
				IFile rpmFile = null;
				if (element instanceof IFile) {
					rpmFile = (IFile) element;
				} else if (element instanceof IAdaptable) {
					rpmFile = (IFile) ((IAdaptable) element)
							.getAdapter(IFile.class);
				}
				if (rpmFile != null) {
					try {
						Process child = new ProcessBuilder(Activator
								.getRpmlintPath(), "-i", rpmFile.getLocation() //$NON-NLS-1$
								.toString()).start();
						BufferedInputStream in = new BufferedInputStream(child
								.getInputStream());
						BufferedReader is = new BufferedReader(
								new InputStreamReader(in));
						String line;
						MessageConsole myConsole = findConsole(Messages.RunRpmlintAction_0);
						MessageConsoleStream out = myConsole.newMessageStream();
						myConsole.clearConsole();
						myConsole.activate();
						while ((line = is.readLine()) != null) {
							out.println(line);
						}

					} catch (IOException e) {
						// FIXME: rpmlint is not installed in the default place
						// -> ask user to open the prefs page.
						RpmlintLog.logError(e);
					}
				}
			}
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
