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

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ChangeLogTestProject;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorerTreeItemAppearsCondition;
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
 * UI tests for formatting ChangeLog files.
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class FormatChangeLogSWTBotTest {

	private static SWTWorkbenchBot bot;
	private static SWTBotTree projectExplorerViewTree;
	private ChangeLogTestProject project;
	// The name of the test project, we create
	private final String PROJECT_NAME = "org.eclipse.linuxtools.changelog.ui.formattestproject";

	@BeforeClass
	public static void beforeClass() throws Exception {
		// delay click speed
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
		project = new ChangeLogTestProject(PROJECT_NAME);
		ProjectExplorer.openView();
	}

	@After
	public void tearDown() throws Exception {
		this.project.getTestProject().delete(true, null);
	}

	/**
	 * Simple test for ChangeLog formatting.
	 *
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void canFormatChangeLogFile() throws Exception {
		// add a ChangeLog file
		assertNull(project.getTestProject().findMember(new Path("/ChangeLog")));
		final String changelogContent = "2010-12-14  Severin Gehwolf  <sgehwolf@redhat.com>\n\n" +
			"\tAdded org.eclipse.linuxtools.changelog.tests.ui plug-in.\n" +
			"\t* .classpath: New file.\n" +
			"\t* .project: New file.\n" +
			"\t* .settings/org.eclipse.jdt.core.prefs: New file.\n" +
			"\t* build.properties: New file.\n" +
			"\t* src/log4j.xml: New file.\n" +
			"\t* src/org/eclipse/linuxtools/changelog/tests/ui/utils/ContextMenuHelper.java: New file.\n" +
			"\t* src/org/eclipse/linuxtools/changelog/tests/ui/utils/ProjectExplorer.java: New file.\n" +
			"\t* src/org/eclipse/linuxtools/changelog/tests/ui/utils/ProjectExplorerTreeItemAppearsCondition.java: New file.\n";
		project.addFileToProject("/", "ChangeLog", new ByteArrayInputStream(changelogContent.getBytes()));
		assertNotNull(project.getTestProject().findMember(new Path("/ChangeLog")));

		// select ChangeLog file
		String teamProviderString = "n/a";
		SWTBotTreeItem projectItem = ProjectExplorer.expandProject(projectExplorerViewTree, PROJECT_NAME, teamProviderString);
		long oldTimeout = SWTBotPreferences.TIMEOUT;
		SWTBotPreferences.TIMEOUT = 5000;
		bot.waitUntil(new ProjectExplorerTreeItemAppearsCondition(projectExplorerViewTree, PROJECT_NAME, teamProviderString, "ChangeLog"));
		SWTBotPreferences.TIMEOUT = oldTimeout;
		SWTBotTreeItem changeLogItem = ProjectExplorer.getProjectItem(projectItem, "ChangeLog");
		changeLogItem.doubleClick(); // should open ChangeLog file

		oldTimeout = SWTBotPreferences.TIMEOUT;
		SWTBotPreferences.TIMEOUT = 3 * 5000;
		// Wait for ChangeLog editor to open
		Matcher<?> editorMatcher = allOf(
				IsInstanceOf.instanceOf(IEditorReference.class),
				withPartName("ChangeLog")
				);
		bot.waitUntil(Conditions.waitForEditor((Matcher<IEditorReference>) editorMatcher));
		SWTBotEditor swtBoteditor = bot.activeEditor();
		assertEquals("ChangeLog", swtBoteditor.getTitle());

		SWTBotEclipseEditor swtBotEclipseEditor = swtBoteditor.toTextEditor();

		// Add two extra lines after the first date line
		swtBotEclipseEditor.insertText(1, 0, "\n\n");
		// Should have 3 empty lines between date-line and first file entry
		swtBotEclipseEditor.selectRange(1, 0, 3);

		// format: ESC CTRL+F
		swtBotEclipseEditor.pressShortcut(Keystrokes.ESC);
		swtBotEclipseEditor.pressShortcut(Keystrokes.CTRL, KeyStroke.getInstance("F"));
		swtBoteditor.save();
		String secondLine = swtBotEclipseEditor.getTextOnLine(1);
		String thirdLine = swtBotEclipseEditor.getTextOnLine(2);
		// FIXME: These assertions are lame.
		assertEquals("", secondLine);
		assertTrue(!"".equals(thirdLine));
	}

}
