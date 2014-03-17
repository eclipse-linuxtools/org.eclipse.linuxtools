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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoChecksums;
import org.eclipse.linuxtools.internal.rpm.createrepo.ICreaterepoCompressionTypes;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.ICreaterepoTestConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.TestCreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.TestUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Tests for CreaterepoGeneralPropertyPage class.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreaterepoGeneralPropertyPageTest {

	private static TestCreaterepoProject testProject;
	private static SWTWorkbenchBot bot;
	private static SWTBotView navigator;
	private CreaterepoProject project;

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
		TestUtils.openResourcePerspective(bot);
		navigator = TestUtils.enterProjectFolder(bot);
	}

	/**
	 * Delete the project when tests are done.
	 *
	 * @throws CoreException
	 */
	@AfterClass
	public static void tearDownAfterClass() throws CoreException {
		TestUtils.exitProjectFolder(bot, navigator);
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
		openPropertyPage();
	}

	/**
	 * Restore the defaults for the general preference page.
	 *
	 * @throws BackingStoreException
	 */
	@After
	public void tearDown() throws BackingStoreException {
		try {
			if (bot.shell(String.format(ICreaterepoTestConstants.PROPERTIES_SHELL,
					ICreaterepoTestConstants.REPO_NAME)).isActive()) {
				bot.shell(String.format(ICreaterepoTestConstants.PROPERTIES_SHELL,
						ICreaterepoTestConstants.REPO_NAME)).close();
			}
		} catch (WidgetNotFoundException e) {
			// cannot close property shell
		}
		testProject.restoreDefaults();
		IEclipsePreferences pref = project.getEclipsePreferences();
		pref.clear();
		pref.flush();
		assertEquals(0, pref.keys().length);
	}

	/**
	 * Test if all preferences modified in the general createrepo property
	 * page persist to project preferences.
	 */
	@Test
	public void testChangePreferences() {
		bot.checkBox(Messages.CreaterepoGeneralPropertyPage_projectSettings).click();
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
		openPropertyPage();
		IEclipsePreferences pref = project.getEclipsePreferences();
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED),
				bot.checkBox(Messages.CreaterepoGeneralPropertyPage_projectSettings).isChecked());
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanChecksumName).isChecked());
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanGenerateSQLDB).isChecked());
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks).isChecked());
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanPrettyXML).isChecked());
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_WORKERS,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_WORKERS)),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoPreferencePage_numWorkers).getText()));
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT)),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoPreferencePage_numChangelogLimit).getText()));
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanCheckTS).isChecked());
		assertTrue(bot.radio(pref.get(CreaterepoPreferenceConstants.PREF_CHECKSUM,
				prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_CHECKSUM))).isSelected());
		assertTrue(bot.radio(pref.get(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE,
				prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE))).isSelected());
	}

	/**
	 * Test if all preferences modified in the general createrepo property
	 * page will reset to default.
	 */
	@Test
	public void testRestoreDefaults() {
		bot.checkBox(Messages.CreaterepoGeneralPropertyPage_projectSettings).click();
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
		openPropertyPage();
		bot.button(ICreaterepoTestConstants.DEFAULTS).click();
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		IEclipsePreferences pref = project.getEclipsePreferences();
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED));
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME)),
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME));
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB)),
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB));
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS)),
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS));
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML)),
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML));
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_WORKERS,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_WORKERS)),
				prefStore.getDefaultInt(CreaterepoPreferenceConstants.PREF_WORKERS));
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT)),
				prefStore.getDefaultInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT));
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS)),
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS));
		assertEquals(pref.get(CreaterepoPreferenceConstants.PREF_CHECKSUM, prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_CHECKSUM)),
				prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_CHECKSUM));
		assertEquals(pref.get(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE, prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE)),
				prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE));
	}

	/**
	 * Test if all preferences stay the same as it was after pressing cancel
	 * instead of ok.
	 */
	@Test
	public void testCancel() {
		bot.checkBox(Messages.CreaterepoGeneralPropertyPage_projectSettings).click();
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
		openPropertyPage();
		bot.button(ICreaterepoTestConstants.DEFAULTS).click();
		bot.button(ICreaterepoTestConstants.CANCEL_BUTTON).click();
		openPropertyPage();
		IEclipsePreferences pref = project.getEclipsePreferences();
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_GENERAL_ENABLED),
				bot.checkBox(Messages.CreaterepoGeneralPropertyPage_projectSettings).isChecked());
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanChecksumName).isChecked());
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_GENERATE_DB)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanGenerateSQLDB).isChecked());
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanIgnoreSymlinks).isChecked());
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_PRETTY_XML)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanPrettyXML).isChecked());
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_WORKERS,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_WORKERS)),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoPreferencePage_numWorkers).getText()));
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT)),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoPreferencePage_numChangelogLimit).getText()));
		assertEquals(pref.getBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS,
				prefStore.getDefaultBoolean(CreaterepoPreferenceConstants.PREF_CHECK_TS)),
				bot.checkBox(Messages.CreaterepoPreferencePage_booleanCheckTS).isChecked());
		assertTrue(bot.radio(pref.get(CreaterepoPreferenceConstants.PREF_CHECKSUM,
				prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_CHECKSUM))).isSelected());
		assertTrue(bot.radio(pref.get(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE,
				prefStore.getDefaultString(CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE))).isSelected());
	}

	/**
	 * Open the property page.
	 */
	private static void openPropertyPage() {
		TestUtils.openPropertyPage(bot, navigator);
		bot.tree().select(ICreaterepoTestConstants.CREATEREPO_CATEGORY);
	}

}
