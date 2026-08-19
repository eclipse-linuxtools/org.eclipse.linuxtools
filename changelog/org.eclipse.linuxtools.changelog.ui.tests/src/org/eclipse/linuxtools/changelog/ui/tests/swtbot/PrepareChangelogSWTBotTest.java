/*******************************************************************************
 * Copyright (c) 2010, 2026 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.swtbot;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.linuxtools.changelog.ui.tests.utils.GitTestProject;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * UI tests for "Prepare ChangeLog" (CTRL+ALT+P) and the clipboard magic
 * (CTRL+ALT+V) on a project shared through EGit.
 */
public class PrepareChangelogSWTBotTest extends AbstractSWTBotTest {

    private static final String PROJECT_NAME = "org.eclipse.linuxtools.changelog.tests";
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String CHANGELOG_CONTENT = "2010-12-08  Will Probe  <will@example.com>\n\n"
            + "\t* " + MANIFEST_PATH + ": New file.\n";
    private static final String MANIFEST_CONTENT = "Manifest-Version: 1.0\n"
            + "Bundle-SymbolicName: " + PROJECT_NAME + "\n";

    // EGit's subscriber only knows about repositories that existed when it was
    // first asked for, so share the project once for the whole class.
    private static GitTestProject gitProject;
    private static IProject project;

    @BeforeAll
    public static void createSharedProject() throws Exception {
        gitProject = new GitTestProject(PROJECT_NAME);
        gitProject.addFileToProject("/", "ChangeLog",
                new ByteArrayInputStream(CHANGELOG_CONTENT.getBytes(StandardCharsets.UTF_8)));
        gitProject.addFileToProject("/META-INF", "MANIFEST.MF",
                new ByteArrayInputStream(MANIFEST_CONTENT.getBytes(StandardCharsets.UTF_8)));
        gitProject.commitAll("Initial import");
        gitProject.connect();
        project = gitProject.getTestProject();
    }

    @AfterAll
    public static void deleteSharedProject() throws Exception {
        gitProject.dispose();
    }

    @BeforeEach
    public void setUp() throws Exception {
        ProjectExplorer.openView();
        // Every test starts from a clean checkout of the initial commit
        gitProject.resetHard();
    }

    @AfterEach
    public void tearDown() throws Exception {
        bot.closeAllEditors();
    }

    /**
     * Basic prepare changelog test.
     *
     * @throws Exception
     */
    @Test
    public void canPrepareChangeLog() throws Exception {
        deleteManifest();

        selectChangeLog();
        bot.menu("Prepare Changelog").click(); // Should be unique

        SWTBotEclipseEditor eclipseEditor = waitForChangeLogEditor();
        // make sure expected entry has been added.
        assertTrue(matchHead(eclipseEditor.getText(), "\t* " + MANIFEST_PATH + ":", 3));
    }

    /**
     * Should be able to save changes to ChangeLog file in clipboard.
     * Tests CTRL + ALT + V functionality.
     */
    @Test
    public void canPrepareChangeLogAndSaveChangesInChangeLogFileToClipboard() throws Exception {
        deleteManifest();

        selectChangeLog();
        // CTRL + ALT + P
        bot.activeShell().pressShortcut(Keystrokes.CTRL, Keystrokes.ALT, KeyStroke.getInstance("P"));

        SWTBotEclipseEditor eclipseEditor = waitForChangeLogEditor();
        // make sure expected entry has been added.
        assertTrue(matchHead(eclipseEditor.getText(), "\t* " + MANIFEST_PATH + ":", 3));
        eclipseEditor.selectLine(0); // select first line
        final String expectedFirstLineContent = eclipseEditor.getSelection();

        // save changes to clipboard: CTRL + ALT + V
        eclipseEditor.pressShortcut(Keystrokes.CTRL, Keystrokes.ALT, KeyStroke.getInstance("V"));

        // create and open a new file for pasting
        String pasteFile = "newFile";
        IFile newFile = project.getFile(new Path(pasteFile));
        newFile.create(new ByteArrayInputStream(new byte[0]) /* empty content */, false, null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);

        assertNotNull(project.findMember(new Path(pasteFile)));

        ProjectExplorer.expandProject(projectExplorerViewTree, PROJECT_NAME).expandNode(pasteFile).select()
                .doubleClick();
        bot.waitUntil(Conditions.waitForEditor(withPartName(pasteFile)));
        SWTBotEditor swtBoteditor = bot.activeEditor();
        assertEquals(pasteFile, swtBoteditor.getTitle());
        eclipseEditor = swtBoteditor.toTextEditor();

        // go to beginning of editor
        eclipseEditor.selectRange(0, 0, 0);
        // paste
        eclipseEditor.pressShortcut(Keystrokes.CTRL, KeyStroke.getInstance("V"));
        swtBoteditor.save();
        // make sure proper content was pasted
        assertTrue(matchHead(eclipseEditor.getText(), "\t* " + MANIFEST_PATH + ":", 3));
        eclipseEditor.selectLine(0); // select first line
        final String actualFirstLineContent = eclipseEditor.getSelection();
        assertEquals(expectedFirstLineContent, actualFirstLineContent);
    }

    /**
     * Delete the committed manifest so the working tree has a change for
     * "Prepare ChangeLog" to pick up.
     */
    private void deleteManifest() throws Exception {
        IResource manifest = project.findMember(new Path(MANIFEST_PATH));
        assertNotNull(manifest);
        manifest.delete(true, null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
    }

    /**
     * Select the ChangeLog file of the test project in the Project Explorer.
     */
    private void selectChangeLog() {
        SWTBotTreeItem projectItem = ProjectExplorer.expandProject(projectExplorerViewTree, PROJECT_NAME);
        assertNotNull(projectItem);
        SWTBotTreeItem changeLogItem = ProjectExplorer.getProjectItem(projectItem, "ChangeLog");
        assertNotNull(changeLogItem);
        changeLogItem.select();
    }

    /**
     * Wait for the ChangeLog editor to open and save it, so no "save changes"
     * pop-up gets in the way later.
     */
    private SWTBotEclipseEditor waitForChangeLogEditor() {
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        SWTBotPreferences.TIMEOUT = 3 * 5000;
        try {
            bot.waitUntil(Conditions.waitForEditor(withPartName("ChangeLog")));
        } finally {
            SWTBotPreferences.TIMEOUT = oldTimeout;
        }
        SWTBotEditor swtBoteditor = bot.activeEditor();
        swtBoteditor.save();
        assertEquals("ChangeLog", swtBoteditor.getTitle());
        return swtBoteditor.toTextEditor();
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
