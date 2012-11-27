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

package org.eclipse.linuxtools.systemtap.ui.structures;

import org.eclipse.linuxtools.systemtap.ui.structures.ui.PasswordDialog;
import org.eclipse.ui.PlatformUI;



public class PasswordPrompt implements IPasswordPrompt {
	public PasswordPrompt() {
		this(null);
	}

	public PasswordPrompt(String pass) {
		password = pass;
		triedSaved = false;
	}

	/**
	 * Prompts the user for their password.
	 *
	 * @return The string response of the user.
	 */
	public String getPassword() {
		if(triedSaved || null == password) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					PasswordDialog input = new PasswordDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					input.open();
					password = input.getPassword();
					save = input.getPasswordSaved();
					input.dispose();
					if(null == password) password = ""; //$NON-NLS-1$
				}
			});
		}

		triedSaved = true;
		return password;
	}

	public boolean getSavePassword() {
		return save;
	}

	private boolean save;
	private String password;
	private boolean triedSaved;
}
