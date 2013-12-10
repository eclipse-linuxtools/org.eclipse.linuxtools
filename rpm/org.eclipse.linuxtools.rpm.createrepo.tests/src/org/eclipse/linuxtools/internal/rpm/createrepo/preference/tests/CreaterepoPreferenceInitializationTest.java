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
package org.eclipse.linuxtools.internal.rpm.createrepo.preference.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;
import org.junit.Test;

/**
 * Tests for preferences if they are being initialized correctly.
 */
public class CreaterepoPreferenceInitializationTest {

	/**
	 * Test for the initial preferences to be set for the plugin. Test
	 * that if the default value is what the return is, then it failed.
	 * The defaults are intentionally wrong to test if correct values are
	 * stored incorrectly on initialization.
	 */
	@Test
	public void testPreferenceInitialization() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		assertNotEquals(!CreaterepoPreferenceConstants.DEFAULT_UNIQUE_MD_NAME, node.getBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
				!CreaterepoPreferenceConstants.DEFAULT_UNIQUE_MD_NAME));
		assertNotEquals(!CreaterepoPreferenceConstants.DEFAULT_GENERATE_DB, node.getBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB,
				!CreaterepoPreferenceConstants.DEFAULT_GENERATE_DB));
		assertNotEquals(!CreaterepoPreferenceConstants.DEFAULT_IGNORE_SYMLINKS, node.getBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
				!CreaterepoPreferenceConstants.DEFAULT_IGNORE_SYMLINKS));
		assertNotEquals(!CreaterepoPreferenceConstants.DEFAULT_PRETTY, node.getBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML,
				!CreaterepoPreferenceConstants.DEFAULT_PRETTY));
		assertNotEquals(-1309, node.getInt(CreaterepoPreferenceConstants.PREF_WORKERS, -1309));
		assertNotEquals(!CreaterepoPreferenceConstants.DEFAULT_CHECK_TS, node.getBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS,
				!CreaterepoPreferenceConstants.DEFAULT_CHECK_TS));
		assertNotEquals(-1309, node.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT, -1309));
		assertNotEquals(CreaterepoPreferenceConstants.DEFAULT_CHECKSUM.concat("test"), node.get(CreaterepoPreferenceConstants.PREF_CHECKSUM, //$NON-NLS-1$
				CreaterepoPreferenceConstants.DEFAULT_CHECKSUM.concat("test"))); //$NON-NLS-1$
		assertNotEquals(CreaterepoPreferenceConstants.DEFAULT_COMPRESS_TYPE.concat("test"), node.get(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE, //$NON-NLS-1$
				CreaterepoPreferenceConstants.DEFAULT_COMPRESS_TYPE.concat("test"))); //$NON-NLS-1$
	}

	/**
	 * These should be what is stored in the default preferences and what
	 * the values should change to if "Restore Defaults" on a preference page
	 * is clicked. Make sure it tests are checking values declared in
	 * {@linkplain CreaterepoPreferenceConstants}
	 */
	@Test
	public void testPreferenceDefaults() {
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_UNIQUE_MD_NAME, prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_GENERATE_DB, prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_IGNORE_SYMLINKS, prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_PRETTY, prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_NUM_WORKERS, prefStore.getDefaultInt(CreaterepoPreferenceConstants.PREF_WORKERS));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_CHECK_TS, prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_CHANGELOG_LIMIT, prefStore.getDefaultInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_CHECKSUM, prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_CHECKSUM));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_COMPRESS_TYPE, prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_GENERAL_ENABLED, prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_DELTA_ENABLE, prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_NUM_DELTAS, prefStore.getDefaultInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS));
		assertEquals(CreaterepoPreferenceConstants.DEFAULT_MAX_DELTA_SIZE, prefStore.getDefaultInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE));
	}

}
