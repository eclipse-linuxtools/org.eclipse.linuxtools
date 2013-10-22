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
package org.eclipse.linuxtools.internal.profiling.launch.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.Messages;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public abstract class AbstractProviderPropertyTab extends AbstractCPropertyTab {

	protected abstract String getType();
	protected abstract String getPrefPageId();

	// Launch provider extension point tool information attribute
	private static final String PROVIDER_ATT_INFO = "information"; //$NON-NLS-1$

	// Launch provider extension point tool description attribute
	private static final String PROVIDER_ATT_DESC = "description"; //$NON-NLS-1$

	private Link fLink;
	private Button useProjectSetting;
	private ScopedPreferenceStore preferenceStore;
	private Group projectSettingsGroup;
	private Button[] radioButtons;
	private String value;

	@Override
	protected void createControls(final Composite parent) {
		super.createControls(parent);

		usercomp.setLayout(new GridLayout(2, true));

		// Get the property provider (project, file, folder) and fine the project.
		IResource resource = (IResource)page.getElement().getAdapter(IResource.class);
		IProject project = resource.getProject();

		// Create the preference store to use
		ProjectScope ps = new ProjectScope(project);
		ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps, ProviderProfileConstants.PLUGIN_ID);
		scoped.setSearchContexts(new IScopeContext[] { ps, InstanceScope.INSTANCE });
		setPreferenceStore(scoped);

		getPreferenceStore().setDefault(ProviderProfileConstants.USE_PROJECT_SETTINGS + getType(), false);

		useProjectSetting = new Button(usercomp, SWT.CHECK);
		useProjectSetting.setText(Messages.UseProjectSetting_0);
		useProjectSetting.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		useProjectSetting.setSelection(getPreferenceStore().getBoolean(ProviderProfileConstants.USE_PROJECT_SETTINGS + getType()));
		useProjectSetting.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateOptionsEnable();
			}
		});

		String highestProviderId = ProviderFramework.getHighestProviderId(getType());
		if (highestProviderId != null) {
			getPreferenceStore().setDefault(ProviderProfileConstants.PREFS_KEY + getType(), highestProviderId);
		} else {
			useProjectSetting.setEnabled(false);
		}

		fLink= new Link(usercomp, SWT.NULL);
		fLink.setText(Messages.PreferenceLink_0);
		fLink.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		fLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), getPrefPageId(), null, null).open();
			}
		});


		HashMap<String, String> map = ProviderFramework
				.getProviderNamesForType(getType());
		// 2d array containing launch provider names on the first column and
		// corresponding id's on the second.
		String[][] providerList = new String[map.size()][2];
		int i = 0;
		for (Entry<String, String> entry : map.entrySet()) {
			String toolId = entry.getValue();
			String toolDescription = ProviderFramework.getToolInformationFromId(toolId, PROVIDER_ATT_DESC);
			String toolName = entry.getKey();

			// Append tool description to tool name if available.
			if (toolDescription != null && !toolDescription.isEmpty()) {
				toolName = toolName + " " + "[" + toolDescription + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			providerList[i][0] = toolName;
			providerList[i][1] = toolId;
			i++;
		}

		projectSettingsGroup = new Group(usercomp, SWT.NONE);
		projectSettingsGroup.setFont(parent.getFont());
		projectSettingsGroup.setText(Messages.ProviderPreferencesPage_1);
		projectSettingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 8;
		layout.numColumns = 1;
		projectSettingsGroup.setLayout(layout);

        radioButtons = new Button[map.size()];
        for (int j = 0; j < radioButtons.length; j++) {
            Button radio = new Button(projectSettingsGroup, SWT.RADIO | SWT.LEFT);
            radio.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
            radioButtons[j] = radio;
            String[] labelAndValue = providerList[j];
			String curProviderId = labelAndValue[1];
			// Set tool tip description text.
			String toolInfo = ProviderFramework.getToolInformationFromId(curProviderId,
					PROVIDER_ATT_INFO);
			if (toolInfo != null && !toolInfo.isEmpty()) {
				radio.setToolTipText(toolInfo);
			}
			radio.setText(labelAndValue[0]);
            radio.setData(labelAndValue[1]);
            radio.setFont(parent.getFont());
            radio.addSelectionListener(new SelectionAdapter() {
            	@Override
				public void widgetSelected(SelectionEvent event) {
            		value = (String) event.widget.getData();
            	}
            });
        }
        projectSettingsGroup.addDisposeListener(new DisposeListener() {
            @Override
			public void widgetDisposed(DisposeEvent event) {
                projectSettingsGroup = null;
                radioButtons = null;
            }
        });
		updateOptionsEnable();
		updateValue(getPreferenceStore().getString(ProviderProfileConstants.PREFS_KEY + getType()));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, getHelpContextId());
	}

	private void setButtonsEnabled(boolean value) {
        for (int j = 0; j < radioButtons.length; j++) {
        	radioButtons[j].setEnabled(value);
        }
	}

	private void updateOptionsEnable() {
		if (useProjectSetting.getSelection() == true) {
			projectSettingsGroup.setEnabled(true);
			setButtonsEnabled(true);
			fLink.setVisible(false);
		} else {
			setButtonsEnabled(false);
			projectSettingsGroup.setEnabled(false);
			fLink.setVisible(true);
		}
	}

	@Override
	protected void performDefaults() {
		if (useProjectSetting.getSelection() == true)
			updateValue(getPreferenceStore().getDefaultString(ProviderProfileConstants.PREFS_KEY + getType()));
		updateOptionsEnable();
	}

	@Override
	public void performOK() {
		getPreferenceStore().setValue(ProviderProfileConstants.USE_PROJECT_SETTINGS + getType(), useProjectSetting.getSelection());
		getPreferenceStore().setValue(ProviderProfileConstants.PREFS_KEY + getType(), value);
		try {
			getPreferenceStore().save();
		} catch (IOException e) {
			// do nothing
		}
	}

	private ScopedPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}

	private void setPreferenceStore(ScopedPreferenceStore store) {
		preferenceStore = store;
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		getPreferenceStore().setValue(ProviderProfileConstants.USE_PROJECT_SETTINGS + getType(), useProjectSetting.getSelection());
		getPreferenceStore().setValue(ProviderProfileConstants.PREFS_KEY + getType(), value);
		try {
			getPreferenceStore().save();
		} catch (IOException e) {
			// do nothing
		}
	}

    /**
     * Select the radio button that conforms to the given value.
     *
     * @param selectedValue the selected value
     */
    private void updateValue(String selectedValue) {
        this.value = selectedValue;
        if (radioButtons == null) {
			return;
		}

        if (this.value != null) {
            boolean found = false;
            for (int i = 0; i < radioButtons.length; i++) {
                Button radio = radioButtons[i];
                boolean selection = false;
                if (((String) radio.getData()).equals(this.value)) {
                    selection = true;
                    found = true;
                }
                radio.setSelection(selection);
            }
            if (found) {
				return;
			}
        }

        // We weren't able to find the value. So we select the first
        // radio button as a default.
        if (radioButtons.length > 0) {
            radioButtons[0].setSelection(true);
            this.value = (String) radioButtons[0].getData();
        }
        return;
    }

	@Override
	public String getHelpContextId() {
		return ProviderProfileConstants.PLUGIN_ID + ".profiling_categories";  //$NON-NLS-1$
	}

	// This page can be displayed for project only
	@Override
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}

	@Override
	protected void updateButtons() {/* Empty block */}

	@Override
	protected void updateData(ICResourceDescription cfg) {/* Empty block */}

}
