/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/

package org.eclipse.linuxtools.oprofile.launch.configuration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.oprofile.core.Oprofile;
import org.eclipse.linuxtools.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Thic class represents the event configuration tab of the launcher dialog.
 */
public class OprofileEventConfigTab extends AbstractLaunchConfigurationTab {
	private Button _defaultEventCheck;
	private OprofileCounter[] _counters = OprofileCounter.getCounters(null);
	private CounterSubTab[] _counterSubTabs;

	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout());

		createVerticalSpacer(top, 1);

		//default event checkbox
		_defaultEventCheck = new Button(top, SWT.CHECK);
		_defaultEventCheck.setText(OprofileLaunchMessages.getString("tab.event.defaultevent.button.text")); //$NON-NLS-1$
		_defaultEventCheck.setLayoutData(new GridData());
		_defaultEventCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				_handleEnabledToggle();
			}
		});

		createVerticalSpacer(top, 1);

		//tabs for each of the counters
		OprofileCounter[] counters = OprofileCounter.getCounters(null);
		TabItem[] counterTabs = new TabItem[counters.length];
		_counterSubTabs = new CounterSubTab[counters.length];
		
		TabFolder tabFolder = new TabFolder(top, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		for (int i = 0; i < counters.length; i++) {
			Composite c = new Composite(tabFolder, SWT.NONE);
			CounterSubTab currentTab = new CounterSubTab(c, counters[i], this);
			_counterSubTabs[i] = currentTab;
			
			counterTabs[i] = new TabItem(tabFolder, SWT.NONE);
			counterTabs[i].setControl(c);
			counterTabs[i].setText("Ctr " + String.valueOf(i));		//TODO: externalize this
		}
	}

	public void initializeFrom(ILaunchConfiguration config) {
		try {
			for (int i = 0; i < _counters.length; i++) {
				_counters[i].loadConfiguration(config);
			}
			
			for (CounterSubTab tab : _counterSubTabs) {
				tab.initializeTab(config);
			}

			boolean enabledState = config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, true);
			_defaultEventCheck.setSelection(enabledState);
			setEnabledState(!enabledState);

			updateLaunchConfigurationDialog();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public boolean isValid(ILaunchConfiguration config) {
		int numEnabledEvents = 0;
		boolean valid = true;

		OprofileCounter[] counters = new OprofileCounter[Oprofile.getNumberOfCounters()];
		for (int i = 0; i < counters.length; i++) {
			counters[i] = new OprofileCounter(i);
			counters[i].loadConfiguration(config);
			if (counters[i].getEnabled()) {
				++numEnabledEvents;

				if (counters[i].getEvent() == null) {
					valid = false;
					break;
				}

				// First check min count
				int min = counters[i].getEvent().getMinCount();
				if (counters[i].getCount() < min) {
					valid = false;
					break;
				}

				// Next ask oprofile if it is valid
				if (!Oprofile.checkEvent(
							counters[i].getNumber(), 
							counters[i].getEvent().getNumber(), 
							counters[i].getEvent().getUnitMask().getMaskValue())) 
				{
					valid = false;
					break;
				}
			}
		}

		boolean b =(numEnabledEvents > 0 && valid);
		return b;
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, _defaultEventCheck.getSelection());
		
		for (CounterSubTab cst : _counterSubTabs) {
			cst.performApply(config);
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		boolean useDefault = true;

		// When instantiated, the OprofileCounter will set defaults.
		for (int i = 0; i < _counters.length; i++) {
			_counters[i].saveConfiguration(config);
			if (_counters[i].getEnabled()) {
				useDefault = false;
			}
		}

		config.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, useDefault);
	}
	
	public String getName() {
		return OprofileLaunchMessages.getString("tab.event.name"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return OprofileLaunchPlugin.getImageDescriptor(OprofileLaunchPlugin.ICON_EVENT_TAB).createImage();
	}

	// handles the toggling of the default event check box
	private void _handleEnabledToggle() {
		setEnabledState(!_defaultEventCheck.getSelection());
		updateLaunchConfigurationDialog();
	}
	
	private void setEnabledState(boolean state) {
		for (CounterSubTab cst : _counterSubTabs) {
			cst.setEnabledState(state);
		}
	}

	public void __updateLaunchConfigurationDialog() {
		updateLaunchConfigurationDialog();
	}
	
	public void __setErrorMessage(String message) {
		setErrorMessage(message);
	}
	
	public void __createVerticalSpacer(Composite c, int colspan) {
		createVerticalSpacer(c, colspan);
	}
}
