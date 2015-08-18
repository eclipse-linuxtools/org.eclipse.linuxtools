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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ContextMenuHelper;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorerTreeItemAppearsCondition;
import org.eclipse.linuxtools.changelog.ui.tests.utils.SVNProject;
import org.eclipse.linuxtools.changelog.ui.tests.utils.SVNProjectCreatedCondition;
import org.eclipse.linuxtools.changelog.ui.tests.utils.TableAppearsCondition;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * UI tests for creating changelogs from SVN history (commit messages).
 *
 */
public class CreateChangeLogFromHistorySWTBotTest extends AbstractSWTBotTest {

    private IProject project;
    private SVNProject subversionProject;
    // The name of the test project, we create
    private final String PROJECT_NAME = "org.eclipse.linuxtools.changelog.tests";
    // An available SVN repo
    private final String SVN_PROJECT_URL = "svn://dev.eclipse.org/svnroot/technology/" +
        "org.eclipse.linuxtools/changelog/trunk";

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
     * Create changelog from SVN history (commit messages).
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void canPrepareChangeLogFromSVNHistory() throws Exception {
        // select ChangeLog file
        String teamProviderString = "[changelog/trunk/" + PROJECT_NAME + "]";
        SWTBotTreeItem projectItem = ProjectExplorer.expandProject(projectExplorerViewTree, PROJECT_NAME, teamProviderString);
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        SWTBotPreferences.TIMEOUT = 5000;
        bot.waitUntil(new ProjectExplorerTreeItemAppearsCondition(projectExplorerViewTree, PROJECT_NAME, teamProviderString, "ChangeLog"));
        SWTBotPreferences.TIMEOUT = oldTimeout;
        SWTBotTreeItem changeLogItem = ProjectExplorer.getProjectItem(projectItem, "ChangeLog");
        changeLogItem.select();

        // open history for ChangeLog file
        clickOnShowHistory(projectExplorerViewTree);
        SWTBot historyViewBot = bot.viewByTitle("History").bot();
        // wait for SVN revision table to appear
        oldTimeout = SWTBotPreferences.TIMEOUT;
        SWTBotPreferences.TIMEOUT = 3 * 5000;
        historyViewBot.waitUntil(new TableAppearsCondition());
        SWTBotPreferences.TIMEOUT = oldTimeout;
        SWTBotTable historyTable = historyViewBot.table();
        historyTable.select(0); // select the first row

        // right-click => Generate Changelog...
        clickOnGenerateChangeLog(historyTable);
        SWTBotShell shell = bot.shell("Generate ChangeLog").activate();

        SWTBot generateChangelogBot = shell.bot();
        generateChangelogBot.radio("Clipboard").click();
        generateChangelogBot.button("OK").click();

        // create and open a new file for pasting
        String pasteFile = "newFile";
        IFile newFile = project.getFile(new Path(pasteFile));
        newFile.create(new ByteArrayInputStream("".getBytes()) /* empty content */, false, null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);

        assertNotNull(project.findMember(new Path(pasteFile)));

        ProjectExplorer.expandProject(projectExplorerViewTree, PROJECT_NAME,
                teamProviderString).expandNode(pasteFile).select().doubleClick();
        Matcher<IEditorReference> editorMatcher = allOf(
                IsInstanceOf.instanceOf(IEditorReference.class),
                withPartName(pasteFile)
                );
        bot.waitUntil(Conditions.waitForEditor(editorMatcher));
        oldTimeout = SWTBotPreferences.TIMEOUT;
        SWTBotPreferences.TIMEOUT = oldTimeout;
        SWTBotEditor swtBoteditor = bot.activeEditor();
        assertEquals(pasteFile, swtBoteditor.getTitle());
        SWTBotEclipseEditor eclipseEditor = swtBoteditor.toTextEditor();

        // go to beginning of editor
        eclipseEditor.selectRange(0, 0, 0);
        // paste
        eclipseEditor.pressShortcut(Keystrokes.CTRL, KeyStroke.getInstance("V"));
        swtBoteditor.save();
        // make sure some changelog like text was pasted
        String text = eclipseEditor.getText();
        assertFalse(text.isEmpty());
    }

    /**
     * Helper method for right-clicking => Generate ChangeLog in History
     * view table.
     *
     * Pre: History view table row selected.
     */
    private void clickOnGenerateChangeLog(SWTBotTable table) {
        String menuItem = "Generate ChangeLog...";
        ContextMenuHelper.clickContextMenu(table, menuItem);
    }

    /**
     * Helper method for right-click => Team => Show History.
     */
    private void clickOnShowHistory(SWTBotTree tree) {
        String menuItem = "Team";
        String subMenuItem = "Show History";
        ContextMenuHelper.clickContextMenu(tree, menuItem, subMenuItem);
    }

}
