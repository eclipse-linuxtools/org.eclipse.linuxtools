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

import org.eclipse.linuxtools.internal.systemtap.ui.editor.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;



public class NewFileAction extends OpenFileAction {
	
	
	/**
	 * Creates a new file.
	 * @return the new file object.
	 */
	@Override
	protected File queryFile() {
		FileDialog dialog= new FileDialog(window.getShell(), SWT.SAVE);
		dialog.setText(Localization.getString("NewFileAction.NewFile")); //$NON-NLS-1$
		String path= dialog.open();
		if (path != null && path.length() > 0)
			return new File(path);
		return null;
	}
}
