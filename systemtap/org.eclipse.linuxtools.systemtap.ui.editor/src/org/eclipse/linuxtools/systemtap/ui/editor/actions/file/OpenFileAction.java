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

package org.eclipse.linuxtools.systemtap.ui.editor.actions.file;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.internal.systemtap.ui.editor.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * An action used for opening Systemtap scripts from the filesystem.
 * @since 2.0
 */
public class OpenFileAction extends Action implements IWorkbenchWindowActionDelegate {

	private boolean successful;
	private IAction action;
	private boolean cancelled;

	/**
	 * @since 2.0
	 */
	protected IWorkbenchWindow window;

	public OpenFileAction() {
		super();
		setEnabled(true);
		successful = false;
	}

	@Override
	public void run(IAction act) {
		run();
	}

	/**
	 * Opens the editor input.
	 */
	@Override
	public void run() {
		successful = false;
		if (window == null) {
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		File file = queryFile();
		if (file != null) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
			IWorkbenchPage page= window.getActivePage();
			try {
				IDE.openEditorOnFileStore(page, fileStore);
				successful = true;
			} catch (PartInitException e) {
				//Pass
			}
		}
	}

	/**
	 * @return The style to use for the FileDialog when querying for a file.
	 * @since 2.2
	 */
	protected int dialogStyle() {
		return SWT.OPEN;
	}

	/**
	 * @return The name to give to the FileDialog when querying for a file.
	 * @since 2.2
	 */
	protected String dialogName() {
		return Localization.getString("OpenFileAction.OpenFile"); //$NON-NLS-1$
	}

	/**
	 * Request the name and location of the file to the user.
	 * @return the File object associated to the selected file.
	 */
	protected File queryFile() {
		FileDialog dialog= new FileDialog(window.getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[]{"*.stp"}); //$NON-NLS-1$
		dialog.setText(dialogName());
		String path= dialog.open();
		if (path != null && path.length() > 0) {
			cancelled = false;
			return new File(path);
		}
		cancelled = true;
		return null;
	}

	/**
	 * @return <code>true</code> if the last file query was cancelled,
	 * or <code>false</code> if a file was selected.
	 * @since 2.2
	 */
	public boolean wasCancelled() {
		return cancelled;
	}

	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * @since 2.0
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * @since 2.0
	 */
	@Override
	public void selectionChanged(IAction act, ISelection select) {
		action = act;
		action.setEnabled(true);
	}

	/**
	 * @since 2.0
	 */
	@Override
	public void dispose() {
		window = null;
		action = null;
	}
}
