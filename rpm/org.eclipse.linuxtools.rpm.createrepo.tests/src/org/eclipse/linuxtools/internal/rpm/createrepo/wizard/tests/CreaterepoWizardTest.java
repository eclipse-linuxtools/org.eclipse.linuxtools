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
package org.eclipse.linuxtools.internal.rpm.createrepo.wizard.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProjectNature;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.linuxtools.rpm.createrepo.IRepoFileConstants;
import org.eclipse.linuxtools.rpm.createrepo.tests.ICreaterepoTestConstants;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot tests for CreaterepoWizard.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreaterepoWizardTest {

	private static final String PROJECT_NAME = "createrepo-test-project"; //$NON-NLS-1$
	private static final String REPO_ID = "createrepo-test-repo"; //$NON-NLS-1$
	private static final String REPO_FILE = REPO_ID.concat(".repo"); //$NON-NLS-1$

	private static final String REPO_WIZARD_NAME = "Test repository for createrepo plugin"; //$NON-NLS-1$
	private static final String REPO_WIZARD_URL = "http://www.example.com/test"; //$NON-NLS-1$
	private static final String REPO_FILE_CONTENTS =
			String.format("[%s]%s=%s%s=%s", REPO_ID, IRepoFileConstants.NAME,  //$NON-NLS-1$
			REPO_WIZARD_NAME, IRepoFileConstants.BASE_URL, REPO_WIZARD_URL);

	private static SWTWorkbenchBot bot;
	private static IWorkspaceRoot root;
	private static NullProgressMonitor monitor;
	private IProject project;

	/**
	 * Setup the bot, monitor and workspace root.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		bot = new SWTWorkbenchBot();
		root = ResourcesPlugin.getWorkspace().getRoot();
		monitor = new NullProgressMonitor();
	}

	/**
	 * Focus on main eclipse platform shell before continue SWTBot tests.
	 */
	@Before
	public void setUp() {
		try {
			bot.shell(ICreaterepoTestConstants.MAIN_SHELL).activate();
		} catch (WidgetNotFoundException e) {
			// cannot activate main shell, continue anyways
		}
	}

	/**
	 * Delete the project and its contents for each test itereation.
	 *
	 * @throws CoreException
	 */
	@After
	public void tearDown() throws CoreException {
		if (project != null && project.exists()) {
			project.delete(true, monitor);
		}
	}

	/**
	 * Go through the project creation wizard process of creating a new
	 * createrepo project.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testCreaterepoWizardProjectCreation() throws CoreException, IOException {
		// go through the process of creating a new createrepo project
		bot.menu(ICreaterepoTestConstants.FILE).menu(ICreaterepoTestConstants.NEW).menu(ICreaterepoTestConstants.OTHER).click();
		SWTBotShell shell = bot.shell(ICreaterepoTestConstants.NEW);
		shell.activate();
		bot.tree().expandNode(ICreaterepoTestConstants.CREATEREPO_CATEGORY).select(ICreaterepoTestConstants.CREATEREPO_PROJECT_WIZARD);
		bot.button(ICreaterepoTestConstants.NEXT_BUTTON).click();
		bot.textWithLabel(ICreaterepoTestConstants.PROJECT_NAME_LABEL).setText(PROJECT_NAME);
		bot.button(ICreaterepoTestConstants.NEXT_BUTTON).click();
		bot.textWithLabel(Messages.CreaterepoNewWizardPageTwo_labelID).setText(REPO_ID);
		bot.textWithLabel(Messages.CreaterepoNewWizardPageTwo_labelName).setText(REPO_WIZARD_NAME);
		bot.textWithLabel(Messages.CreaterepoNewWizardPageTwo_labelURL).setText(REPO_WIZARD_URL);
		bot.button(ICreaterepoTestConstants.FINISH_BUTTON).click();

		// verify that project has been initialized properly
		project = root.getProject(PROJECT_NAME);
		assertTrue(project.exists());
		assertTrue(project.hasNature(CreaterepoProjectNature.CREATEREPO_NATURE_ID));
		// 3 = .project + content folder + .repo file
		assertEquals(3, project.members().length);

		// contains the content folder and repo file
		assertTrue(project.findMember(ICreaterepoConstants.CONTENT_FOLDER).exists());
		assertTrue(project.findMember(REPO_FILE).exists());

		// content folder has nothing in it
		IFolder contentFolder = (IFolder) project.findMember(ICreaterepoConstants.CONTENT_FOLDER);
		assertEquals(0, contentFolder.members().length);

		// get the created .repo file contents
		IFile repoFile = (IFile) project.findMember(REPO_FILE);
		// repo file should not be empty
		assertNotEquals(repoFile.getContents().available(), 0);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(repoFile.getContents()));
		String line;
		while ((line = br.readLine()) != null) {
			// disregards newline
			sb.append(line);
		}
		assertEquals(REPO_FILE_CONTENTS, sb.toString());
	}

}
