/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.swtbot.tests;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.FrameworkUtil;

/**
 * Abstract SWTBot test for Perf views, sub-classes must implement the abstract
 * methods to specify the test view and logic.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractSWTBotTest extends AbstractTest {
	private static final String PROJ_NAME = "fibTest";

	@BeforeClass
	public static void setUpWorkbench() throws Exception {
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
	public void runPerfViewTest() throws Exception {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();

		/*
		 * - Method returns when the build is complete -
		 * AbstractTest#createProjectAndBuild builds a single executable binary
		 * under "Binaries".
		 */
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()),
				PROJ_NAME);

		// Focus on project explorer view.
		bot.viewByTitle("Project Explorer").bot();
		bot.activeShell();
		SWTBotTree treeBot = bot.tree();
		treeBot.setFocus();

		// Select project binary.
		treeBot.expandNode(PROJ_NAME).expandNode("Binaries").getNode(0).select();

		// Launch configuration strings
		String menuItem = "Profiling Tools";
		String configMenuTitle = "Profiling Tools Configurations";
		String subMenuItem = configMenuTitle + "...";

		// Open profiling configurations dialog
		MenuItem menu = ContextMenuHelper.contextMenu(treeBot, menuItem, subMenuItem);
		click(menu);

		bot.shell(configMenuTitle).activate();

		// Create new Perf configuration
		SWTBotTree profilingConfigs = bot.tree();
		SWTBotTree perfNode = profilingConfigs.select("Profile with Perf");
		perfNode.contextMenu("New").click();

		// Activate options tab
		bot.cTabItem("Perf Options").activate();

		setPerfOptions(bot);

		bot.button("Apply").click();

		if (PerfCore.checkPerfInPath()) {
			bot.button("Profile").click();

		} else {
			bot.button("Close").click();
			openStubView();
		}

		testPerfView();
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

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return null;
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
	}

	/**
	 * Set Perf launch options.
	 *
	 * @param bot SWTWorkbenchBot bot focused in Perf options tab.
	 */
	protected abstract void setPerfOptions(SWTWorkbenchBot bot);

	/**
	 * Open view with fake data. To be used when perf is not installed.
	 */
	protected abstract void openStubView();

	/**
	 * Test perf view.
	 */
	protected abstract void testPerfView();

	/**
	 * Stub data for use in case the Perf tool is not installed.
	 */
	protected static class StubPerfData implements IPerfData {
		@Override
		public String getPerfData() {
			return "stub_perf_data";
		}

		@Override
		public String getTitle() {
			return "stub_perf_title";
		}

	}
}
