package org.eclipse.linuxtools.cdt.autotools.ui.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestToolActions {
	private static SWTWorkbenchBot	bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		// Close the Welcome view
		bot.viewByTitle("Welcome").close();
		// Turn off automatic building by default
		bot.menu("Window").menu("Preferences").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();
		bot.tree().expandNode("General").select("Workspace");
		SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
		if (buildAuto != null && buildAuto.isChecked())
			buildAuto.click();
		bot.button("Apply").click();
		// Ensure that the C/C++ perspective is chosen automatically
		// and doesn't require user intervention
		bot.tree().expandNode("General").select("Perspectives");
		SWTBotRadio radio = bot.radio("Always open");
		if (radio != null && !radio.isSelected())
			radio.click();
		bot.button("OK").click();
		bot.menu("File").menu("New").menu("Project...").click();
		 
		shell = bot.shell("New Project");
		shell.activate();
		bot.tree().expandNode("C/C++").select("C Project");
		bot.button("Next >").click();
 
		bot.textWithLabel("Project name:").setText("GnuProject1");
		bot.tree().expandNode("GNU Autotools").select("Hello World ANSI C Autotools Project");
 
		bot.button("Finish").click();
	}
 
	@Test
	// Verify we can access the aclocal tool
	public void canAccessAclocal() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		path = path.append("aclocal.m4");
		File f = new File(path.toOSString());
		assertTrue(!f.exists());
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Aclocal").click();
		SWTBotShell shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking aclocal in.*GnuProject1.*aclocal --help.*Usage: aclocal.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we still don't have an aclocal.m4 file yet
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		// Now lets run aclocal for our hello world project which hasn't had any
		// autotool files generated yet.
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Aclocal").click();
		shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.button("OK").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking aclocal in.*GnuProject1.*aclocal.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we now have an aclocal.m4 file created
		f = new File(path.toOSString());
		assertTrue(f.exists());
	}

	@Test
	// Verify we can access the autoconf tool
	public void canAccessAutoconf() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		path = path.append("configure");
		File f = new File(path.toOSString());
		assertTrue(!f.exists());
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoconf").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking autoconf in.*GnuProject1.*autoconf.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we now have a configure script
		f = new File(path.toOSString());
		assertTrue(f.exists());
		// Now lets delete the configure file and run autoconf from the project explorer
		// menu directly from the configure.ac file.
		assertTrue(f.delete());
		view = bot.viewByTitle("Project Explorer");
		SWTBotTreeItem node = view.bot().tree().expandNode("GnuProject1").getNode("configure.ac");
		node.setFocus();
		node.select().contextMenu("Invoke Autoconf").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking autoconf in.*GnuProject1.*autoconf.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we now have a configure script again
		f = new File(path.toOSString());
		assertTrue(f.exists());
	}
	
	@Test
	// Verify we can access the aclocal tool
	public void canAccessAutomake() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		IPath path2 = path.append("src/Makefile.in");
		path = path.append("Makefile.in");
		File f = new File(path.toOSString());
		assertTrue(!f.exists());
		File f2 = new File(path2.toOSString());
		assertTrue(!f2.exists());
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Automake").click();
		SWTBotShell shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking automake in.*GnuProject1.*automake --help.*Usage:.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we still don't have Makefile.in files yet
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		f2 = new File(path2.toOSString());
		assertTrue(!f2.exists());
		// Now lets run automake for our hello world project which hasn't had any
		// Makefile.in files generated yet.
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Automake").click();
		shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--add-missing"); // need this to successfully run here
		bot.text(1).typeText("Makefile src/Makefile");
		bot.button("OK").click();
		bot.sleep(2000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking automake in.*GnuProject1.*automake --add-missing Makefile src/Makefile.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we now have Makefile.in files created
		f = new File(path.toOSString());
		assertTrue(f.exists());
		f2 = new File(path2.toOSString());
		assertTrue(f2.exists());
	}
	
	@AfterClass
	public static void sleep() {
		bot.sleep(4000);
	}

}
