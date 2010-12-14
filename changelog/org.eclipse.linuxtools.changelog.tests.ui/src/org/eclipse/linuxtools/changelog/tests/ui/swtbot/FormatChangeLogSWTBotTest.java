/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.tests.ui.swtbot;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import org.eclipse.core.resources.IProject;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.linuxtools.changelog.tests.ui.utils.ProjectExplorerTreeItemAppearsCondition;
import org.eclipse.linuxtools.changelog.tests.ui.utils.ProjectExplorer;
import org.eclipse.linuxtools.changelog.tests.ui.utils.SVNProject;
import org.eclipse.linuxtools.changelog.tests.ui.utils.SVNProjectCreatedCondition;
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
import org.hamcrest.Matchers;
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
	private IProject  project;
	private SVNProject subversionProject;
	// The name of the test project, we create
	private final String PROJECT_NAME = "org.eclipse.linuxtools.changelog.tests";
	// An available SVN repo
	private final String SVN_PROJECT_URL = "svn://dev.eclipse.org/svnroot/technology/" +
		"org.eclipse.linuxtools/changelog/trunk";
 
	@BeforeClass
	public static void beforeClass() throws Exception {
		// delay click speed
		//System.setProperty("org.eclipse.swtbot.playback.delay", "200");
		bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException e) {
			// ignore
		}
		// Make sure project explorer is open and tree available
		ProjectExplorer.openView();
		projectExplorerViewTree = ProjectExplorer.getTree();
	}
	
	@Before
	public void setUp() throws Exception {
		// Do an SVN checkout of the changelog.tests plugin
		subversionProject = new SVNProject(bot);
		project = subversionProject.setProjectName(PROJECT_NAME).setRepoURL(SVN_PROJECT_URL).checkoutProject();
		bot.waitUntil(new SVNProjectCreatedCondition(PROJECT_NAME));
		ProjectExplorer.openView();
	}
 
	@After
	public void tearDown() throws Exception {
		this.project.delete(true, null);
		// discard existing repo from previous test runs
		try {
			subversionProject.discardRepositoryLocation();
		} catch (WidgetNotFoundException e) {
			// Ignore case if repository not existing
		}
	}

	/**
	 * Simple test for ChangeLog formatting.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void canFormatChangeLogFile() throws Exception {
		// select ChangeLog file
		String teamProviderString = "[changelog/trunk/" + PROJECT_NAME + "]";
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
		Matcher<?> editorMatcher = Matchers.allOf(
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
