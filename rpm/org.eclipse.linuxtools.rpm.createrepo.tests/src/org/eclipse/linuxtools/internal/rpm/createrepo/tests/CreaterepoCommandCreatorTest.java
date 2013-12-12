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
package org.eclipse.linuxtools.internal.rpm.createrepo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoCommandCreator;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoChecksums;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoCompressionTypes;
import org.eclipse.linuxtools.rpm.createrepo.tests.TestCreaterepoProject;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Tests for CreaterepoCommandCreator class. This checks if the commands
 * are properly generated from preferences.
 */
public class CreaterepoCommandCreatorTest {

	private static TestCreaterepoProject testProject;
	private CreaterepoProject project;

	/**
	 * Initialize the project.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testProject = new TestCreaterepoProject();
		assertTrue(testProject.getProject().exists());
	}

	/**
	 * Delete the project when tests are done.
	 *
	 * @throws CoreException
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		testProject.dispose();
		assertFalse(testProject.getProject().exists());
	}

	/**
	 * Get the CreaterepoProject at the beginning of each test.
	 *
	 * @throws CoreException
	 */
	@Before
	public void setUp() throws Exception {
		project = testProject.getCreaterepoProject();
		assertNotNull(project);
	}

	/**
	 * Clear the preferences after each test.
	 *
	 * @throws BackingStoreException
	 */
	@After
	public void tearDown() throws Exception {
		IEclipsePreferences pref = project.getEclipsePreferences();
		pref.clear();
		pref.flush();
		testProject.restoreDefaults();
		assertEquals(0, pref.keys().length);
	}

