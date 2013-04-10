/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial implementation
 *******************************************************************************/


package org.eclipse.linuxtools.systemtap.ui.ide.test.swtbot;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestCreateSystemtapScript {

	static SWTWorkbenchBot bot;

	private static final String SYSTEMTAP_PROJECT_NAME = "SystemtapTest";

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();

		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
		} catch (WidgetNotFoundException e) {
			//ignore
		}

		// Create a Systemtap project.
		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu("Project...");
		projectMenu.click();

		SWTBotShell shell = bot.shell("New Project");
		shell.activate();

		bot.tree().expandNode("General").select("Project");

		bot.button("Next >").click();

		bot.textWithLabel("Project name:").setText(SYSTEMTAP_PROJECT_NAME);
		bot.button("Finish").click();
		while (shell.isOpen()){
			bot.sleep(1000);
		}
	}

	public static void createScript(SWTWorkbenchBot bot, String scriptName) {

		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu("Other...");
		projectMenu.click();

		SWTBotShell shell = bot.shell("New");
		shell.activate();

		bot.tree().expandNode("Systemtap").select("Systemtap Script");
		bot.button("Next >").click();

		bot.textWithLabel("Script Name:").setText(scriptName);
		bot.button("Browse").click();

		bot.tree().select(SYSTEMTAP_PROJECT_NAME);
		bot.button("OK").click();

		bot.button("Finish").click();

		assert(bot.activeEditor().getTitle().equals(scriptName));
	}

	@Test
	public void testCreateScript(){
		createScript(bot, "testScript.stp");
	}
}