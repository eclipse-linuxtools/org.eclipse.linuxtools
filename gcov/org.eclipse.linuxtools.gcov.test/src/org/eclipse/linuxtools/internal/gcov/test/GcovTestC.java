package org.eclipse.linuxtools.internal.gcov.test;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class GcovTestC {


		private static SWTWorkbenchBot	bot;

		private static final String PROJECT_NAME = "Gcov_C_test";
		private static final String PROJECT_TYPE = "C Project";
		

		@BeforeClass
		public static void beforeClass() throws Exception {
			bot = new SWTWorkbenchBot();
			bot.captureScreenshot(PROJECT_NAME + ".beforeClass.1.jpg");
			try {
				bot.viewByTitle("Welcome").close();
				// hide Subclipse Usage stats popup if present/installed
				bot.shell("Subclipse Usage").activate();
				bot.button("Cancel").click();
			} catch (WidgetNotFoundException e) {
				//ignore
			}

			bot.perspectiveByLabel("C/C++").activate();
			bot.sleep(500);
			for (SWTBotShell sh : bot.shells()) {
				if (sh.getText().startsWith("C/C++")) {
					sh.activate();
					bot.sleep(500);
					break;
				}
			}
			bot.captureScreenshot(PROJECT_NAME + ".beforeClass.2.jpg");
			// Turn off automatic building by default
			SWTBotMenu windowsMenu = bot.menu("Window");
			windowsMenu.menu("Preferences").click();
			SWTBotShell shell = bot.shell("Preferences");
			shell.activate();
			bot.tree().expandNode("General").select("Workspace");
			SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
			if (buildAuto != null && buildAuto.isChecked())
				buildAuto.click();
			bot.sleep(1000);
			bot.button("Apply").click();
			bot.button("OK").click();

			System.out.println("Test: " + GcovTestC.class.getName());
			GcovTest.createProject(bot, PROJECT_NAME, PROJECT_TYPE);
			GcovTest.populateProject(bot, PROJECT_NAME);
			GcovTest.compileProject(bot, PROJECT_NAME);
		}

			@Test
			public void openGcovFileDetails() throws Exception {
				GcovTest.openGcovFileDetails(bot, PROJECT_NAME);
			}
		
			@Test
			public void openGcovSummary() throws Exception {
				GcovTest.openGcovSummary(bot, PROJECT_NAME, true);
			}
			
			@Test
			public void testGcovSummaryByLaunch() throws Exception {
				GcovTest.openGcovSummaryByLaunch(bot, PROJECT_NAME);
			}
}
