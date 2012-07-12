/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.snapshot;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class SnapshotOptionsTab extends ProfileLaunchConfigurationTab {

	Composite top;
	Combo providerCombo;
	AbstractLaunchConfigurationTab[] tabs;
	ILaunchConfiguration initial;

	public void createControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout(1, true));
		providerCombo = new Combo(top, SWT.READ_ONLY);
		providerCombo.setItems(ProfileLaunchConfigurationTabGroup
				.getTabGroupIdsForType("snapshot"));

		final CTabFolder tabgroup = new CTabFolder(top, SWT.NONE);

		providerCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// dispose of old tabs
				for (CTabItem item : tabgroup.getItems()) {
					item.dispose();
				}

				// get the tabs associated with the selected ID
				tabs = ProfileLaunchConfigurationTabGroup
						.getTabGroupProviderFromId(providerCombo.getText())
						.getProfileTabs();

				// create the tab item, and load the specified tab inside
				for (ILaunchConfigurationTab tab : tabs) {
					CTabItem item = new CTabItem(tabgroup, SWT.NONE);
					item.setText(tab.getName());
					item.setImage(tab.getImage());

					tab.createControl(tabgroup);
					item.setControl(tab.getControl());
				}

				// initialize all tab widgets based on the configuration
				initializeFrom(initial);
				top.layout();
			}
		});

	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (providerCombo != null && !providerCombo.getText().equals("")) {
			for (AbstractLaunchConfigurationTab tab : tabs) {
				tab.setDefaults(configuration);
			}
		}
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		/**
		 *  First time the configuration is selected.
		 *
		 *  This is a cheap way to get access to the launch configuration.
		 *  Our tabs are loaded dynamically, so the tab group doesn't "know"
		 *  about them. We get access to this launch configuration to ensure
		 *  that we can properly load the widgets the first time.
		 */
		if (initial == null){
			initial = configuration;
		}
		if (providerCombo != null && !providerCombo.getText().equals("")) {
			for (AbstractLaunchConfigurationTab tab : tabs) {
				tab.initializeFrom(configuration);
			}
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (providerCombo != null && !providerCombo.getText().equals("")) {
			for (AbstractLaunchConfigurationTab tab : tabs) {
				tab.performApply(configuration);
			}
		}
	}

	public String getName() {
		return "Snapshot";
	}

}
