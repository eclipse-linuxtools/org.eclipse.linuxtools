package org.eclipse.linuxtools.internal.gcov.test;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	org.eclipse.linuxtools.internal.gcov.test.GcovTestC.CreateProject.class,
	org.eclipse.linuxtools.internal.gcov.test.GcovTestC.PopulateProject.class,
	org.eclipse.linuxtools.internal.gcov.test.GcovTestC.CompileProject.class,
	org.eclipse.linuxtools.internal.gcov.test.GcovTestC.OpenGcovFileDetails.class,
	org.eclipse.linuxtools.internal.gcov.test.GcovTestC.OpenGcovSummary.class
})
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
				// ignore
			}

			bot.perspectiveByLabel("C/C++").activate();
			bot.captureScreenshot(PROJECT_NAME + ".beforeClass.2.jpg");
			// Turn off automatic building by default
			bot.menu("Window").menu("Preferences").click();
			SWTBotShell shell = bot.shell("Preferences");
			shell.activate();
			bot.tree().expandNode("General").select("Workspace");
			SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
			if (buildAuto != null && buildAuto.isChecked())
				buildAuto.click();
			bot.sleep(1000);
			bot.button("Apply").click();
			bot.button("OK").click();
		}

		@RunWith(SWTBotJunit4ClassRunner.class)
		public static class CreateProject {
			@Test
			public void test() {
				System.out.println("Test: " + getClass().getName());
				GcovTest.createProject(bot, PROJECT_NAME, PROJECT_TYPE);
			}
		}

		@RunWith(SWTBotJunit4ClassRunner.class)
		public static class PopulateProject {
			@Test
			public void test() throws Exception {
				GcovTest.populateProject(bot, PROJECT_NAME);
			}
		}

		@RunWith(SWTBotJunit4ClassRunner.class)
		public static class CompileProject {
			@Test
			public void test() throws Exception {
				GcovTest.compileProject(bot, PROJECT_NAME);
			}
		}
		
		@RunWith(SWTBotJunit4ClassRunner.class)
		public static class OpenGcovFileDetails {
			@Test
			public void test() throws Exception {
				GcovTest.openGcovFileDetails(bot, PROJECT_NAME);
			}
		}
		
		@RunWith(SWTBotJunit4ClassRunner.class)
		public static class OpenGcovSummary {
			@Test
			public void test() throws Exception {
				GcovTest.openGcovSummary(bot, PROJECT_NAME, true);
			}
		}
		
}
