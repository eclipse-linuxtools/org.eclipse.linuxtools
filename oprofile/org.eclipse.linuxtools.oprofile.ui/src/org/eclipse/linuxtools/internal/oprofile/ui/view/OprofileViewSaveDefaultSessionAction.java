/*******************************************************************************
 * Copyright (c) 2004, 2017 Red Hat, Inc.
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
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.sessions.SessionManager;
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
 * on the default session for consistency (since non-default sessions can't be
 * saved).
 */
public class OprofileViewSaveDefaultSessionAction extends Action {
    private IRemoteFileProxy proxy;

    public OprofileViewSaveDefaultSessionAction() {
        super(OprofileUiMessages
                .getString("view.actions.savedefaultsession.label")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        boolean defaultSessionExists = false;
        UiModelRoot modelRoot = UiModelRoot.getDefault();
        String defaultSessionName = null;
        IUiModelElement[] modelEvents = null;

        if (modelRoot.hasChildren()) {
            IUiModelElement[] sessions = modelRoot.getChildren();
            for (IUiModelElement e : sessions) {
                if (e instanceof UiModelError)
                    break;

                if (e instanceof UiModelSession) {

                    if (((UiModelSession) e).isDefaultSession()) {
                        defaultSessionExists = true;
                        defaultSessionName = e.getLabelText();
                        modelEvents = ((UiModelSession) e).getChildren();
                        break;
                    }

                    if (defaultSessionExists)
                        break;
                }
            }

            if (defaultSessionExists) {
                // the following code was originially written by Keith Seitz
                InputDialog dialog = new InputDialog(
                        OprofileUiPlugin.getActiveWorkbenchShell(),
                        OprofileUiMessages.getString("savedialog.title"), //$NON-NLS-1$
                        OprofileUiMessages.getString("savedialog.message"), //$NON-NLS-1$
                        OprofileUiMessages.getString("savedialog.initial"), //$NON-NLS-1$
                        new SaveSessionValidator());

                int result = dialog.open();
				if (result == Window.OK) {
					SessionManager.saveSession(dialog.getValue());

					// remove the default session
                    for (int i = 0; i < modelEvents.length; i++) {
                        SessionManager.deleteSession(defaultSessionName, modelEvents[i].getLabelText());
                    }

					if (Oprofile.OprofileProject.OPERF_BINARY.equals(Oprofile.OprofileProject.getProfilingBinary())) {
						// remove oprofile_data so current event no longer
						// be there
						OprofileViewDeleteSessionAction.deleteOperfDataFolder(
								Oprofile.OprofileProject.getProject().getFolder(Oprofile.OprofileProject.OPERF_DATA));
					}
					OprofileUiPlugin.getDefault().getOprofileView().refreshView();
				}
            } else {
                MessageDialog
                        .openError(
                                OprofileUiPlugin.getActiveWorkbenchShell(),
                                OprofileUiMessages
                                        .getString("defaultsessiondialog.nodefaultsession.title"), //$NON-NLS-1$
                                OprofileUiMessages
                                        .getString("defaultsessiondialog.nodefaultsession.message")); //$NON-NLS-1$
            }
        }
    }

    // Original author: Keith Seitz <keiths@redhat.com>
    private class SaveSessionValidator implements IInputValidator {

        private SessionManager session = null;

        public SaveSessionValidator() {
            session = new SessionManager(SessionManager.SESSION_LOCATION);
        }

        @Override
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
                String format = OprofileUiMessages
                        .getString("savedialog.validator.invalidChar"); //$NON-NLS-1$
				return MessageFormat.format(format, newText.substring(index, index + 1), newText);
            }

            // Cannot contain whitespace
            if (newText.contains(" ") || newText.contains("\t")) { //$NON-NLS-1$ //$NON-NLS-2$
                String format = OprofileUiMessages
                        .getString("savedialog.validator.containsWhitespace"); //$NON-NLS-1$
                return MessageFormat.format(format, newText);
            }

            if (session.existsSession(newText)) {
                String format = OprofileUiMessages
                        .getString("savedialog.validator.exists"); //$NON-NLS-1$
                return MessageFormat.format(format, newText);
            }

            // Must not already exist (opcontrol doesn't allow it)

            try {
                proxy = RemoteProxyManager.getInstance().getFileProxy(
                        Oprofile.OprofileProject.getProject());
            } catch (CoreException e) {
                e.printStackTrace();
            }

            IFileStore fileStore = proxy.getResource(Oprofile
                    .getDefaultSamplesDirectory() + newText);
            if (fileStore.fetchInfo().exists()) {
                String format = OprofileUiMessages
                        .getString("savedialog.validator.exists"); //$NON-NLS-1$
                return MessageFormat.format(format, newText);
            }

            // Everything OK
            return null;
        }
    }

}
