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
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.linuxtools.profiling.snapshot.launch.Messages;

public class SnapshotPreferencesPage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	private static final String TYPE = "snapshot"; //$NON-NLS-1$
	private static final String KEY = "provider"; //$NON-NLS-1$
	private static final String QUALIFIER = "org.eclipse.linuxtools.profiling.snapshot"; //$NON-NLS-1$
	private static IScopeContext configScopeInstance = ConfigurationScope.INSTANCE;

	public void init(IWorkbench workbench) {
		final IPreferenceStore store = new ScopedPreferenceStore(
				configScopeInstance, QUALIFIER);
		setPreferenceStore(store);
		setDescription(Messages.SnapshotPreferencesPage_0);
	}

	@Override
	protected void createFieldEditors() {
		HashMap<String, String> map = ProfileLaunchConfigurationTabGroup
				.getProviderNamesForType(TYPE);
		// 2d array containing launch provider names on the first column and
		// corresponding id's on the second.
		String[][] providerList = new String[map.size()][2];
		int i = 0;
		for (Entry<String, String> entry : map.entrySet()) {
			providerList[i][0] = entry.getKey();
			providerList[i][1] = entry.getValue();
			i++;
		}
		RadioGroupFieldEditor editor = new RadioGroupFieldEditor(KEY,
				Messages.SnapshotPreferencesPage_1, 1, providerList,
				getFieldEditorParent());
		addField(editor);
	}

	public SnapshotPreferencesPage() {
		super(GRID);
	}

	public void initializeDefaultPreferences() {
		super.performDefaults();
		String providerId = ProfileLaunchShortcut
				.getDefaultLaunchShortcutProviderId(TYPE);
		configScopeInstance.getNode(QUALIFIER).put(KEY, providerId);
	}

	/**
	 * Get id of launch provider in the preference store.
	 * 
	 * @return unique launch provider identifier.
	 * @since 1.2
	 */
	public static String getSelectedProviderId() {
		return configScopeInstance.getNode(QUALIFIER).get(KEY, "");

	}
}