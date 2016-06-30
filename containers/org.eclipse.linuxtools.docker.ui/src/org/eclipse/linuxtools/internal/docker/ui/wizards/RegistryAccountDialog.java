package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RegistryAccountDialog extends Dialog {

	private String serverAddress;
	private String username;
	private String email;
	private char[] password;
	private String title;
	private String explanation = WizardMessages
			.getString("RegistryAccountDialog.add.edit.explanation"); //$NON-NLS-1$

	public RegistryAccountDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
	}

	public RegistryAccountDialog(Shell parentShell, String title,
			String defaultServerAddress, String explanation) {
		super(parentShell);
		this.title = title;
		this.serverAddress = defaultServerAddress;
		this.explanation = explanation;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final int COLUMNS = 2;

		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(10, 10)
				.applyTo(container);

		final Label explanationLabel = new Label(container, SWT.NONE);
		explanationLabel.setText(explanation);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(false, false).applyTo(explanationLabel);

		final Label serverLabel = new Label(container, SWT.NONE);
		serverLabel.setText(
				WizardMessages.getString("RegistryAccountDialog.server.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(serverLabel);
		final Text serverText = new Text(container, SWT.BORDER);
		serverText.setToolTipText(
				WizardMessages.getString("RegistryAccountDialog.server.desc")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(serverText);
		if (serverAddress != null) {
			serverText.setText(serverAddress);
		}
		serverText.addModifyListener(e -> {
			serverAddress = serverText.getText();
			validate();
		});

		final Label usernameLabel = new Label(container, SWT.NONE);
		usernameLabel.setText(WizardMessages
				.getString("RegistryAccountDialog.username.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(usernameLabel);
		final Text usernameText = new Text(container, SWT.BORDER);
		usernameText.setToolTipText(WizardMessages
				.getString("RegistryAccountDialog.username.desc")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(usernameText);
		if (username != null) {
			usernameText.setText(username);
		}
		usernameText.addModifyListener(e -> {
			username = usernameText.getText();
			validate();
		});

		final Label emailLabel = new Label(container, SWT.NONE);
		emailLabel.setText(
				WizardMessages.getString("RegistryAccountDialog.email.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(emailLabel);
		final Text emailText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(emailText);
		if (email != null) {
			emailText.setText(email);
		} else {
			email = ""; //$NON-NLS-1$
		}
		emailText.addModifyListener(e -> {
			email = emailText.getText();
		});

		final Label passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setText(WizardMessages
				.getString("RegistryAccountDialog.password.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(passwordLabel);
		final Text passwordText = new Text(container, SWT.BORDER);
		passwordText.setEchoChar('*');
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(passwordText);
		if (password != null) {
			passwordText.setText(new String(password));
		}
		passwordText.addModifyListener(e -> {
			password = passwordText.getText().toCharArray();
			validate();
		});

		return container;
	}

	private void validate() {
		if (serverAddress != null && !serverAddress.isEmpty()
				&& username != null && !username.isEmpty()
				&& password != null && password.length > 0) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	public void setInputData(IRegistryAccount info) {
		this.serverAddress = info.getServerAddress();
		this.username = info.getUsername();
		this.email = info.getEmail();
		this.password = info.getPassword();
	}

	public IRegistryAccount getSignonInformation() {
		return new RegistryAccountInfo(serverAddress, username, email, password);
	}

}
