/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.handlers;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.widgets.Display;

/**
 * Class for handling general tasks handled by session saving commands:
 * File name creation and validation, command enablement, data file verification.
 */
public abstract class AbstractSaveDataHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		InputDialog dialog = new InputDialog(Display.getCurrent()
				.getActiveShell(), Messages.PerfSaveSession_title,
				Messages.PerfSaveSession_msg, "", new IInputValidator() {

					@Override
					public String isValid(String newText) {
						if ("".equals(newText)) {
							return Messages.PerfSaveSession_invalid_filename_msg;
						}
						return null;
					}
				});

		if (dialog.open() == Window.OK) {
			saveData(dialog.getValue());
		}

		return null;
	}

	@Override
	public boolean isEnabled() {
		IPath curWorkingDirectory = getWorkingDir();
		return curWorkingDirectory != null && !curWorkingDirectory.isEmpty()
				&& verifyData();
	}

	/**
	 * Get current working directory.
	 * @return current working directory.
	 */
	protected IPath getWorkingDir() {
		return PerfPlugin.getDefault().getWorkingDir();
	}

	/**
	 * New data location based on specified name, which the specified
	 * extension will be appended to.
	 *
	 * @param filename
	 * @param extension
	 * @return
	 */
	public IPath getNewDataLocation(String filename, String extension) {
		IPath newFilename  = getWorkingDir().append(filename);
		return newFilename.addFileExtension(extension);

	}

	/**
	 * Verify that we can save the specified file.
	 *
	 * @param file <code>File</code> to save
	 * @return true if we can go ahead and save the file, false otherwise
	 */
	public boolean canSave(File file) {
		if (file.exists()) {
			String msg = MessageFormat.format(
					Messages.PerfSaveSession_file_exists_msg,
					new Object[] { file.getName() });
			return MessageDialog.openQuestion(Display.getCurrent()
					.getActiveShell(),
					Messages.PerfSaveSession_file_exists_title, msg);
		}
		return true;
	}

	/**
	 * Open error dialog informing user of saving failure.
	 * @param filename
	 */
	public void openErroDialog(String title, String pattern, String arg) {
		String errorMsg = MessageFormat.format(pattern, new Object[] { arg });
		MessageDialog.openError(Display.getCurrent().getActiveShell(), title,
				errorMsg);
	}

	/**
	 * Close specified resource
	 *
	 * @param resrc resource to close
	 * @param resrcName resource name
	 */
	public void closeResource(Closeable resrc, String resrcName) {
		if (resrc != null) {
			try {
				resrc.close();
			} catch (IOException e) {
				openErroDialog(Messages.PerfResourceLeak_title,
						Messages.PerfResourceLeak_msg, resrcName);
			}
		}
	}

	/**
	 * Save data to file with specified name and return handle
	 *
	 * @param filename the file name
	 */
	public abstract File saveData(String filename);

	/**
	 * Verify data to save.
	 *
	 * @return true if data is valid
	 */
	public abstract boolean verifyData();

	@Override
	public boolean isHandled() {
		return isEnabled();
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	@Override
	public void dispose() {

	}
}
