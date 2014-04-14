/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * Convenience class for opening an error dialog from a non-UI job.
 *
 */
public class SystemTapUIErrorMessages extends UIJob {
	private String title, message;
	private static boolean active = true;

	public SystemTapUIErrorMessages(String name, String title, String message) {
		super(name);
		this.title = title;
		this.message = message;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (!active) {
			return Status.CANCEL_STATUS;
		}
		//Test that this job is running in the UI thread
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (window == null) {
			return Status.CANCEL_STATUS; //Something is wrong!
		}

		Shell sh = new Shell();

		MessageDialog.openError(sh, title, message);
		return Status.OK_STATUS;
	}


	public static void setActive(boolean val) {
		active = val;
	}
}
