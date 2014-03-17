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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoChecksums;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoCompressionTypes;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.ICreaterepoTestConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.TestCreaterepoProject;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for CreaterepoPreferencePage class.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreaterepoPreferencePageTest {

	private static TestCreaterepoProject testProject;
	private CreaterepoProject project;
	private static SWTWorkbenchBot bot;

	/**
	 * Initialize the test project.
	 *
	 * @throws CoreException
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws CoreException {
		testProject = new TestCreaterepoProject();
		assertTrue(testProject.getProject().exists());
		bot = new SWTWorkbenchBot();
		testProject.restoreDefaults();
		try {
			bot.shell(ICreaterepoTestConstants.MAIN_SHELL).activate();
		} catch (WidgetNotFoundException e) {
			// cannot activate main shell, continue anyways
		}
	}

	/**
	 * Delete the project when tests are done.
	 *
	 * @throws CoreException
	 */
	@AfterClass
	public static void tearDownAfterClass() throws CoreException {
		testProject.dispose();
		assertFalse(testProject.getProject().exists());
	}

	/**
	 * Get the CreaterepoProject at the beginning of each test.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Before
	public void setUp() throws CoreException {
		project = testProject.getCreaterepoProject();
		assertNotNull(project);
		// before doing anything with SWTBot, activate the main shell
		try {
			bot.shell(ICreaterepoTestConstants.MAIN_SHELL).activate();
		} catch (WidgetNotFoundException e) {
			// cannot activate main shell, continue anyways
		}
		openPreferencePage();
	}

	/**
	 * Restore the defaults for the general preference page.
	 */
	@After
	public void tearDown() {
		try {
			if (bot.shell(ICreaterepoTestConstants.PREFERENCES).isActive()) {
				bot.shell(ICreaterepoTestConstants.PREFERENCES).close();
			}
		} catch (WidgetNotFoundException e) {
			// cannot close preference shell
		}
		testProject.restoreDefaults();
	}

	/**
	 * Test if all preferences modified in the general createrepo preference
	 * page persist to workspace preferences.
	 */
	@Test
	public void testChangePreferences() {
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanChecksumName).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanGenerateSQLDB).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanPrettyXML).click();
		bot.textWithLabel(Messages.CreaterepoPreferencePage_numWorkers).setText("9"); //$NON-NLS-1$
		bot.textWithLabel(Messages.CreaterepoPreferencePage_numChangelogLimit).setText("13"); //$NON-NLS-1$
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanCheckTS).click();
		bot.radio(ICreaterepoChecksums.SHA512).click();
		bot.radio(ICreaterepoCompressionTypes.XZ).click();
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		openPreferencePage();
		// verify the changes
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanChecksumName).isChecked());
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanGenerateSQLDB).isChecked());
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks).isChecked());
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanPrettyXML).isChecked());
		assertEquals(prefStore.getInt(CreaterepoPreferenceConstants.PREF_WORKERS),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoPreferencePage_numWorkers).getText()));
		assertEquals(prefStore.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoPreferencePage_numChangelogLimit).getText()));
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanCheckTS).isChecked());
		assertTrue(bot.radio(prefStore.getString(CreaterepoPreferenceConstants.PREF_CHECKSUM)).isSelected());
		assertTrue(bot.radio(prefStore.getString(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE)).isSelected());
	}

	/**
	 * Test if the restore defaults button resets the workspace preference settings back
	 * to its defaults.
	 */
	@Test
	public void testRestoreDefaults() {
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanChecksumName).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanGenerateSQLDB).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanPrettyXML).click();
		bot.textWithLabel(Messages.CreaterepoPreferencePage_numWorkers).setText("9"); //$NON-NLS-1$
		bot.textWithLabel(Messages.CreaterepoPreferencePage_numChangelogLimit).setText("13"); //$NON-NLS-1$
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanCheckTS).click();
		bot.radio(ICreaterepoChecksums.SHA512).click();
		bot.radio(ICreaterepoCompressionTypes.XZ).click();
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		openPreferencePage();
		bot.button(ICreaterepoTestConstants.DEFAULTS).click();
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		// veryify the changes
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_GENERATE_DB));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_PRETTY_XML));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_WORKERS));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_CHECK_TS));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_CHECKSUM));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME));
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME));
	}

	/**
	 * Test if all preferences stay the same as it was after pressing cancel
	 * instead of ok.
	 */
	@Test
	public void testCancel() {
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanChecksumName).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanGenerateSQLDB).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks).click();
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanPrettyXML).click();
		bot.textWithLabel(Messages.CreaterepoPreferencePage_numWorkers).setText("9"); //$NON-NLS-1$
		bot.textWithLabel(Messages.CreaterepoPreferencePage_numChangelogLimit).setText("13"); //$NON-NLS-1$
		bot.checkBox(Messages.CreaterepoPreferencePage_booleanCheckTS).click();
		bot.radio(ICreaterepoChecksums.SHA512).click();
		bot.radio(ICreaterepoCompressionTypes.XZ).click();
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		openPreferencePage();
		bot.button(ICreaterepoTestConstants.DEFAULTS).click();
		bot.button(ICreaterepoTestConstants.CANCEL_BUTTON).click();
		openPreferencePage();
		// verify the changes
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanChecksumName).isChecked());
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanGenerateSQLDB).isChecked());
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks).isChecked());
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanPrettyXML).isChecked());
		assertEquals(prefStore.getInt(CreaterepoPreferenceConstants.PREF_WORKERS),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoPreferencePage_numWorkers).getText()));
		assertEquals(prefStore.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoPreferencePage_numChangelogLimit).getText()));
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanCheckTS).isChecked());
		assertTrue(bot.radio(prefStore.getString(CreaterepoPreferenceConstants.PREF_CHECKSUM)).isSelected());
		assertTrue(bot.radio(prefStore.getString(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE)).isSelected());
	}

	/**
	 * Open the preference page.
	 */
	private static void openPreferencePage() {
		// open the preferences window
		bot.menu(ICreaterepoTestConstants.WINDOW).menu(ICreaterepoTestConstants.PREFERENCES).click();
		SWTBotShell shell = bot.shell(ICreaterepoTestConstants.PREFERENCES);
		shell.activate();
		bot.tree().select(ICreaterepoTestConstants.CREATEREPO_CATEGORY);
	}

}
