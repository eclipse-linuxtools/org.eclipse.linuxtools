/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for RPM UI Editor related things.
 *
 */
public class RPMUtils {

	/**
	 * Utility classes should not have a public or default constructor.
	 */
	private RPMUtils() {}

	/**
	 * Show an error dialog.
	 *
	 * @param shell A valid shell
	 * @param title The error dialog title
	 * @param message The message to be displayed.
	 */
	public static void showErrorDialog(final Shell shell,
			final String title, final String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, title, message);
			}
		});
	}
}
