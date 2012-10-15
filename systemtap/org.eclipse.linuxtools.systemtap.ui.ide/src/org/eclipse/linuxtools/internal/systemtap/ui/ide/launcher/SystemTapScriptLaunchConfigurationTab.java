/*******************************************************************************
 * Copyright (c) 2012 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SystemTapScriptLaunchConfigurationTab extends
		AbstractLaunchConfigurationTab {

	public void createControl(Composite parent) {
		
		GridLayout layout = new GridLayout();
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(layout);
		top.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true));

		// Script path
		Group scriptSettingsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		scriptSettingsGroup.setText("Systemtap Script:");
		scriptSettingsGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false));
		layout = new GridLayout();
		layout.numColumns = 2;
		scriptSettingsGroup.setLayout(layout);
		Text scriptPathText = new Text(scriptSettingsGroup,  SWT.SINGLE | SWT.BORDER);
		scriptPathText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Button selectScriptButon = new Button(scriptSettingsGroup, 0);
		GridData gridData = new GridData();
		gridData.widthHint = 110;
		selectScriptButon.setLayoutData(gridData);
		selectScriptButon.setText("Browse...");

		// User Settings
		Group userSettingsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		layout = new GridLayout();
		userSettingsGroup.setLayout(layout);
		layout.numColumns = 2;
		userSettingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Button currentUserCheckButton = new Button(userSettingsGroup, SWT.CHECK);
		currentUserCheckButton.setText("Execute script as current user.");
		currentUserCheckButton.setSelection(true);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		currentUserCheckButton.setLayoutData(gridData);

		Label label = new Label(userSettingsGroup, SWT.NONE);
		label.setText("User Name:");
		Text userNameText = new Text(userSettingsGroup, SWT.SINGLE | SWT.BORDER);
		userNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		label = new Label(userSettingsGroup, SWT.NONE);
		label.setText("Password:");
		Text userPasswordText = new Text(userSettingsGroup, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		userPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		userSettingsGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false));
		userSettingsGroup.setText("User:");

		// Host settings
		Group hostSettingsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		hostSettingsGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false));
		hostSettingsGroup.setText("Host:");
		layout = new GridLayout();
		hostSettingsGroup.setLayout(layout);
		layout.numColumns = 2;

		Button localHostCheckButton = new Button(hostSettingsGroup, SWT.CHECK);
		localHostCheckButton.setText("Run script on localhost.");
		localHostCheckButton.setSelection(true);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		localHostCheckButton.setLayoutData(gridData);

		label = new Label(hostSettingsGroup, SWT.NONE);
		label.setText("Host Name:");
		Text hostNameText = new Text(hostSettingsGroup, SWT.SINGLE | SWT.BORDER);
		hostNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub

	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	public String getName() {
		return "Settings"; 
	}

}
