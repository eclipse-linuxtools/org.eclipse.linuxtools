/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.swtbot;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ChangeLogTestProject;
import org.eclipse.linuxtools.changelog.ui.tests.utils.ProjectExplorer;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * UI test for "Prepare ChangeLog" when project not shared.
 *
 */
public class DisabledPrepareChangelogSWTBotTest extends AbstractSWTBotTest {

    private final String projectName = "not-shared";
    private ChangeLogTestProject project;

    @Before
    public void setUp() throws Exception {
        project = new ChangeLogTestProject(projectName);
    }

    @After
    public void tearDown() throws Exception {
        this.project.getTestProject().delete(true, null);
    }

    /**
     * If the project is not shared by any CVS or SVN team provider, "Prepare ChangeLog"
     * should be disabled.
     *
     * @throws Exception
     */
    @Test
    public void cannotPrepareChangeLogOnNonCVSOrSVNProject() throws Exception {
        assertNull(project.getTestProject().findMember(new Path("/ChangeLog")));

        final String changeLogContent = "2010-12-08  Will Probe  <will@example.com>\n\n" +
            "\t* path/to/some/non-existing/file.c: New file.\n";
        project.addFileToProject("/", "ChangeLog", new ByteArrayInputStream(changeLogContent.getBytes()));

        assertNotNull(project.getTestProject().findMember(new Path("/ChangeLog")));

        // select ChangeLog file
        String teamProviderString = "n/a";
        SWTBotTreeItem projectItem = ProjectExplorer.expandProject(projectExplorerViewTree, projectName, teamProviderString);
        SWTBotTreeItem changeLogItem = ProjectExplorer.getProjectItem(projectItem, "ChangeLog");
        changeLogItem.select();
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        SWTBotPreferences.TIMEOUT = 100;
        try {
            bot.menu("Prepare ChangeLog").click(); // Should be disabled (throws exception)
            fail("'Prepare ChangeLog' should be disabled");
        } catch (TimeoutException e) {
            assertTrue(e.getMessage().contains("The widget with mnemonic 'Prepare ChangeLog' was not enabled."));
        }
        SWTBotPreferences.TIMEOUT = oldTimeout;
    }

}
