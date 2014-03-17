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
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.ICreaterepoTestConstants;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.TestCreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.TestUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
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

	private static SWTWorkbenchBot bot;
	private static NullProgressMonitor monitor;
	private static SWTBotView navigator;
	private TestCreaterepoProject testProject;

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
		TestUtils.openResourcePerspective(bot);
	}

	/**
	 * Create a new test project and open the .repo file.
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		testProject = new TestCreaterepoProject();
		navigator = TestUtils.enterProjectFolder(bot);
		assertTrue(testProject.getProject().exists());
		TestUtils.openRepoFile(bot, navigator);
	}

	/**
	 * Delete the project when tests are done.
	 *
	 * @throws CoreException
	 */
	@After
	public  void tearDown() throws CoreException {
		TestUtils.exitProjectFolder(bot, navigator);
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

}
