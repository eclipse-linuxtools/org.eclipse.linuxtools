package org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



public class SelectServerDialog extends Dialog {
	private Text hostText;
	private Text userText;
	private Text passwordText;
	private Button rememberButton;
	private Button connectButton;
	private Button cancelButton;
	private boolean result;

	public SelectServerDialog(Shell parent) {
		super(parent);
	}

	public boolean open() {
		if (ConsoleLogPlugin.getDefault().getPreferenceStore().getBoolean(ConsoleLogPreferenceConstants.REMEMBER_SERVER)) {
			return true;
		}
		result = false;

		Shell parent = getParent();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(Messages.SelectServerDialog_RemoteServerDetails);
		shell.pack();

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		shell.setLayout(layout);

		GridData dataLeft = new GridData();
		dataLeft.grabExcessHorizontalSpace = false;
		dataLeft.horizontalAlignment = SWT.LEFT;
		Label hostLabel = new Label(shell, SWT.NONE);
		hostLabel.setText(Messages.SelectServerDialog_Host);
		hostLabel.setLayoutData(dataLeft);

		GridData dataFill = new GridData();
		dataFill.grabExcessHorizontalSpace = true;
		dataFill.horizontalAlignment = SWT.FILL;
		hostText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		hostText.setLayoutData(dataFill);
		hostText.setText(ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.HOST_NAME));

		Label userLabel = new Label(shell, SWT.NONE);
		userLabel.setText(Messages.SelectServerDialog_User);
		userLabel.setLayoutData(dataLeft);

		userText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		userText.setLayoutData(dataFill);
		userText.setText(ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.SCP_USER));

		Label passwordLabel = new Label(shell, SWT.NONE);
		passwordLabel.setText(Messages.SelectServerDialog_Password);
		passwordLabel.setLayoutData(dataLeft);

		passwordText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		passwordText.setEchoChar('*');
		passwordText.setLayoutData(dataFill);
		passwordText.setText(ConsoleLogPlugin.getDefault().getPreferenceStore().getString(ConsoleLogPreferenceConstants.SCP_PASSWORD));


		GridData data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		data.horizontalSpan = 2;
		rememberButton = new Button(shell, SWT.CHECK);
		rememberButton.setLayoutData(data);
		rememberButton.setText(Messages.SelectServerDialog_AlwaysConnectToHost);

		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setLayoutData(data);
		cancelButton.setSize(50, 100);
		cancelButton.setText(Messages.SelectServerDialog_Cancel);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = false;
				shell.dispose();
			}
		});

		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		connectButton = new Button(shell, SWT.PUSH);
		connectButton.setLayoutData(data);
		connectButton.setSize(50, 100);
		connectButton.setText(Messages.SelectServerDialog_Connect);
		connectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// FIXME: no error handling is done, should probably be
				// pushed down to the connection level
				// Set the preferences to this new info.
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.HOST_NAME, hostText.getText());
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.REMEMBER_SERVER, rememberButton.getSelection());
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.SCP_PASSWORD, passwordText.getText());
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.SCP_USER, userText.getText());
				result = true;

				shell.close();
			}
		});

		shell.pack();
		shell.open();

		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		return result;
	}
}
