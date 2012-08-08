/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.ssh.proxy;

import org.eclipse.linuxtools.internal.ssh.proxy.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SSHPasswordDialog extends Dialog {
	private String password;
	private Text passwordField;
	public SSHPasswordDialog(Shell parent) {
		super(parent);
		this.getShell().setText(Messages.SSHPasswordDialog_Title);
	}

	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		
		Layout layout = comp.getLayout();
		if (!(layout instanceof GridLayout)) {
			layout = new GridLayout();
			comp.setLayout(layout);
		}
		((GridLayout)layout).numColumns = 2;

		Label passwordLabel = new Label(comp, SWT.RIGHT);
		passwordLabel.setText(Messages.SSHPasswordDialog_Password);

		passwordField = new Text(comp, SWT.SINGLE | SWT.PASSWORD);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		passwordField.setLayoutData(data);
		return comp;
	}


	protected void buttonPressed(int buttonId) {
		if (buttonId == Dialog.OK)
			this.password = passwordField.getText();
		super.buttonPressed(buttonId);
	}

	public String getPassword() {
		return password;
	}
}
