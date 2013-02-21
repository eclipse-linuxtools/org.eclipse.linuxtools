/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.structures.StructuresPlugin;
import org.eclipse.ui.PlatformUI;

/**
 * A convenience class for showing error dialogs which display the full stack
 * trace in the details section.
 * @since 2.0
 *
 */
public class ExceptionErrorDialog {

	public static int openError(String title, Exception e){
		Status status = new Status(IStatus.ERROR, StructuresPlugin.PLUGIN_ID, title, e);
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		return ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, writer.toString(), status);
	}

}
