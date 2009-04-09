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

package org.eclipse.linuxtools.systemtapgui.editor.actions.file;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.systemtapgui.editor.PathEditorInput;
import org.eclipse.linuxtools.systemtapgui.editor.SimpleEditor;
import org.eclipse.linuxtools.systemtapgui.editor.actions.EditorAction;
import org.eclipse.linuxtools.systemtapgui.editor.internal.Localization;
import org.eclipse.linuxtools.systemtapgui.editor.internal.RecentFileLog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;



public class OpenFileAction extends EditorAction {
	public OpenFileAction() {
		super();
		setEnabled(true);
	}
	
	public void run(IAction act) {
		run();
	}
	
	/**
	 * Opens the editor input.
	 */
	public void run() {
		File file = queryFile();
		if (file != null) {
			IEditorInput input= createEditorInput(file);
			String editorId= getEditorId(file);
			IWorkbenchPage page= window.getActivePage();
			try {
				page.openEditor(input, editorId);
				RecentFileLog.updateRecentFiles(file);
			} catch (PartInitException e) {}
		} else if (file != null) {
			String msg = MessageFormat.format(Localization.getString("OpenFileAction.FileIsNull"), (Object [])(new String[] {file.getName()}));
			MessageDialog.openWarning(window.getShell(), Localization.getString("OpenFileAction.Problem"), msg);
		}
	}
	
	/**
	 * Request the name and location of the file to the user.
	 * @return the File object associated to the selected file.
	 */
	protected File queryFile() {
		FileDialog dialog= new FileDialog(window.getShell(), SWT.OPEN);
		dialog.setText(Localization.getString("OpenFileAction.OpenFile"));
		String path= dialog.open();
		if (path != null && path.length() > 0)
			return new File(path);
		return null;
	}

	/**
	 * Returns the editor ID associated with the specified file.
	 * @param file the file to examine
	 * @return the editor ID
	 */
	protected String getEditorId(File file) {
		IWorkbench workbench= window.getWorkbench();
		IEditorRegistry editorRegistry= workbench.getEditorRegistry();
		IEditorDescriptor descriptor= editorRegistry.getDefaultEditor(file.getName());
		if (descriptor != null && descriptor.getId().startsWith("org.eclipse.linuxtools.systemtapgui")) {	//TODO: descriptor.getId().startsWith("org.eclipse.linuxtools.systemtapgui") is a temparary fix until we find out why .txt files are opening with org.eclipse.ui.DefautTextEditor //$NON-NLS-1$
			return descriptor.getId();
		}
		return SimpleEditor.ID;
	}

	/**
	 * Creates an editor input.
	 * @param file the file you wish the editor to point at
	 * @return the input created
	 */
	protected IEditorInput createEditorInput(File file) {
		IPath location= new Path(file.getAbsolutePath());
		PathEditorInput input= new PathEditorInput(location);
		return input;
	}
}
