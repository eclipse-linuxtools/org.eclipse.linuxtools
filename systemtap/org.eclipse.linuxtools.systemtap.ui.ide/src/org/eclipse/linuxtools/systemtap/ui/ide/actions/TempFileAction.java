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

package org.eclipse.linuxtools.systemtap.ui.ide.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.linuxtools.systemtap.ui.ide.IDEPerspective;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;



/**
 * This <code>Action</code> creates an editor on a temporary file.
 * @author Henry Hughes
 */
public class TempFileAction extends Action {
	public void run() {
		LogManager.logDebug("Start run:", this);
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		IWorkbenchWindow main = null;
		for(int i = 0; i < windows.length; i++) {
			String s = windows[i].getActivePage().getPerspective().getId();
			if(s.equals(IDEPerspective.ID))
				main = windows[i];
		}
		if(main == null)
			return;
		try {
			PathEditorInput p = new PathEditorInput();
			main.getActivePage().openEditor(p, STPEditor.ID);
		} catch (PartInitException e) {
			LogManager.logDebug("PartInitException run: " + e.getMessage(), this);
		} catch(IOException e) {
			LogManager.logCritical("IOException run: " + e.getMessage(), this);
		}
		LogManager.logDebug("End run:", this);
	}
	
}
