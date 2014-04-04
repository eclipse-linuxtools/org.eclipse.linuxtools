/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - original Action implementation and API
 *     Red Hat - migration to Handler implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.editor.handlers.file;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.editor.EditorPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.editor.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * A handler used for opening SystemTap scripts from the filesystem.
 * @since 3.0
 */
public class OpenFileHandler extends AbstractHandler {

    private boolean successful;
    private boolean cancelled;
    private IWorkbenchWindow window = null;

    public OpenFileHandler() {
    }

    /**
     * Opens the provided file's editor input, if it is a valid Systemtap file.
     */
    public void executeOnFile(File file) {
        if (window == null) {
            window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        }
        if (file != null && file.exists() && file.getName().endsWith(".stp")) { //$NON-NLS-1$
            runActions(file);
        }
    }

    /**
     * Queries the user for the SystemTap file to open.
     * @param event If execution is handled by a plugin extension, this will contain
     * an event containing all the information about the current state of the application.
     * To execute this code elsewhere, it is sufficient to pass <code>null</code> here.
     *
     */
    @Override
    public Object execute(ExecutionEvent event) {
        if (window == null) {
            window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        }
        File file = queryFile();
        if (file != null) {
            runActions(file);
        }
        return null;
    }

    private void runActions(File file) {
        successful = false;
        IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
        IWorkbenchPage page = window.getActivePage();
        try {
            IDE.openEditorOnFileStore(page, fileStore);
            successful = true;
        } catch (PartInitException e) {
            ErrorDialog.openError(window.getShell(),
                    Localization.getString("OpenFileHandler.Problem"), //$NON-NLS-1$
                    Localization.getString("OpenFileHandler.ProblemMessage"), //$NON-NLS-1$
                    new Status(IStatus.ERROR, EditorPlugin.ID, e.getMessage(), e));
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
        return Localization.getString("OpenFileHandler.OpenFile"); //$NON-NLS-1$
    }

    /**
     * Request the name and location of the file to the user.
     * @return the File object associated to the selected file.
     */
    protected File queryFile() {
        FileDialog dialog = new FileDialog(window.getShell(), dialogStyle());
        dialog.setFilterExtensions(new String[]{"*.stp"}); //$NON-NLS-1$
        dialog.setText(dialogName());
        String path = dialog.open();
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

}
