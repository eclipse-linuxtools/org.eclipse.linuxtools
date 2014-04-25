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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorerTreeItemAppearsCondition;
import org.eclipse.linuxtools.changelog.ui.tests.utils.SVNProject;
import org.eclipse.linuxtools.changelog.ui.tests.utils.SVNProjectCreatedCondition;
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
 *
 * UI tests for "Prepare ChangeLog" (CTRL+ALT+P) and the clipboard magic
 * (CTRL+ALT+V).
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class PrepareChangelogSWTBotTest {

    private static SWTWorkbenchBot bot;
    private static SWTBotTree projectExplorerViewTree;
    private SVNProject subversionProject;
    private IProject  project;
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
     * Basic prepare changelog test.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void canPrepareChangeLog() throws Exception {
        // Find manifest file
        IResource manifest = project.findMember(new Path("/META-INF/MANIFEST.MF"));
        assertNotNull(manifest);
        // delete it
        manifest.delete(true, null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);

        // select ChangeLog file
        String teamProviderString = "[changelog/trunk/" + PROJECT_NAME + "]";
        SWTBotTreeItem projectItem = ProjectExplorer.expandProject(projectExplorerViewTree, PROJECT_NAME, teamProviderString);
        SWTBotTreeItem changeLogItem = ProjectExplorer.getProjectItem(projectItem, "ChangeLog");
        changeLogItem.select();
        bot.menu("Prepare ChangeLog").click(); // Should be unique

        long oldTimeout = SWTBotPreferences.TIMEOUT;
        SWTBotPreferences.TIMEOUT = 3 * 5000;
        // Wait for ChangeLog editor to open
        Matcher<?> editorMatcher = allOf(
                IsInstanceOf.instanceOf(IEditorReference.class),
                withPartName("ChangeLog")
                );
        bot.waitUntil(Conditions.waitForEditor((Matcher<IEditorReference>) editorMatcher));
        SWTBotPreferences.TIMEOUT = oldTimeout;

        SWTBotEditor swtBoteditor = bot.activeEditor();
        swtBoteditor.save(); // save to avoid "save changes"-pop-up
        assertEquals("ChangeLog", swtBoteditor.getTitle());
        SWTBotEclipseEditor eclipseEditor = swtBoteditor.toTextEditor();
        // make sure expected entry has been added.
        assertTrue(matchHead(eclipseEditor.getText(), "\t* META-INF/MANIFEST.MF:", 3));
    }

    /**
     * Should be able to save changes to ChangeLog file in clipboard.
     * Tests CTRL + ALT + V functionality.
     *
     * @throws Exception
     */
    @Test
    public void canPrepareChangeLogAndSaveChangesInChangeLogFileToClipboard() throws Exception {
        // Find manifest file
        IResource manifest = project.findMember(new Path("/META-INF/MANIFEST.MF"));
        assertNotNull(manifest);
        // delete it
        manifest.delete(true, null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);

        // select ChangeLog file
        String teamProviderString = "[changelog/trunk/" + PROJECT_NAME + "]";
        SWTBotTreeItem projectItem = ProjectExplorer.expandProject(projectExplorerViewTree, PROJECT_NAME, teamProviderString);
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        SWTBotPreferences.TIMEOUT = 5000;
        bot.waitUntil(new ProjectExplorerTreeItemAppearsCondition(projectExplorerViewTree, PROJECT_NAME, teamProviderString, "ChangeLog"));
        SWTBotPreferences.TIMEOUT = oldTimeout;
        SWTBotTreeItem changeLogItem = ProjectExplorer.getProjectItem(projectItem, "ChangeLog");
        changeLogItem.select();
        // CTRL + ALT + P
        bot.activeShell().pressShortcut(Keystrokes.CTRL, Keystrokes.ALT, KeyStroke.getInstance("P"));

        oldTimeout = SWTBotPreferences.TIMEOUT;
        SWTBotPreferences.TIMEOUT = 3 * 5000;
        // Wait for ChangeLog editor to open
        Matcher<IEditorReference> editorMatcher = allOf(
                IsInstanceOf.instanceOf(IEditorReference.class),
                withPartName("ChangeLog")
                );
        bot.waitUntil(Conditions.waitForEditor(editorMatcher));
        SWTBotEditor swtBoteditor = bot.activeEditor();
        swtBoteditor.save(); // save to avoid "save changes"-pop-up
        assertEquals("ChangeLog", swtBoteditor.getTitle());
        SWTBotEclipseEditor eclipseEditor = swtBoteditor.toTextEditor();
        // make sure expected entry has been added.
        assertTrue(matchHead(eclipseEditor.getText(), "\t* META-INF/MANIFEST.MF:", 3));
        eclipseEditor.selectLine(0); // select first line
        final String expectedFirstLineContent = eclipseEditor.getSelection();

        // save changes to clipboard: CTRL + ALT + V
        eclipseEditor.pressShortcut(Keystrokes.CTRL, Keystrokes.ALT, KeyStroke.getInstance("V"));

        // create and open a new file for pasting
        String pasteFile = "newFile";
        IFile newFile = project.getFile(new Path(pasteFile));
        newFile.create(new ByteArrayInputStream("".getBytes()) /* empty content */, false, null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);

        assertNotNull(project.findMember(new Path(pasteFile)));

        ProjectExplorer.expandProject(projectExplorerViewTree, PROJECT_NAME,
                teamProviderString).expandNode(pasteFile).select().doubleClick();
        //bot.activeShell().pressShortcut(Keystrokes.F3); // open file
        editorMatcher = allOf(
                IsInstanceOf.instanceOf(IEditorReference.class),
                withPartName(pasteFile)
                );
        bot.waitUntil(Conditions.waitForEditor(editorMatcher));
        SWTBotPreferences.TIMEOUT = oldTimeout;
        swtBoteditor = bot.activeEditor();
        assertEquals(pasteFile, swtBoteditor.getTitle());
        eclipseEditor = swtBoteditor.toTextEditor();

        // go to beginning of editor
        eclipseEditor.selectRange(0, 0, 0);
        // paste
        eclipseEditor.pressShortcut(Keystrokes.CTRL, KeyStroke.getInstance("V"));
        swtBoteditor.save();
        // make sure proper content was pasted
        assertTrue(matchHead(eclipseEditor.getText(), "\t* META-INF/MANIFEST.MF:", 3));
        eclipseEditor.selectLine(0); // select first line
        final String actualFirstLineContent = eclipseEditor.getSelection();
        assertEquals(expectedFirstLineContent, actualFirstLineContent);
    }

    /**
     * Determine if first <code>i</code> lines in <code>text</code> contain
     * the string <code>matchText</code>.
     *
     * @param text The text to compare to.
     * @param matchText The match string to look for.
     * @param i The number of lines in text to consider.
     * @return
     *
     * @throws IllegalArgumentException if <code>i</code> is invalid.
     */
    private boolean matchHead(String text, String matchText, int i) throws IllegalArgumentException {
        if ( i < 0 ) {
            throw new IllegalArgumentException();
        }
        String[] lines = text.split("\n");
        if ( lines.length < i ) {
            throw new IllegalArgumentException();
        }
        // arguments appear to be good
        for (int j = 0; j < i; j++) {
            if (lines[j].contains(matchText)) {
                return true;
            }
        }
        return false; // no match
    }

}
