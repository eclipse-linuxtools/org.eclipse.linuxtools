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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
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
	private static final String PROJ_NAME = "fibTest";
	private static final String STUB_TOOLTIP = "tooltip test";
	private static final String STUB_LABEL = "Test Tool [description test]";
	private static final String PROFILING_PREFS_CATEGORY = "Timing";
	private static final String PROFILING_PREFS_TYPE = "timing";
	private static final String[][] PROFILING_PREFS_INFO = {
			{ "Coverage", "coverage" }, { "Memory", "memory" },{ "Timing", "timing" } };

	@BeforeClass
	public static void setUpWorkbench() throws Exception {
		// Set up is based from from GcovTest{c,CPP}.

		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
		} catch (WidgetNotFoundException e) {
			// ignore
		}

		// Set C/C++ perspective.
		bot.perspectiveByLabel("C/C++").activate();
		bot.sleep(500);
		for (SWTBotShell sh : bot.shells()) {
			if (sh.getText().startsWith("C/C++")) {
				sh.activate();
				bot.sleep(500);
				break;
			}
		}

		// Turn off automatic building by default to avoid timing issues
		SWTBotMenu windowsMenu = bot.menu("Window");
		windowsMenu.menu("Preferences").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();
		bot.tree().expandNode("General").select("Workspace");
		SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
		if (buildAuto != null && buildAuto.isChecked()) {
			buildAuto.click();
		}
		bot.sleep(1000);
		bot.button("Apply").click();
		bot.button("OK").click();
		bot.waitUntil(shellCloses(shell));
	}

	@Test
	public void testDefaultPreferences() throws Exception {
		for (String[] preferenceInfo : PROFILING_PREFS_INFO) {
			checkDefaultPreference(preferenceInfo[0], preferenceInfo[1]);
		}
	}

	@Test
	public void testPreferencesPage() throws Exception {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();

		// Set default tool for "timing" profiling.
		checkDefaultPreference(PROFILING_PREFS_CATEGORY, PROFILING_PREFS_TYPE);

		// Open preferences shell.
		SWTBotMenu windowsMenu = bot.menu("Window");
		windowsMenu.menu("Preferences").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();

		// Go to "Profiling Categories" preferences page.
		SWTBotTreeItem treeItem = bot.tree().expandNode("C/C++").expandNode("Profiling Categories");
		assertNotNull(treeItem);

		// Select "Timing" category page.
		treeItem.select(PROFILING_PREFS_CATEGORY);

		// Get name of default tool to deselect.
		String defaultToolId = ProviderFramework.getProviderIdToRun(null, PROFILING_PREFS_TYPE);
		String defaultToolName = ProviderFramework.getToolInformationFromId(defaultToolId , "name");

		// Workaround for BZ #344484.
		deselectSelectionByName(defaultToolName, bot);

		// Assert specified tool to select is what we expect and select it.
		SWTBotRadio stubRadio = bot.radio(STUB_LABEL);
		assertNotNull(stubRadio);
		assertTrue(STUB_TOOLTIP.equals(stubRadio.getToolTipText()));
		stubRadio.click();

		bot.button("Apply").click();
		bot.button("OK").click();
	}

	@Test
	public void testProfileProject() throws Exception {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), PROJ_NAME);
		testPreferencesPage();

		// Focus on project explorer view.
		bot.viewByTitle("Project Explorer").bot();
		bot.activeShell();
		SWTBotTree treeBot = bot.tree();
		treeBot.setFocus();

		// Select project binary.
		// AbstractTest#createProjectAndBuild builds one executable binary under Binaries.
		treeBot.expandNode(PROJ_NAME).expandNode("Binaries").getNode(0).select();

		String menuItem = "Profiling Tools";
		String subMenuItem = "3 Profile Timing";

		// Click on "Profiling Tools -> 3 Profiling Timing" context menu to execute shortcut.
		ContextMenuHelper.clickContextMenu(treeBot, menuItem, subMenuItem);

		// Assert that the expected tool is running.
		SWTBotShell profileShell = bot.shell("Successful profile launch").activate();
		assertNotNull(profileShell);

		bot.button("OK").click();
		bot.waitUntil(shellCloses(profileShell));

		deleteProject(proj);
	}

	private static void checkDefaultPreference(String preferenceCategory, String profilingType){
		SWTWorkbenchBot bot = new SWTWorkbenchBot();

		// Open preferences shell.
		SWTBotMenu windowsMenu = bot.menu("Window");
		windowsMenu.menu("Preferences").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();

		// Go to specified tree item in "Profiling Categories" preferences page.
		SWTBotTreeItem treeItem = bot.tree().expandNode("C/C++").expandNode("Profiling Categories");
		assertNotNull(treeItem);

		treeItem.select(preferenceCategory);

		// Restore defaults.
		bot.button("Restore Defaults").click();
		bot.button("Apply").click();

		// Get information for default tool.
		String defaultToolId = ProviderFramework.getProviderIdToRun(null, profilingType);
		String defaultToolName = ProviderFramework.getToolInformationFromId(defaultToolId , "name");
		String defaultToolInfo = ProviderFramework.getToolInformationFromId(defaultToolId , "information");
		String defaultToolDescription = ProviderFramework.getToolInformationFromId(defaultToolId , "description");
		String defaultToolLabel = defaultToolName + " [" + defaultToolDescription + "]";

		// Assert default radio is as expected.
		SWTBotRadio defaultRadio = bot.radio(defaultToolLabel);
		assertNotNull(defaultRadio);
		assertTrue(defaultToolInfo.equals(defaultRadio.getToolTipText()));

		bot.button("Apply").click();
		bot.button("OK").click();
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return null;
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc)
			throws CoreException {
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
			public void run() {
				@SuppressWarnings("unchecked")
				Matcher<Widget> matcher = allOf(widgetOfType(Button.class),
						withStyle(SWT.RADIO, "SWT.RADIO"),
						withRegex(name + ".*"));

				Button b = (Button) bot.widget(matcher); // the current selection
				b.setSelection(false);
			}
		});
	}
}