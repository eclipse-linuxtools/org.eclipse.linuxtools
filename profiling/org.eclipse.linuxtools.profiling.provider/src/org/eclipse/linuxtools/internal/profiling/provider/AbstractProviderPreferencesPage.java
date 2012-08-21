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
package org.eclipse.linuxtools.internal.profiling.provider;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.linuxtools.internal.profiling.provider.launch.Messages;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public abstract class AbstractProviderPreferencesPage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static IScopeContext configScopeInstance = ConfigurationScope.INSTANCE;
	public static final String PREFS_KEY = "provider"; //$NON-NLS-1$

	public AbstractProviderPreferencesPage() {
		super(GRID);
	}

	public void init(IWorkbench workbench) {
			final IPreferenceStore store = new ScopedPreferenceStore(
					configScopeInstance, getProfilingType());
			setPreferenceStore(store);

	}

	public void initializeDefaultPreferences() {
			super.performDefaults();
			String providerId = ProfileLaunchShortcut
					.getDefaultLaunchShortcutProviderId(getProfilingType());
			configScopeInstance.getNode(getProfilingType())
					.put(PREFS_KEY, providerId);

	}

	@Override
	protected void createFieldEditors() {
			HashMap<String, String> map = ProfileLaunchConfigurationTabGroup
					.getProviderNamesForType(getProfilingType());
			// 2d array containing launch provider names on the first column and
			// corresponding id's on the second.
			String[][] providerList = new String[map.size()][2];
			int i = 0;
			for (Entry<String, String> entry : map.entrySet()) {
				providerList[i][0] = entry.getKey();
				providerList[i][1] = entry.getValue();
				i++;
			}
			RadioGroupFieldEditor editor = new RadioGroupFieldEditor(PREFS_KEY,
					Messages.ProviderPreferencesPage_1, 1, providerList,
					getFieldEditorParent());
			addField(editor);

	}

	/**
	 * Get profiling type of this plug-in.
	 *
	 * @return String profiling type this plug-in supports.
	 */
	protected abstract String getProfilingType();

}
