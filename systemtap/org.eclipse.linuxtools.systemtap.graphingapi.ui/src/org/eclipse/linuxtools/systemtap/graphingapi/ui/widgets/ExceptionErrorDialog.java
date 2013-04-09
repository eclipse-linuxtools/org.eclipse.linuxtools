/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.ui.PlatformUI;

/**
 * A convenience class for showing error dialogs which display the full stack
 * trace in the details section.
 * @since 2.0
 *
 */
public class ExceptionErrorDialog {

	public static int openError(String message, Exception e){
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		Status status = new Status(IStatus.ERROR, GraphingAPIUIPlugin.PLUGIN_ID, e.toString(), new Throwable(writer.toString()));
		return ErrorDialog.openError(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), message,
				message, status);
	}

}
