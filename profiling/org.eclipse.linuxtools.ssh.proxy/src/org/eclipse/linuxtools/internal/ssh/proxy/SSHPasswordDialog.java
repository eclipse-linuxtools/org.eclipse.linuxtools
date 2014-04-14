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

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SSHPasswordDialog extends Dialog {
    private String password;
    private Text passwordField;
    private String user, host;
    public SSHPasswordDialog(Shell parent, String user, String host) {
        super(parent);
        this.user = user;
        this.host = host;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.SSHPasswordDialog_Title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite comp = (Composite) super.createDialogArea(parent);

        Layout layout = comp.getLayout();
        if (!(layout instanceof GridLayout)) {
            layout = new GridLayout();
            comp.setLayout(layout);
        }
        ((GridLayout)layout).numColumns = 2;

        Label passwordTitle= new Label(comp, SWT.RIGHT);
        Label passwordLabel = new Label(comp, SWT.RIGHT);
        GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = GridData.FILL;
        passwordTitle.setLayoutData(gridData);
        if (host != null && user != null) {
            passwordTitle.setText(MessageFormat.format(Messages.SSHPasswordDialog_Password_Title, user, host));
        }

        passwordLabel.setText(Messages.SSHPasswordDialog_Password);

        passwordField = new Text(comp, SWT.SINGLE | SWT.PASSWORD);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        passwordField.setLayoutData(data);
        return comp;
    }


    @Override
    protected void okPressed() {
        this.password = passwordField.getText();
        super.okPressed();
    }

    public String getPassword() {
        return password;
    }
}
