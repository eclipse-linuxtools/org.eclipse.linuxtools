/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;

/**
 * Initialize the default preferences.
 */
public class CreaterepoPreferenceInitializer extends
		AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// initial preferences
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		// CreaterepoPreferencePage
		node.putBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME, CreaterepoPreferenceConstants.DEFAULT_UNIQUE_MD_NAME);
		node.putBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB, CreaterepoPreferenceConstants.DEFAULT_GENERATE_DB);
		node.putBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS, CreaterepoPreferenceConstants.DEFAULT_IGNORE_SYMLINKS);
		node.putBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML, CreaterepoPreferenceConstants.DEFAULT_PRETTY);
		node.putInt(CreaterepoPreferenceConstants.PREF_WORKERS, CreaterepoPreferenceConstants.DEFAULT_NUM_WORKERS);
		node.putBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS, CreaterepoPreferenceConstants.DEFAULT_CHECK_TS);
		node.putInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT, CreaterepoPreferenceConstants.DEFAULT_CHANGELOG_LIMIT);
		node.put(CreaterepoPreferenceConstants.PREF_CHECKSUM, CreaterepoPreferenceConstants.DEFAULT_CHECKSUM);
		node.put(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE, CreaterepoPreferenceConstants.DEFAULT_COMPRESS_TYPE);

		// default preferences
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		// CreaterepoPreferencePage
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME, CreaterepoPreferenceConstants.DEFAULT_UNIQUE_MD_NAME);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_GENERATE_DB, CreaterepoPreferenceConstants.DEFAULT_GENERATE_DB);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS, CreaterepoPreferenceConstants.DEFAULT_IGNORE_SYMLINKS);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_PRETTY_XML, CreaterepoPreferenceConstants.DEFAULT_PRETTY);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_WORKERS, CreaterepoPreferenceConstants.DEFAULT_NUM_WORKERS);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_CHECK_TS, CreaterepoPreferenceConstants.DEFAULT_CHECK_TS);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT, CreaterepoPreferenceConstants.DEFAULT_CHANGELOG_LIMIT);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_CHECKSUM, CreaterepoPreferenceConstants.DEFAULT_CHECKSUM);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE, CreaterepoPreferenceConstants.DEFAULT_COMPRESS_TYPE);

		// CreaterepoGeneralPropertyPage
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED, CreaterepoPreferenceConstants.DEFAULT_GENERAL_ENABLED);

		// CreaterepoDeltaPropertyPage
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE, CreaterepoPreferenceConstants.DEFAULT_DELTA_ENABLE);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_NUM_DELTAS, CreaterepoPreferenceConstants.DEFAULT_NUM_DELTAS);
		prefStore.setDefault(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE, CreaterepoPreferenceConstants.DEFAULT_MAX_DELTA_SIZE);
	}

}
