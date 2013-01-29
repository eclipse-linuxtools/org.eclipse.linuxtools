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
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;



/**
 * This <code>Action</code> is used to run a SystemTap script that is currently open in the editor.
 * @author Ryan Morse
 */

public class RunScriptAction extends RunScriptBaseAction {

	/**
	 * Returns the path of the current editor in the window this action is associated with.
	 * @return The string representation of the path of the current file.
	 */
	@Override
	protected String getFilePath() {
		IEditorPart ed = fWindow.getActivePage().getActiveEditor();
		if(ed.getEditorInput() instanceof PathEditorInput)
		 return ((PathEditorInput)ed.getEditorInput()).getPath().toString();
		else
	     return ResourceUtil.getFile(ed.getEditorInput()).getLocation().toString();

	}

	/**
	 * Checks if the current editor is operating on a file that actually exists and can be
	 * used as an argument to stap (as opposed to an unsaved buffer).
	 * @return True if the file is valid.
	 */
	@Override
	protected boolean isValid() {
		IEditorPart ed = fWindow.getActivePage().getActiveEditor();

		if(isValidFile(ed))
			if(isValidDirectory(getFilePath()))
				return true;
		return true;
	}

	private boolean isValidFile(IEditorPart ed) {
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