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

import static org.junit.Assert.*;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.tests.ICreaterepoTestConstants;
import org.eclipse.linuxtools.rpm.createrepo.tests.TestCreaterepoProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Tests for CreaterepoDeltaPropertyPage class.
 */
public class CreaterepoDeltaPropertyPageTest {

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
		openResourcePerspective();
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
	 * Test if all preferences modified in the deltas createrepo property
	 * page persist to project preferences.
	 */
	@Test
	public void testChangePreferences() {
		bot.checkBox(Messages.CreaterepoDeltaPropertyPage_booleanEnableLabel).click();
		bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxNumberOfDeltas).setText("9"); //$NON-NLS-1$
		bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxDeltaSize).setText("13"); //$NON-NLS-1$
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		openPropertyPage();
		IEclipsePreferences pref = project.getEclipsePreferences();
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE),
				bot.checkBox(Messages.CreaterepoDeltaPropertyPage_booleanEnableLabel).isChecked());
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS)),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxNumberOfDeltas).getText()));
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE)),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxDeltaSize).getText()));
	}

	/**
	 * Test if all preferences modified in the deltas createrepo property
	 * page will reset to default.
	 */
	@Test
	public void testRestoreDefaults() {
		bot.checkBox(Messages.CreaterepoDeltaPropertyPage_booleanEnableLabel).click();
		bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxNumberOfDeltas).setText("9"); //$NON-NLS-1$
		bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxDeltaSize).setText("13"); //$NON-NLS-1$
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		openPropertyPage();
		bot.button(ICreaterepoTestConstants.DEFAULTS).click();
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		IEclipsePreferences pref = project.getEclipsePreferences();
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertTrue(prefStore.isDefault(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE));
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS)),
				prefStore.getDefaultInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS));
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE)),
				prefStore.getDefaultInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE));
	}

	/**
	 * Test if all preferences stay the same as it was after pressing cancel
	 * instead of ok.
	 */
	@Test
	public void testCancel() {
		bot.checkBox(Messages.CreaterepoDeltaPropertyPage_booleanEnableLabel).click();
		bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxNumberOfDeltas).setText("9"); //$NON-NLS-1$
		bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxDeltaSize).setText("13"); //$NON-NLS-1$
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		openPropertyPage();
		bot.button(ICreaterepoTestConstants.DEFAULTS).click();
		bot.button(ICreaterepoTestConstants.CANCEL_BUTTON).click();
		openPropertyPage();
		IEclipsePreferences pref = project.getEclipsePreferences();
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		assertEquals(prefStore.getBoolean(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE),
				bot.checkBox(Messages.CreaterepoDeltaPropertyPage_booleanEnableLabel).isChecked());
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_NUM_DELTAS)),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxNumberOfDeltas).getText()));
		assertEquals(pref.getInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE,
				prefStore.getInt(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE)),
				Integer.parseInt(bot.textWithLabel(Messages.CreaterepoDeltaPropertyPage_maxDeltaSize).getText()));
	}

	/**
	 * Open the resource perspective.
	 */
	private static void openResourcePerspective() {
		// turn on the resource perspective
		bot.menu(ICreaterepoTestConstants.WINDOW).menu(ICreaterepoTestConstants.SHOW_VIEW)
		.menu(ICreaterepoTestConstants.OTHER).click();
		SWTBotShell shell = bot.shell(ICreaterepoTestConstants.SHOW_VIEW);
		shell.activate();
		bot.tree().expandNode(ICreaterepoTestConstants.GENERAL_NODE).select(ICreaterepoTestConstants.NAVIGATOR);
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
	}

	/**
	 * Open the property page.
	 */
	private static void openPropertyPage() {
		SWTBotView view = bot.viewByTitle(ICreaterepoTestConstants.NAVIGATOR);
		view.show();
		// select the .repo file from the package explorer and open its properties
		Composite packageExplorer = (Composite)view.getWidget();
		assertNotNull(packageExplorer);
		Tree swtTree = bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), packageExplorer);
		assertNotNull(swtTree);
		SWTBotTree botTree = new SWTBotTree(swtTree);
		botTree.expandNode(ICreaterepoTestConstants.PROJECT_NAME).getNode(ICreaterepoTestConstants.REPO_NAME)
			.contextMenu(ICreaterepoTestConstants.PROPERTIES).click();
		// get a handle of the property shell
		SWTBotShell propertyShell = bot.shell(String.format(ICreaterepoTestConstants.PROPERTIES_SHELL,
				ICreaterepoTestConstants.REPO_NAME));
		propertyShell.activate();
		bot.tree().expandNode(ICreaterepoTestConstants.CREATEREPO_CATEGORY).getNode(ICreaterepoTestConstants.DELTAS).select();
	}

}
