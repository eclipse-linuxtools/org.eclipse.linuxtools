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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.tests.ICreaterepoTestConstants;
import org.eclipse.linuxtools.rpm.createrepo.tests.TestCreaterepoProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotMultiPageEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot tests for RepoFormEditor.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreaterepoRepoFormEditorTest {

	private static TestCreaterepoProject testProject;
	private static SWTWorkbenchBot bot;

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
	}

	/**
	 * Delete the project when tests are done.
	 *
	 * @throws CoreException
	 */
	@AfterClass
	public static void tearDownAfterClass() throws CoreException {
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
		// open the package explorer view
		bot.menu(ICreaterepoTestConstants.WINDOW).menu(ICreaterepoTestConstants.SHOW_VIEW).menu(ICreaterepoTestConstants.OTHER).click();
		SWTBotShell shell = bot.shell(ICreaterepoTestConstants.SHOW_VIEW);
		shell.activate();
		bot.tree().expandNode(ICreaterepoTestConstants.GENERAL_NODE).select(ICreaterepoTestConstants.NAVIGATOR);
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		SWTBotView view = bot.viewByTitle(ICreaterepoTestConstants.NAVIGATOR);
		view.show();
		// select the repo file from the package explorer and open it
		Composite packageExplorer = (Composite)view.getWidget();
		assertNotNull(packageExplorer);
		Tree swtTree = bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), packageExplorer);
		assertNotNull(swtTree);
		SWTBotTree botTree = new SWTBotTree(swtTree);
		botTree.expandNode(ICreaterepoTestConstants.PROJECT_NAME).getNode(ICreaterepoTestConstants.REPO_NAME)
			.contextMenu(ICreaterepoTestConstants.OPEN).click();
		// get a handle on the multipage editor that was opened
		SWTBotMultiPageEditor editor = bot.multipageEditorByTitle(ICreaterepoTestConstants.REPO_NAME);
		editor.show();
		// 3 = repository form page, metadata form page, repo file
		assertEquals(3, editor.getPageCount());
		// activate the pages to make sure they exist and work
		editor.activatePage(Messages.MetadataPage_title);
		editor.activatePage(ICreaterepoTestConstants.REPO_NAME);
		editor.activatePage(Messages.ImportRPMsPage_title);
	}

}
