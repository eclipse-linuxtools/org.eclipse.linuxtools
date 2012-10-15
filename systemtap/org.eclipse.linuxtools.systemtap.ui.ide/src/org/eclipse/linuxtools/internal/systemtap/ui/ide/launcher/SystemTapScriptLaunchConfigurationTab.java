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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
		scriptSettingsGroup.setText(Messages.SystemTapScriptLaunchConfigurationTab_0);
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
		selectScriptButon.setText(Messages.SystemTapScriptLaunchConfigurationTab_1);

		// User Settings
		Group userSettingsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		layout = new GridLayout();
		userSettingsGroup.setLayout(layout);
		layout.numColumns = 2;
		userSettingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Button currentUserCheckButton = new Button(userSettingsGroup, SWT.CHECK);
		currentUserCheckButton.setText(Messages.SystemTapScriptLaunchConfigurationTab_2);
		currentUserCheckButton.setSelection(true);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		currentUserCheckButton.setLayoutData(gridData);

		final Label userNameLabel = new Label(userSettingsGroup, SWT.NONE);
		userNameLabel.setText(Messages.SystemTapScriptLaunchConfigurationTab_3);
		final Text userNameText = new Text(userSettingsGroup, SWT.SINGLE | SWT.BORDER);
		userNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Label userPasswordLabel = new Label(userSettingsGroup, SWT.NONE);
		userPasswordLabel.setText(Messages.SystemTapScriptLaunchConfigurationTab_4);
		final Text userPasswordText = new Text(userSettingsGroup, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		userPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		userSettingsGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false));
		userSettingsGroup.setText(Messages.SystemTapScriptLaunchConfigurationTab_5);

		currentUserCheckButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				update();
			}
			
			private void update(){
				boolean enable = !currentUserCheckButton.getSelection();
				userNameText.setEnabled(enable);
				userNameLabel.setEnabled(enable);
				userPasswordText.setEnabled(enable);
				userPasswordLabel.setEnabled(enable);
			}
		});
		userNameText.setEnabled(false);
		userNameLabel.setEnabled(false);
		userPasswordText.setEnabled(false);
		userPasswordLabel.setEnabled(false);

		// Host settings
		Group hostSettingsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		hostSettingsGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false));
		hostSettingsGroup.setText(Messages.SystemTapScriptLaunchConfigurationTab_6);
		layout = new GridLayout();
		hostSettingsGroup.setLayout(layout);
		layout.numColumns = 2;

		final Button localHostCheckButton = new Button(hostSettingsGroup, SWT.CHECK);
		localHostCheckButton.setText(Messages.SystemTapScriptLaunchConfigurationTab_7);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		
		final Label hostNamelabel = new Label(hostSettingsGroup, SWT.NONE);
		hostNamelabel.setText(Messages.SystemTapScriptLaunchConfigurationTab_8);
		final Text hostNameText = new Text(hostSettingsGroup, SWT.SINGLE | SWT.BORDER);
		hostNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		localHostCheckButton.setLayoutData(gridData);
		localHostCheckButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				update();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				update();
			}
			
			private void update(){
				boolean enable = !localHostCheckButton.getSelection();
				hostNamelabel.setEnabled(enable);
				hostNameText.setEnabled(enable);
			}
		});
		localHostCheckButton.setSelection(true);
		hostNamelabel.setEnabled(false);
		hostNameText.setEnabled(false);
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
		return Messages.SystemTapScriptLaunchConfigurationTab_9; 
	}

}
