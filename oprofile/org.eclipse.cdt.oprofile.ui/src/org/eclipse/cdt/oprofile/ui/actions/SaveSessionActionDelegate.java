/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.ui.actions;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.cdt.oprofile.core.OpcontrolException;
import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.cdt.oprofile.core.OprofileCorePlugin;
import org.eclipse.cdt.oprofile.core.SessionEvent;
import org.eclipse.cdt.oprofile.ui.OprofilePlugin;
import org.eclipse.cdt.oprofile.ui.OprofileUIMessages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

/**
 * ActionDelegate for saving the default session
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class SaveSessionActionDelegate extends AbstractOprofileUiAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		
		// First things first, check if we have a default session to save
		boolean haveDefault = false;
		SessionEvent[] sessions = Oprofile.getSessionEvents();
		for (int i = 0; i < sessions.length && !haveDefault; ++i) {
			for (int j = 0; j < sessions[i].sessions.length; ++j) {
				if (sessions[i].sessions[j].isDefaultSession()) {
					haveDefault = true;
					break;
				}
			}
		}
		
		if (haveDefault) {
			// These seem a little stringent. I just borrowed them from the rules for making Projects.
			IInputValidator validator = new IInputValidator() {
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
						String format = OprofileUIMessages.getString("savedialog.validator.invalidChar"); //$NON-NLS-1$
						Object[] fmtArgs = new Object[] { newText.substring(index, index + 1), newText };
						return MessageFormat.format(format, fmtArgs);
					}
						
					// Cannot end with period -- Is this such a big deal?
					if (newText.endsWith(".")) {
						String format = OprofileUIMessages.getString("savedialog.validator.endsWithPeriod"); //$NON-NLS-1$
						Object[] fmtArgs = new Object[] { newText };
						return MessageFormat.format(format, fmtArgs);
					}
					
					// Cannot start or end with whitespace
					if (newText.startsWith(" ") || newText.endsWith(" ")
						|| newText.startsWith("\t") || newText.endsWith("\t")) {
						String format = OprofileUIMessages.getString("savedialog.validator.StartsEndsWithWhitespace"); //$NON-NLS-1$
						Object[] fmtArgs = new Object[] { newText };
						return MessageFormat.format(format, fmtArgs);
					}
					
					// Must not already exist (opcontrol doesn't allow it)
					File file = new File(Oprofile.getDefaultSamplesDirectory(), newText);
					if (file.exists()) {
						String format = OprofileUIMessages.getString("savedialog.validator.exists"); //$NON-NLS-1$
						Object[] fmtArgs = new Object[] { newText };
						return MessageFormat.format(format, fmtArgs);
					}
	
					// Everything OK
					return null;
				}
			};
			
			InputDialog dialog = new InputDialog(OprofilePlugin.getActiveWorkbenchShell(),
					OprofileUIMessages.getString("savedialog.title"),   // $NON-NLS-1$
					OprofileUIMessages.getString("savedialog.message"),   // $NON-NLS-1$
					OprofileUIMessages.getString("savedialog.initial"),  // $NON-NLS-1$
					validator);
			int result = dialog.open();
			if (result == Window.OK) {
				try {
					OprofileCorePlugin.getDefault().getOpcontrolProvider().saveSession(dialog.getValue());
					_updateViews();
				} catch (OpcontrolException oe) {
					_showErrorDialog(null, "opcontrolProvider", oe); //$NON-NLS-1$
				}
			}
		}
	}
}