	/**
	 * Preference set should be shown in the command arguments created.
	 *
	 * @throws BackingStoreException
	 */
	@Test
	public void testCommandArgumentsWorkspacePreferences() {
		setWorkspacePreferences();
		CreaterepoCommandCreator commandCreator = new CreaterepoCommandCreator(project.getEclipsePreferences());
		List<String> commands = commandCreator.getCommands();
		assertFalse(commands.isEmpty());
		String switchesToAdd = createStringFromList(commands);
		// waldo should not be found
		assertEquals(0, findCommandSwitch(switchesToAdd, "waldo")); //$NON-NLS-1$
		// deltas was not set
		assertEquals(0, findCommandSwitch(switchesToAdd, "--deltas")); //$NON-NLS-1$
		// these switches can be found set in setWorkspacePreferences()
		assertEquals(1, findCommandSwitch(switchesToAdd, "--simple-md-filenames")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--no-database")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--skip-symlinks")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--pretty")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--workers\\s+\\d+")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--checkts")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--changelog-limit\\s+\\d+")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--checksum\\s+\\S+")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--compress-type\\s+\\S+")); //$NON-NLS-1$
		// intentionally put multiple occurences of a switch that should only occur once
		String testDuplicate = "--changelog-limit 31 --changelog-limit 5 --changelog-limit 2"; //$NON-NLS-1$
		assertTrue(findCommandSwitch(testDuplicate, "--changelog-limit\\s+\\d+") > 1); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--no-database")); //$NON-NLS-1$
	}


	/**
	 * Set test workspace preferences.
	 */
	private void setWorkspacePreferences() {
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		// --simple-md-filenames
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME, false);
		// --no-database
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_GENERATE_DB, false);
		// --skip-symlinks
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS, true);
		// --pretty
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_PRETTY_XML, true);
		// --workers 73
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_WORKERS, 73);
		// --checkts
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_CHECK_TS, true);
		// --changelog-limit 31
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT, 31);
		// --checksum md5
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_CHECKSUM, ICreaterepoChecksums.MD5);
		// --compress-type gz
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE, ICreaterepoCompressionTypes.GZ);
	}

	/**
	 * Preference set should be shown in the command arguments created.
	 *
	 * @throws BackingStoreException
	 */
	@Test
	public void testCommandArgumentsProjectPreferences() throws BackingStoreException {
		setProjectPreferences();
		CreaterepoCommandCreator commandCreator = new CreaterepoCommandCreator(project.getEclipsePreferences());
		List<String> commands = commandCreator.getCommands();
		assertFalse(commands.isEmpty());
		String switchesToAdd = createStringFromList(commands);
		// waldo should not be found
		assertEquals(0, findCommandSwitch(switchesToAdd, "waldo")); //$NON-NLS-1$
		// make sure that the workspace preference is using --simple-md-filenames
		assertFalse(Activator.getDefault().getPreferenceStore().getBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME));
		// make sure project properties are being used
		assertTrue(Activator.isProjectPrefEnabled());
		// simple should not be found because project properties are enabled and the project properties set
		// it as unique
		assertEquals(0, findCommandSwitch(switchesToAdd, "--simple-md-filenames")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--unique-md-filenames")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--no-database")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--skip-symlinks")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--workers\\s+\\d+")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--checkts")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--changelog-limit\\s+\\d+")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--checksum\\s+\\S+")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--compress-type\\s+\\S+")); //$NON-NLS-1$
		assertTrue(Activator.isDeltaPrefEnabled());
		assertEquals(1, findCommandSwitch(switchesToAdd, "--deltas")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--num-deltas\\s+\\d+")); //$NON-NLS-1$
		assertEquals(1, findCommandSwitch(switchesToAdd, "--max-delta-rpm-size\\s+\\d+")); //$NON-NLS-1$
		// 2 directories were specified
		assertEquals(2, findCommandSwitch(switchesToAdd, "--oldpackagedirs\\s+\\S+")); //$NON-NLS-1$
		// 4 distro tags were specified
		assertEquals(4, findCommandSwitch(switchesToAdd, "--distro\\s+\\S+")); //$NON-NLS-1$
		// no content tags were specified, but it is left over from removing a content tag in the metadata page
		// it should not appear
		assertEquals(0, findCommandSwitch(switchesToAdd, "--content\\s+\\S+")); //$NON-NLS-1$
		// pretty setting was not set
		assertEquals(0, findCommandSwitch(switchesToAdd, "--pretty")); //$NON-NLS-1$
	}

	/**
	 * Set test project preferences.
	 *
	 * @throws BackingStoreException 
	 */
	private void setProjectPreferences() throws BackingStoreException {
		// enable use of project preferences
		IPreferenceStore prefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED, true);
		// --simple-md-filenames should not be used because project properties are enabled
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME, false);
		// set the project preferences
		IEclipsePreferences pref = project.getEclipsePreferences();
		// --unique-md-filenames
		pref.putBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME, true);
		// --no-database
		pref.putBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB, false);
		// --skip-symlinks
		pref.putBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS, true);
		// --pretty
		pref.putBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML, false);
		// --workers 13
		pref.putInt(CreaterepoPreferenceConstants.PREF_WORKERS, 13);
		// --checkts
		pref.putBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS, true);
		// --changelog-limit 37
		pref.putInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT, 37);
		// --checksum sha512
		pref.put(CreaterepoPreferenceConstants.PREF_CHECKSUM, ICreaterepoChecksums.SHA512);
		// --compress-type xz
		pref.put(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE, ICreaterepoCompressionTypes.XZ);
		// enable deltas
		// --deltas
		prefStore.setValue(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE, true);
		// --num-deltas 13
		pref.putInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS, 13);
		// --max-delta-rpm-size 37
		pref.putInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE, 37);
		// --oldpackagedirs /tmp --oldpackagedirs /home
		pref.put(CreaterepoPreferenceConstants.PREF_OLD_PACKAGE_DIRS, "/tmp;/home"); //$NON-NLS-1$
		// --distro cpeid,distrotag --distro tag2 --distro tag3 --distro tag4
		pref.put(CreaterepoPreferenceConstants.PREF_DISTRO_TAG, "cpeid,distrotag;tag2;tag3;tag4"); //$NON-NLS-1$
		pref.put(CreaterepoPreferenceConstants.PREF_CONTENT_TAG, ""); //$NON-NLS-1$
		pref.flush();
	}

	/**
	 * Create a string from a given list of strings.
	 *
	 * @param list A list of strings.
	 * @return A string.
	 */
	private String createStringFromList(List<String> list) {
		String str = ""; //$NON-NLS-1$
		if (!list.isEmpty()) {
			for (String temp : list) {
				if (!temp.isEmpty()) {
					str = str.concat(temp).concat(" "); //$NON-NLS-1$
				}
			}
			str = str.trim();
		}
		return str;
	}

	/**
	 * Try and find if a command string has a specific switch in it.
	 *
	 * @param command The command string.
	 * @param switchPattern The switch to look for.
	 * @return 0 if switch cannot be found, 1 if single found, >1 if multiple found.
	 */
	private int findCommandSwitch(String command, String switchPattern) {
		int matches = 0;
		Pattern pattern = Pattern.compile(switchPattern, Pattern.CASE_INSENSITIVE);
		Matcher variableMatcher = pattern.matcher(command);
		while (variableMatcher.find()) {
			matches++;
		}
		return matches;
	}
}
