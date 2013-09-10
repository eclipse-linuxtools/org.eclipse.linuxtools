/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.provider.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.FrameworkUtil;

@RunWith(SWTBotJunit4ClassRunner.class)
public class PreferencesTest extends AbstractTest{
	private static final String PROJ_NAME = "fibTest"; //$NON-NLS-1$
	private static final String STUB_TOOLTIP = "tooltip test"; //$NON-NLS-1$
	private static final String STUB_LABEL = "Test Tool [description test]"; //$NON-NLS-1$
	private static final String PROFILING_PREFS_CATEGORY = "Timing"; //$NON-NLS-1$
	private static final String PROFILING_PREFS_TYPE = "timing"; //$NON-NLS-1$
	private static final String[][] PROFILING_PREFS_INFO = {
			{ "Coverage", "coverage" }, { "Memory", "memory" },{ "Timing", "timing" } };  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	@BeforeClass
	public static void setUpWorkbench() throws Exception {
		// Set up is based from from GcovTest{c,CPP}.

		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close(); //$NON-NLS-1$
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate(); //$NON-NLS-1$
			bot.button("Cancel").click(); //$NON-NLS-1$
		} catch (WidgetNotFoundException e) {
			// ignore
		}

		// Set C/C++ perspective.
		bot.perspectiveByLabel("C/C++").activate(); //$NON-NLS-1$
		bot.sleep(500);
		for (SWTBotShell sh : bot.shells()) {
			if (sh.getText().startsWith("C/C++")) { //$NON-NLS-1$
				sh.activate();
				bot.sleep(500);
				break;
			}
		}

