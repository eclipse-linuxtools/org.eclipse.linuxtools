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

package org.eclipse.linuxtools.systemtap.ui.structures.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.linuxtools.internal.systemtap.ui.structures.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



public class PasswordDialog extends Dialog {
	public PasswordDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Localization.getString("PasswordDialog.Password"));
		shell.setSize(275, 150);
	}
	
	/**
	 * Creates the dialog that requests SUDO password from the user.
	 * 
	 * @param parent The parent composite object.
	 * 
	 * @return The dialogue composite.
	 */
	 @Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);

		Label lblPassword = new Label(comp, SWT.RIGHT);
		lblPassword.setText(Localization.getString("PasswordDialog.SUDOPassword"));
		lblPassword.setBounds(5, 15, 110, 20);
		
		txtPassword = new Text(comp, SWT.SINGLE | SWT.PASSWORD);
		txtPassword.setBounds(115, 15, 150, 20);

		chkSavePassword = new Button(comp, SWT.CHECK);
		chkSavePassword.setText(Localization.getString("PasswordDialog.SavePassword"));
		chkSavePassword.setBackground(comp.getBackground());
		chkSavePassword.setBounds(5, 45, 200, 20);
		
		return comp;
	}
	
	 /**
      * Retrieves and stores the password.
	  * 
	  * @param buttonId ID of the button that the user clicks.
	  */
	@Override
	protected void buttonPressed(int buttonId) {
		if(0 == buttonId)
			password = txtPassword.getText();
		else
			password = null;
		
		passwordSaved = chkSavePassword.getSelection();
		
		super.buttonPressed(buttonId);
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean getPasswordSaved() {
		return passwordSaved;
	}
	
	public void dispose() {
		password = null;
		txtPassword.dispose();
	}

	private Text txtPassword;
	private String password;
	private Button chkSavePassword;
	private boolean passwordSaved;
}