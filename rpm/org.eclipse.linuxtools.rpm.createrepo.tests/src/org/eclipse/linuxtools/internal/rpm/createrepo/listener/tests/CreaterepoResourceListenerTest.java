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
package org.eclipse.linuxtools.internal.rpm.createrepo.listener.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot tests for CreaterepoResourceChangeListener. Simply create a project,
 * close/delete it, and test if the activator is also closed.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreaterepoResourceListenerTest {

	private TestCreaterepoProject testProject;
	private static SWTWorkbenchBot bot;
	private static NullProgressMonitor monitor;

	/**
	 * Initialize the bot.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		bot = new SWTWorkbenchBot();
		monitor = new NullProgressMonitor();
		try {
			bot.shell(ICreaterepoTestConstants.MAIN_SHELL).activate();
		} catch (WidgetNotFoundException e) {
			// cannot activate main shell, continue anyways
		}
	}

	/**
	 * Create a new test project and open the .repo file.
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		testProject = new TestCreaterepoProject();
		assertTrue(testProject.getProject().exists());
		openRepoFile();
	}

	/**
	 * Delete the project when tests are done.
	 *
	 * @throws CoreException
	 */
	@After
	public  void tearDown() throws CoreException {
		testProject.dispose();
		assertFalse(testProject.getProject().exists());
	}

	/**
	 * Test if editor is still open after closing the project.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testCloseProjectCloseEditor() throws CoreException {
		assertFalse(bot.editors().isEmpty());
		assertEquals(1, bot.editors().size());
		assertNotNull(bot.multipageEditorByTitle(ICreaterepoTestConstants.REPO_NAME));
		assertTrue(bot.multipageEditorByTitle(ICreaterepoTestConstants.REPO_NAME).isActive());
		testProject.getProject().close(monitor);
		assertFalse(testProject.getProject().isOpen());
		assertTrue(bot.editors().isEmpty());
	}

	/**
	 * Test if editor is still open after deleting the project.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testDeleteProjectCloseEditor() throws CoreException {
		assertFalse(bot.editors().isEmpty());
		assertEquals(1, bot.editors().size());
		assertNotNull(bot.multipageEditorByTitle(ICreaterepoTestConstants.REPO_NAME));
		assertTrue(bot.multipageEditorByTitle(ICreaterepoTestConstants.REPO_NAME).isActive());
		testProject.dispose();
		assertFalse(testProject.getProject().exists());
		assertTrue(bot.editors().isEmpty());
	}

	/**
	 * Helper method to help setup the test by opening the .repo file.
	 */
	private static void openRepoFile() {
		// open the package explorer view
		bot.menu(ICreaterepoTestConstants.WINDOW).menu(ICreaterepoTestConstants.SHOW_VIEW)
		.menu(ICreaterepoTestConstants.OTHER).click();
		SWTBotShell shell = bot.shell(ICreaterepoTestConstants.SHOW_VIEW);
		shell.activate();
		bot.tree().expandNode(ICreaterepoTestConstants.GENERAL_NODE).select(ICreaterepoTestConstants.NAVIGATOR);
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		SWTBotView view = bot.viewByTitle(ICreaterepoTestConstants.NAVIGATOR);
		view.show();
		// select the .repo file from the package explorer and open it
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
	}

}
