/********************************************************************************
 * Copyright (c) 2008-2010 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.linuxtools.sequoyah.device.tools.ITool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Otavio Ferranti
 */
public class DialogLogin extends TitleAreaDialog {

	final private String WINDOW_TITLE = Messages.LoginDialog_Window_Title;
	final private String WINDOW_MESSAGE = Messages.LoginDialog_Window_Message;
	final private String WINDOW_MESSAGE_LOGIN_INVALID =
			Messages.LoginDialog_Msg_Login_Invalid;
	final private String LABEL_USER = Messages.LoginDialog_Label_User;
	final private String LABEL_PASSWORD = Messages.LoginDialog_Label_Password;

	private Text userText;
	private Text passwordText;

	private ITool tool;
	private boolean login_retry = false;

	/**
	 * The constructor.
	 * @param parentShell
	 * @param tool
	 */
	public DialogLogin(Shell parentShell, ITool tool) {
		this(parentShell, tool, false);
	}
	
	/**
	 * The other constructor.
	 * @param parentShell
	 * @param tool
	 * @param login_retry
	 */
	public DialogLogin(Shell parentShell, ITool tool, boolean login_retry) {
		super(parentShell);
		this.login_retry = login_retry;
		this.tool = tool;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		setTitle(WINDOW_TITLE);
		
		if (login_retry) {
			setErrorMessage(WINDOW_MESSAGE_LOGIN_INVALID);
		} else {
			setMessage(WINDOW_MESSAGE);
		}
		
		Composite dialogArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		
		gridLayout.marginLeft = 7;
		gridLayout.marginRight = 7;
		
		dialogArea.setLayout(gridLayout);
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		dialogArea.setFont(parent.getFont());
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	
		Label hostLabel = new Label(dialogArea, SWT.NULL);
		hostLabel.setText(LABEL_USER);
		userText = new Text(dialogArea, SWT.BORDER);
		userText.setLayoutData(gridData);
		
		Label portLabel = new Label(dialogArea, SWT.NULL);
		portLabel.setText(LABEL_PASSWORD);
		passwordText = new Text(dialogArea, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(gridData);
		
		return dialogArea;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		return super.getInitialSize();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		tool.login(userText.getText(), passwordText.getText());
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		tool.disconnect();
		super.cancelPressed();
	}
}
