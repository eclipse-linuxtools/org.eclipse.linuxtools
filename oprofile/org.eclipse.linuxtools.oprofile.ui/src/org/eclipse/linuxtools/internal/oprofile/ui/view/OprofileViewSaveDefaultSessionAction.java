/*******************************************************************************
 * Copyright (c) 2004, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Keith Seitz <keiths@redhat.com> - SaveSessionValidator code
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import java.text.MessageFormat;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelError;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelRoot;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSession;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;

/**
 * Menu item to save the default session. Moved from a double-click in the view
 * on the default session for consistency (since non-default sessions can't be saved).
 */
public class OprofileViewSaveDefaultSessionAction extends Action {
	private IRemoteFileProxy proxy;

	public OprofileViewSaveDefaultSessionAction() {
		super(OprofileUiMessages.getString("view.actions.savedefaultsession.label")); //$NON-NLS-1$
	}

	@Override
	public void run() {
		boolean defaultSessionExists = false;
		String defaultSessionName = null;
		String eventName = null;
		UiModelRoot modelRoot = UiModelRoot.getDefault();

		if (modelRoot.hasChildren()) {
			IUiModelElement[] events = modelRoot.getChildren();
			for (IUiModelElement e : events) {
				if (e instanceof UiModelError)
					break;

				IUiModelElement[] sessions = e.getChildren();
				for (IUiModelElement s : sessions) {
					if (((UiModelSession)s).isDefaultSession()) {
						defaultSessionExists = true;
						defaultSessionName = s.getLabelText();
						eventName = s.getParent().getLabelText();
						break;
					}
				}
				if (defaultSessionExists)
					break;
			}
		}

		if (defaultSessionExists) {
			//the following code was originially written by Keith Seitz
			InputDialog dialog = new InputDialog(OprofileUiPlugin.getActiveWorkbenchShell(),
					OprofileUiMessages.getString("savedialog.title"),    //$NON-NLS-1$
					OprofileUiMessages.getString("savedialog.message"),    //$NON-NLS-1$
					OprofileUiMessages.getString("savedialog.initial"),   //$NON-NLS-1$
					new SaveSessionValidator());

			int result = dialog.open();
			if (result == Window.OK) {
				try {
					OprofileCorePlugin.getDefault().getOpcontrolProvider().saveSession(dialog.getValue());
					// remove the default session
					OprofileCorePlugin.getDefault().getOpcontrolProvider().deleteSession(defaultSessionName, eventName);
					// clear out collected data by this session
					// if opcontol is used
					if (!Oprofile.OprofileProject.OPERF_BINARY
							.equals(Oprofile.OprofileProject
									.getProfilingBinary())) {
						OprofileCorePlugin.getDefault().getOpcontrolProvider()
								.reset();
					}
					else
					{
						// remove oprofile_data so current event no longer be there
						OprofileViewDeleteSessionAction
								.deleteOperfDataFolder(Oprofile.OprofileProject
										.getProject()
										.getFolder(
												Oprofile.OprofileProject.OPERF_DATA));
					}
					OprofileUiPlugin.getDefault().getOprofileView().refreshView();
				} catch (OpcontrolException oe) {
					OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
				}
			}
		} else {
			MessageDialog.openError(OprofileUiPlugin.getActiveWorkbenchShell(),
					OprofileUiMessages.getString("defaultsessiondialog.nodefaultsession.title"),  //$NON-NLS-1$
					OprofileUiMessages.getString("defaultsessiondialog.nodefaultsession.message")); //$NON-NLS-1$
		}
	}

	//Original author: Keith Seitz <keiths@redhat.com>
	private class SaveSessionValidator implements IInputValidator {
		public String isValid(String newText) {
			// Sanity check
			if (newText.length() == 0) {
				return ""; //$NON-NLS-1$
			}

			// Cannot contain invalid characters
			int index = newText.indexOf('/');
			if (index == -1) {
				index = newText.indexOf('\\');
			}

			if (index != -1) {
				String format = OprofileUiMessages.getString("savedialog.validator.invalidChar"); //$NON-NLS-1$
				Object[] fmtArgs = new Object[] { newText.substring(index, index + 1), newText };
				return MessageFormat.format(format, fmtArgs);
			}

			// Cannot contain whitespace
			if (newText.contains(" ") || newText.contains("\t")) { //$NON-NLS-1$ //$NON-NLS-2$
				String format = OprofileUiMessages.getString("savedialog.validator.containsWhitespace"); //$NON-NLS-1$
				Object[] fmtArgs = new Object[] { newText };
				return MessageFormat.format(format, fmtArgs);
			}

			// Must not already exist (opcontrol doesn't allow it)

			try {
				proxy = RemoteProxyManager.getInstance().getFileProxy(Oprofile.OprofileProject.getProject());
			} catch (CoreException e) {
				e.printStackTrace();
			}

			IFileStore fileStore = proxy.getResource(Oprofile.getDefaultSamplesDirectory() + newText);
			if (fileStore.fetchInfo().exists()) {
				String format = OprofileUiMessages.getString("savedialog.validator.exists"); //$NON-NLS-1$
				Object[] fmtArgs = new Object[] { newText };
				return MessageFormat.format(format, fmtArgs);
			}

			// Everything OK
			return null;
		}
	};


}
