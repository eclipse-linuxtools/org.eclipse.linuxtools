/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.swtbot;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ChangeLogTestProject;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * UI tests for "ChangeLog Entry" (CTRL+ALT+C).
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class AddChangelogEntrySWTBotTest {

	private static SWTWorkbenchBot bot;
	private static SWTBotTree projectExplorerViewTree;
	private ChangeLogTestProject  project;
	private static final String OFFSET_MARKER = "<-- SELECT -->";
	// The name of the test project, we create
	private final String PROJECT_NAME = "changelog-java-project";

	@BeforeClass
	public static void beforeClass() throws Exception {
		// delay click speed; with this turned on things get flaky
		//System.setProperty("org.eclipse.swtbot.playback.delay", "200");
		bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
		} catch (WidgetNotFoundException e) {
			// ignore
		}
		// Make sure project explorer is open and tree available
		ProjectExplorer.openView();
		projectExplorerViewTree = ProjectExplorer.getTree();
	}

	@Before
	public void setUp() throws Exception {
		// Create an empty test project
		project = new ChangeLogTestProject(PROJECT_NAME);
		project.addJavaNature(); // make it a Java project
	}

	@After
	public void tearDown() throws Exception {
		this.project.getTestProject().delete(true, null);
	}

	/**
	 * ChangeLog editor should pop-up if inside an active editor
	 * and a ChangeLog file exists in the project. Tests the CTRL+ALT+c
	 * shortcut.
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void canAddChangeLogEntryUsingShortCutIfSourceIsActive() throws Exception {
		// Add a Java source file
		String sourceCode = "package src;\n" +
			"public class JavaTest {\n" +
				"public static void main(String args[]) {\n" +
					"//" + OFFSET_MARKER + "\n" +
					"System.out.println(\"Hello World!\");\n" +
				"}\n" +
				"}\n";

		assertNull(project.getTestProject().findMember( new Path(
		"/src/JavaTest.java")));
		InputStream newFileInputStream = new ByteArrayInputStream(
				sourceCode.getBytes());
		project.addFileToProject("/src", "JavaTest.java", newFileInputStream);

		// Add a changelog file
		newFileInputStream = new ByteArrayInputStream(
				"".getBytes());
		project.addFileToProject("/", "ChangeLog", newFileInputStream);

		assertNotNull(project.getTestProject().findMember( new Path(
			"/src/JavaTest.java")));
		assertNotNull(project.getTestProject().findMember( new Path(
		"/ChangeLog")));

		// Open JavaTest.java in an editor
		projectExplorerViewTree.expandNode(PROJECT_NAME).expandNode("src").expandNode("JavaTest.java").doubleClick();

		Matcher<?> editorMatcher = allOf(
				IsInstanceOf.instanceOf(IEditorReference.class),
				withPartName("JavaTest.java")
				);
		// Wait for Java editor to open
		bot.waitUntil(Conditions.waitForEditor((Matcher<IEditorReference>) editorMatcher));
		SWTBotEditor swtBoteditor = bot.editorByTitle("JavaTest.java");
		SWTBotEclipseEditor eclipseEditor = swtBoteditor.toTextEditor();
		eclipseEditor.selectLine(getLineOfOffsetMarker(sourceCode));

		// Press: CTRL + ALT + c
		eclipseEditor.pressShortcut(Keystrokes.CTRL, Keystrokes.ALT, KeyStroke.getInstance("C"));
		// Wait for ChangeLog editor to open
		editorMatcher = allOf(
				IsInstanceOf.instanceOf(IEditorReference.class),
				withPartName("ChangeLog")
				);
		bot.waitUntil(Conditions.waitForEditor((Matcher<IEditorReference>) editorMatcher));
		swtBoteditor = bot.activeEditor();
		swtBoteditor.save(); // save to avoid "save changes"-pop-up
		assertEquals("ChangeLog", swtBoteditor.getTitle());
		eclipseEditor = swtBoteditor.toTextEditor();
		// make sure expected entry has been added.
		assertTrue(eclipseEditor.getText().contains("\t* src/JavaTest.java (main):"));
	}

	/**
	 * ChangeLog editor should pop-up if inside an active editor
	 * and a ChangeLog file exists in the project. Tests the "Edit" => "ChangeLog Entry"
	 * menu item.
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void canAddChangeLogEntryUsingEditMenuIfSourceIsActive() throws Exception {
		// Add a Java source file
		String sourceCode = "package src;\n" +
			"public class JavaTest {\n" +
				"public static void main(String args[]) {\n" +
					"//" + OFFSET_MARKER + "\n" +
					"System.out.println(\"Hello World!\");\n" +
				"}\n" +
				"}\n";

		assertNull(project.getTestProject().findMember( new Path(
		"/src/JavaTest.java")));
		InputStream newFileInputStream = new ByteArrayInputStream(
				sourceCode.getBytes());
		project.addFileToProject("/src", "JavaTest.java", newFileInputStream);

		// Add a changelog file
		newFileInputStream = new ByteArrayInputStream(
				"".getBytes());
		project.addFileToProject("/", "ChangeLog", newFileInputStream);

		assertNotNull(project.getTestProject().findMember( new Path(
			"/src/JavaTest.java")));
		assertNotNull(project.getTestProject().findMember( new Path(
		"/ChangeLog")));

		// Open JavaTest.java in an editor
		SWTBotTreeItem projectItem = projectExplorerViewTree.expandNode(PROJECT_NAME);
		projectItem.expandNode("src").expandNode("JavaTest.java").doubleClick();

		Matcher<?> editorMatcher = allOf(
				IsInstanceOf.instanceOf(IEditorReference.class),
				withPartName("JavaTest.java")
				);
		// Wait for editor to open
		bot.waitUntil(Conditions.waitForEditor((Matcher<IEditorReference>) editorMatcher));
		SWTBotEditor swtBoteditor = bot.editorByTitle("JavaTest.java");
		SWTBotEclipseEditor eclipseEditor = swtBoteditor.toTextEditor();
		eclipseEditor.selectLine(getLineOfOffsetMarker(sourceCode));

		// Click menu item.
		bot.menu("Edit").menu("ChangeLog Entry").click();
		// Wait for ChangeLog editor to open
		editorMatcher = allOf(
				IsInstanceOf.instanceOf(IEditorReference.class),
				withPartName("ChangeLog")
				);
		bot.waitUntil(Conditions.waitForEditor((Matcher<IEditorReference>) editorMatcher));
		swtBoteditor = bot.activeEditor();
		swtBoteditor.save(); // save to avoid "save changes"-pop-up
		assertEquals("ChangeLog", swtBoteditor.getTitle());
		eclipseEditor = swtBoteditor.toTextEditor();
		// make sure expected entry has been added.
		assertTrue(eclipseEditor.getText().contains("\t* src/JavaTest.java (main):"));
	}

	/**
	 * FIXME: Disable menu item instead of showing it and doing nothing.
	 *
	 * This test throws WidgetNotFountException (i.e. shouldn't open any editor).
	 */
	 @Test(expected=WidgetNotFoundException.class)
	public void shouldDoNothingIfNoEditorIsActive() {
		assertNull(project.getTestProject().findMember( new Path("/src/dummy")));
		try {
			project.addFileToProject("src", "dummy", new ByteArrayInputStream("".getBytes()));
		} catch (CoreException e) {
			fail("Could not add /src/dummy file to project");
		}
		assertNotNull(project.getTestProject().findMember( new Path("/src/dummy")));
		// Make sure we are in the project explorer view and no editors are open
		bot.closeAllEditors();
		projectExplorerViewTree.expandNode(PROJECT_NAME).expandNode("src");
		// Try to create ChangeLog
		bot.menu("Edit").menu("ChangeLog Entry").click();
		// Don't wait 5 secs
		long oldTimeout = SWTBotPreferences.TIMEOUT;
		SWTBotPreferences.TIMEOUT = 1000; // give it a full second :)
		bot.activeEditor();
		SWTBotPreferences.TIMEOUT = oldTimeout;
	}

	/**
	 * @param The source text.
	 * @return The index of the first line containing the OFFSET_MARKER string in sourceCode.
	 *		   -1 if not found.
	 */
	private int getLineOfOffsetMarker(String sourceCode) {
		// select line containing the println() statement.
		int offset = -1, i = 0;
		for (String line: sourceCode.split("\n")) {
			if (line.indexOf(OFFSET_MARKER) >= 0) {
				offset = i;
				break;
			}
			i++;
		}
		return offset;
	}
}
