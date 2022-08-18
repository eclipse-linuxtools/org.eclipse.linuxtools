/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
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
package org.eclipse.linuxtools.internal.rpm.rpmlint.actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
 * Manually invoke rpmlint action, which prints the output of rpmlint execution
 * to the console.
 */
public class RunRpmlintAction extends AbstractHandler {
	/**
	 * @param event The execution event.
	 * @return Nothing.
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection structured) {
			for (Object element : structured.toList()) {
				IFile rpmFile = null;
				if (element instanceof IFile file) {
					rpmFile = file;
				} else if (element instanceof IAdaptable a) {
					rpmFile = a.getAdapter(IFile.class);
				}
				if (rpmFile != null) {
					runRpmlint(rpmFile.getLocation().toString());
				}
			}
		} else {
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			if (editor != null) {
				IEditorInput editorInput = editor.getEditorInput();
				if (editorInput instanceof IFileEditorInput fei) {
					runRpmlint(fei.getFile().getLocation().toString());
				} else if (editorInput instanceof IURIEditorInput uei) {
					runRpmlint(uei.getURI().getPath());
				}
			}
		}
		return null;

	}

	private static void runRpmlint(String location) {
		String rpmlintPath = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID)
				.getString(PreferenceConstants.P_RPMLINT_PATH);
		try {
			if (Files.exists(Paths.get(rpmlintPath))) {
				String output = Utils.runCommandToString(rpmlintPath, "-i", location); //$NON-NLS-1$
				MessageConsole myConsole = findConsole(Messages.RunRpmlintAction_0);
				MessageConsoleStream out = myConsole.newMessageStream();
				myConsole.clearConsole();
				myConsole.activate();
				out.println(output);
			} else {
				IStatus warning = Status.warning(Messages.RunRpmlintAction_1, null);
				ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Messages.RunRpmlintAction_2, null, warning);
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
		for (IConsole element : existing) {
			if (name.equals(element.getName())) {
				return (MessageConsole) element;
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
}
