package org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;



public class SelectServerDialog extends Dialog {
	private Text hostText;
	private Text portText;
	private Text userText;
	private Text passwordText;
	private Button rememberButton;
	private Button connectButton;
	private Button cancelButton;
	
	public SelectServerDialog(Shell parent) {
		super(parent);
	}
	
	public void open() {
		if (ConsoleLogPlugin.getDefault().getPluginPreferences().getBoolean(ConsoleLogPreferenceConstants.REMEMBER_SERVER)) {
			return;
		}
		
		Shell parent = getParent();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Remote Server Details");
		shell.pack();
		//shell.setSize(350, 220);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		shell.setLayout(layout);
		
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = SWT.LEFT;
		Label hostLabel = new Label(shell, SWT.NONE);
		hostLabel.setText("Host: ");
		hostLabel.setLayoutData(data);
		
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		hostText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		hostText.setLayoutData(data);
		hostText.setText(ConsoleLogPlugin.getDefault().getPluginPreferences().getString(ConsoleLogPreferenceConstants.HOST_NAME));
		
		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = SWT.LEFT;
		Label portLabel = new Label(shell, SWT.NONE);
		portLabel.setText("Port: ");
		portLabel.setLayoutData(data);
		
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		portText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		portText.setLayoutData(data);
		portText.setText(ConsoleLogPlugin.getDefault().getPluginPreferences().getString(ConsoleLogPreferenceConstants.PORT_NUMBER));
		
		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = SWT.LEFT;
		Label userLabel = new Label(shell, SWT.NONE);
		userLabel.setText("User: ");
		userLabel.setLayoutData(data);
		
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		userText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		userText.setLayoutData(data);
		userText.setText(ConsoleLogPlugin.getDefault().getPluginPreferences().getString(ConsoleLogPreferenceConstants.SCP_USER));
		
		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = SWT.LEFT;
		Label passwordLabel = new Label(shell, SWT.NONE);
		passwordLabel.setText("Password: ");
		passwordLabel.setLayoutData(data);
		
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		passwordText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		passwordText.setEchoChar('*');
		passwordText.setLayoutData(data);
		passwordText.setText(ConsoleLogPlugin.getDefault().getPluginPreferences().getString(ConsoleLogPreferenceConstants.SCP_PASSWORD));
		
		
		data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		data.horizontalSpan = 2;
		rememberButton = new Button(shell, SWT.CHECK);
		rememberButton.setLayoutData(data);
		rememberButton.setText("Always connect to this host.");
		rememberButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setLayoutData(data);
		cancelButton.setSize(50, 100);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.CANCELLED, true);
				shell.dispose();
				
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		connectButton = new Button(shell, SWT.PUSH);
		connectButton.setLayoutData(data);
		connectButton.setSize(50, 100);
		connectButton.setText("Connect");
		connectButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// FIXME: no error handling is done, should probably be
				// pushed down to the connection level
				// Set the preferences to this new info.
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.PORT_NUMBER, portText.getText());
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.HOST_NAME, hostText.getText());
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.REMEMBER_SERVER, rememberButton.getSelection());
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.SCP_PASSWORD, passwordText.getText());
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.SCP_USER, userText.getText());
				ConsoleLogPlugin.getDefault().getPreferenceStore().setValue(ConsoleLogPreferenceConstants.CANCELLED, false);

				shell.close();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		shell.pack();
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		
	}
}
