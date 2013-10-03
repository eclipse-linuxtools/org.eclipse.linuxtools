/*******************************************************************************
 * Copyright (c) 2009, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.actions;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.internal.rpm.rpmlint.RpmlintLog;
import org.eclipse.linuxtools.internal.rpm.rpmlint.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Manually invoke rpmlint action, which prints the output of rpmlint execution to the console.
 *
 */
public class RunRpmlintAction extends AbstractHandler{
	/**
	 * @param event The execution event.
	 * @return Nothing.
	 */
	@Override
	public Object execute(ExecutionEvent event)  {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
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
					runRpmlint(rpmFile.getLocation().toString());
				}
			}
		} else {
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			if (editor != null) {
				IEditorInput editorInput = editor.getEditorInput();
				if (editorInput instanceof IFileEditorInput) {
					runRpmlint(((IFileEditorInput) editorInput).getFile().getLocation().toString());
				} else if (editorInput instanceof IURIEditorInput) {
					runRpmlint(((IURIEditorInput) editorInput).getURI().getPath());
				}
			}
		}
		return null;

	}

	private static void runRpmlint(String location) {
		String rpmlintPath = new ScopedPreferenceStore(InstanceScope.INSTANCE,Activator.PLUGIN_ID).getString(
				PreferenceConstants.P_RPMLINT_PATH);
		try {
			if (Utils.fileExist(rpmlintPath)) {
				String output = Utils.runCommandToString(rpmlintPath,
						"-i", location); //$NON-NLS-1$
				MessageConsole myConsole = findConsole(Messages.RunRpmlintAction_0);
				MessageConsoleStream out = myConsole
						.newMessageStream();
				myConsole.clearConsole();
				myConsole.activate();
				out.println(output);
			} else {
				IStatus warning = new Status(
						IStatus.WARNING,
						Activator.PLUGIN_ID,
						1,
						Messages.RunRpmlintAction_1,
						null);
				ErrorDialog.openError(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						Messages.RunRpmlintAction_2,
						null, warning);
			}
		} catch (IOException e) {
			// FIXME: rpmlint is not installed in the default place
			// -> ask user to open the prefs page.
			RpmlintLog.logError(e);
		}
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
}
