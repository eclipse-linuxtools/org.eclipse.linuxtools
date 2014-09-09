/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.form.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotMultiPageEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot tests for RepoFormEditor.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class RepoFormEditorTest {

    private static TestCreaterepoProject testProject;
    private static SWTWorkbenchBot bot;
    private static SWTBotView navigator;

    /**
     * Initialize the test project.
     *
     * @throws CoreException
     */
    @BeforeClass
    public static void setUpBeforeClass() throws CoreException {
        testProject = new TestCreaterepoProject();
        assertTrue(testProject.getProject().exists());
        bot = new SWTWorkbenchBot();
        try {
            bot.shell(ICreaterepoTestConstants.MAIN_SHELL).activate();
        } catch (WidgetNotFoundException e) {
            // cannot activate main shell, continue anyways
        }
        TestUtils.openResourcePerspective(bot);
        navigator = TestUtils.enterProjectFolder(bot);
    }

    /**
     * Delete the project when tests are done.
     *
     * @throws CoreException
     */
    @AfterClass
    public static void tearDownAfterClass() throws CoreException {
        TestUtils.exitProjectFolder(bot, navigator);
        testProject.dispose();
        assertFalse(testProject.getProject().exists());
    }

    /**
     * Test if the multi page editor is properly created. Make sure there are 3
     * pages (Repository, Metadata, repo file), and that all of them can be switched
     * to.
     */
    @Test
    public void testFormEditorCreation() {
        SWTBotMultiPageEditor editor = TestUtils.openRepoFile(bot, navigator);
        // activate the pages to make sure they exist and work
        editor.activatePage(Messages.MetadataPage_title);
        editor.activatePage(ICreaterepoTestConstants.REPO_NAME);
        editor.activatePage(Messages.ImportRPMsPage_title);
    }

}
