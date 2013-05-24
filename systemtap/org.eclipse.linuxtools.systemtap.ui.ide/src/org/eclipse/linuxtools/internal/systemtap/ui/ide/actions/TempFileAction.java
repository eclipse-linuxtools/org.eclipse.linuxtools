/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPerspective;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;



/**
 * This <code>Action</code> creates an editor on a temporary file.
 * @author Henry Hughes
 */
public class TempFileAction extends Action {
	@Override
	public void run() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		IWorkbenchWindow main = null;
		for(int i = 0; i < windows.length; i++) {
			String s = windows[i].getActivePage().getPerspective().getId();
			if(s.equals(IDEPerspective.ID))
				main = windows[i];
		}
		if(main == null){
			return;
		}

		try {
			PathEditorInput p = new PathEditorInput();
			main.getActivePage().openEditor(p, STPEditor.ID);
		} catch (PartInitException e) {
			ExceptionErrorDialog.openError(Messages.TempFileAction_errorDialogTitle, e);
		} catch(IOException e) {
			ExceptionErrorDialog.openError(Messages.TempFileAction_errorDialogTitle, e);
		}
	}
}
