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
package org.eclipse.linuxtools.internal.perf.ui;

import java.io.File;
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

public class PerfSaveSessionHandler implements IHandler {

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
			String fileName = dialog.getValue();

			// get paths
			IPath curWorkingDirectory = PerfPlugin.getDefault().getWorkingDir();
			IPath newDataLoc = curWorkingDirectory.append(fileName).addFileExtension("data"); //$NON-NLS-1$
			IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();

			// get files
			File newDataFile = newDataLoc.toFile();
			File defaultDataFile = defaultDataLoc.toFile();

			// rename default perf data file
			if (defaultDataFile.renameTo(newDataFile)) {
				PerfPlugin.getDefault().setPerfProfileData(newDataLoc);
				PerfPlugin.getDefault().getProfileView().setContentDescription(newDataLoc.toOSString());
			} else{
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						Messages.PerfSaveSession_no_data_found_title,
						Messages.PerfSaveSession_no_data_found_msg);
			}
		}

		return null;
	}

	@Override
	public boolean isEnabled() {
		IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();
		IPath curWorkingDirectory = PerfPlugin.getDefault().getWorkingDir();
		if (defaultDataLoc == null || curWorkingDirectory == null
				|| defaultDataLoc.isEmpty() || curWorkingDirectory.isEmpty()) {
			return false;
		}
		return true;
	}

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
