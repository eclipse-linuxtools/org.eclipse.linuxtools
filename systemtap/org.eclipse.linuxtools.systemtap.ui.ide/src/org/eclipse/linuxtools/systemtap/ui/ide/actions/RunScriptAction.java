/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Briggs, Henry Hughes, Ryan Morse, Roland Grunberg, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.ide.actions;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.SystemTapScriptTester;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;



/**
 * This <code>Action</code> is used to run a SystemTap script that is currently open in the editor.
 * @author Ryan Morse
 */

public class RunScriptAction extends RunScriptBaseAction {

	/**
	 * Checks if the current editor is operating on a file that actually exists and can be
	 * used as an argument to stap (as opposed to an unsaved buffer).
	 * @return True if the file is valid.
	 */
	@Override
	protected boolean isValid() {
		// If the path is not set this action will run the script from
		// the active editor
		if (this.path == null){
			IEditorPart ed = fWindow.getActivePage().getActiveEditor();
			if(!isValidEditor(ed)){
				return false;
			}
		}

		return this.getFilePath().endsWith(SystemTapScriptTester.STP_SUFFIX)
				&& isValidDirectory(this.getFilePath());
	}

	private boolean isValidEditor(IEditorPart ed) {
		if(null == ed) {
			String msg = MessageFormat.format(Localization.getString("RunScriptAction.NoScriptFile"),(Object[]) null); //$NON-NLS-1$
			MessageDialog.openWarning(fWindow.getShell(), Localization.getString("RunScriptAction.Problem"), msg); //$NON-NLS-1$
			return false;
		}

		if(ed.isDirty())
			ed.doSave(new ProgressMonitorPart(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new FillLayout()));

		return true;
	}

}