		// Turn off automatic building by default to avoid timing issues
		SWTBotMenu windowsMenu = bot.menu("Window"); //$NON-NLS-1$
		windowsMenu.menu("Preferences").click(); //$NON-NLS-1$
		SWTBotShell shell = bot.shell("Preferences"); //$NON-NLS-1$
		shell.activate();
		bot.tree().expandNode("General").select("Workspace"); //$NON-NLS-1$ //$NON-NLS-2$
		SWTBotCheckBox buildAuto = bot.checkBox("Build automatically"); //$NON-NLS-1$
		if (buildAuto != null && buildAuto.isChecked()) {
			buildAuto.click();
		}
		bot.sleep(1000);
		bot.button("Apply").click(); //$NON-NLS-1$
		bot.button("OK").click(); //$NON-NLS-1$
		bot.waitUntil(shellCloses(shell));
	}

	@Test
	public void testDefaultPreferences() {
		for (String[] preferenceInfo : PROFILING_PREFS_INFO) {
			checkDefaultPreference(preferenceInfo[0], preferenceInfo[1]);
		}
	}

	@Test
	public void testPreferencesPage() {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();

		// Set default tool for "timing" profiling.
		checkDefaultPreference(PROFILING_PREFS_CATEGORY, PROFILING_PREFS_TYPE);

		// Open preferences shell.
		SWTBotMenu windowsMenu = bot.menu("Window"); //$NON-NLS-1$
		windowsMenu.menu("Preferences").click(); //$NON-NLS-1$
		SWTBotShell shell = bot.shell("Preferences"); //$NON-NLS-1$
		shell.activate();

		// Go to "Profiling Categories" preferences page.
		SWTBotTreeItem treeItem = bot.tree().expandNode("C/C++").expandNode("Profiling").expandNode("Categories"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertNotNull(treeItem);

		// Select "Timing" category page.
		treeItem.select(PROFILING_PREFS_CATEGORY);

		// Get name of default tool to deselect.
		String defaultToolId = ProviderFramework.getProviderIdToRun(null, PROFILING_PREFS_TYPE);
		String defaultToolName = ProviderFramework.getToolInformationFromId(defaultToolId , "name"); //$NON-NLS-1$

		// Workaround for BZ #344484.
		deselectSelectionByName(defaultToolName, bot);

		// Assert specified tool to select is what we expect and select it.
		SWTBotRadio stubRadio = bot.radio(STUB_LABEL);
		assertNotNull(stubRadio);
		assertTrue(STUB_TOOLTIP.equals(stubRadio.getToolTipText()));
		stubRadio.click();

		bot.button("Apply").click(); //$NON-NLS-1$
		bot.button("OK").click(); //$NON-NLS-1$
	}

	@Test
	public void testProfileProject() throws InvocationTargetException, CoreException, URISyntaxException, InterruptedException, IOException {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), PROJ_NAME);
		testPreferencesPage();

		// Focus on project explorer view.
		bot.viewByTitle("Project Explorer").bot(); //$NON-NLS-1$
		bot.activeShell();
		SWTBotTree treeBot = bot.tree();
		treeBot.setFocus();

		// Select project binary.
		// AbstractTest#createProjectAndBuild builds one executable binary under Binaries.
		treeBot.expandNode(PROJ_NAME).expandNode("Binaries").getNode(0).select(); //$NON-NLS-1$

		String menuItem = "Profiling Tools"; //$NON-NLS-1$
		String subMenuItem = "3 Profile Timing"; //$NON-NLS-1$

		// Click on "Profiling Tools -> 3 Profiling Timing" context menu to execute shortcut.
		MenuItem menu = ContextMenuHelper.contextMenu(treeBot, menuItem, subMenuItem);
		click(menu);

		// Assert that the expected tool is running.
		SWTBotShell profileShell = bot.shell("Successful profile launch").activate(); //$NON-NLS-1$
		assertNotNull(profileShell);

		bot.button("OK").click(); //$NON-NLS-1$
		bot.waitUntil(shellCloses(profileShell));

		deleteProject(proj);
	}

	private static void checkDefaultPreference(String preferenceCategory, String profilingType){
		SWTWorkbenchBot bot = new SWTWorkbenchBot();

		// Open preferences shell.
		SWTBotMenu windowsMenu = bot.menu("Window"); //$NON-NLS-1$
		windowsMenu.menu("Preferences").click(); //$NON-NLS-1$
		SWTBotShell shell = bot.shell("Preferences"); //$NON-NLS-1$
		shell.activate();

		// Go to specified tree item in "Profiling Categories" preferences page.
		SWTBotTreeItem treeItem = bot.tree().expandNode("C/C++").expandNode("Profiling").expandNode("Categories"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertNotNull(treeItem);

		treeItem.select(preferenceCategory);

		// Restore defaults.
		bot.button("Restore Defaults").click(); //$NON-NLS-1$
		bot.button("Apply").click(); //$NON-NLS-1$

		// Get information for default tool.
		String defaultToolId = ProviderFramework.getProviderIdToRun(null, profilingType);
		String defaultToolName = ProviderFramework.getToolInformationFromId(defaultToolId , "name"); //$NON-NLS-1$
		String defaultToolInfo = ProviderFramework.getToolInformationFromId(defaultToolId , "information"); //$NON-NLS-1$
		String defaultToolDescription = ProviderFramework.getToolInformationFromId(defaultToolId , "description"); //$NON-NLS-1$
		String defaultToolLabel = defaultToolName + " [" + defaultToolDescription + "]"; //$NON-NLS-1$ //$NON-NLS-2$

		// Assert default radio is as expected.
		SWTBotRadio defaultRadio = bot.radio(defaultToolLabel);
		assertNotNull(defaultRadio);
		assertTrue(defaultToolInfo.equals(defaultRadio.getToolTipText()));

		bot.button("Apply").click(); //$NON-NLS-1$
		bot.button("OK").click(); //$NON-NLS-1$
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return null;
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
	}

	/**
	 * Deselect radio button with partial label name.
	 *
	 * Adapted workaround for BZ #344484:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=344484#c1
	 *
	 * @param name partial label of radio button to deselect.
	 */
	public static void deselectSelectionByName(final String name, final SWTWorkbenchBot bot) {
		UIThreadRunnable.syncExec(new VoidResult() {
			@Override
			public void run() {
				@SuppressWarnings("unchecked")
				Matcher<Widget> matcher = allOf(widgetOfType(Button.class),
						withStyle(SWT.RADIO, "SWT.RADIO"), //$NON-NLS-1$
						withRegex(name + ".*")); //$NON-NLS-1$

				Button b = (Button) bot.widget(matcher); // the current selection
				b.setSelection(false);
			}
		});
	}

	/**
	 * Click specfied menu item.
	 *
	 * @param menuItem
	 *            menu item to click
	 */
	public static void click(final MenuItem menuItem) {
		final Event event = new Event();
		event.time = (int) System.currentTimeMillis();
		event.widget = menuItem;
		event.display = menuItem.getDisplay();
		event.type = SWT.Selection;

		UIThreadRunnable.asyncExec(menuItem.getDisplay(), new VoidResult() {
			@Override
			public void run() {
				menuItem.notifyListeners(SWT.Selection, event);
			}
		});
	}
}