/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.dialogs;



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

import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.preferences.DashboardPreferenceConstants;

@SuppressWarnings("deprecation")
public class ScriptDetails extends Dialog {
	private Text dirText;
	private Text scriptText;
	private Button OKButton;
	private Button cancelButton;
	private boolean canceled = true; 
	
	public ScriptDetails(Shell parent) {
		super(parent);
	}
	
	/**
	 * This allows outside classes to determine if the user clicked ok or cancel.
	 * @return boolean representing whether the cancel button was pressed or not
	 */
	public boolean isCanceled() {
		return canceled;
	}
	
	public void open() {
		
		Shell parent = getParent();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Enter Script details");
		shell.setSize(350, 160);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		shell.setLayout(layout);
		
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = SWT.LEFT;
		Label dirLabel = new Label(shell, SWT.NONE);
		dirLabel.setText("Examples Dir: ");
		dirLabel.setLayoutData(data);
		
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		dirText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		dirText.setLayoutData(data);
		dirText.setText(DashboardPlugin.getDefault().getPluginPreferences().getString(DashboardPreferenceConstants.P_DASHBOARD_EXAMPLES_DIR));
		
		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalAlignment = SWT.LEFT;
		Label scriptLabel = new Label(shell, SWT.NONE);
		scriptLabel.setText("Script: ");
		scriptLabel.setLayoutData(data);
		
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		scriptText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		scriptText.setLayoutData(data);
		//scriptText.setText(ConsoleLogPlugin.getDefault().getPluginPreferences().getString(ConsoleLogPreferenceConstants.SCP_PASSWORD));
		
		
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setLayoutData(data);
		cancelButton.setSize(50, 100);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		OKButton = new Button(shell, SWT.PUSH);
		OKButton.setLayoutData(data);
		OKButton.setSize(50, 100);
		OKButton.setText("OK");
		OKButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// FIXME: no error handling is done, should probably be
				// pushed down to the connection level
				// Set the preferences to this new info.
				DashboardPlugin.getDefault().getPreferenceStore().setValue(DashboardPreferenceConstants.P_DASHBOARD_EXAMPLES_DIR, dirText.getText());
				DashboardPlugin.getDefault().getPreferenceStore().setValue(DashboardPreferenceConstants.P_DASHBOARD_SCRIPT, scriptText.getText());
				canceled = false;
				shell.close();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}
}
