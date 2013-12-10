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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.rpm.createrepo.Createrepo;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoUtils;
import org.eclipse.linuxtools.rpm.createrepo.tests.CreaterepoProjectTest;
import org.eclipse.linuxtools.rpm.createrepo.tests.ICreaterepoTestConstants;
import org.eclipse.linuxtools.rpm.createrepo.tests.TestCreaterepoProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotMultiPageEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.FrameworkUtil;

/**
 * SWTBot tests for ImportRPMsPage. Import RPMs cannot be tested due to
 * SWTBot not supporting native dialogs (File dialogs).
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreaterepoImportRPMsPageTest {

	private static final String TEST_RPM1 = "eclipse-egit-github-3.0.0-2.fc19.noarch.rpm"; //$NON-NLS-1$
	private static final String TEST_RPM_LOC1 = ICreaterepoTestConstants.RPM_RESOURCE_LOC
			.concat(TEST_RPM1);

	private static TestCreaterepoProject testProject;
	private CreaterepoProject project;
	private static SWTWorkbenchBot bot;
	private SWTBot importPageBot;

	/**
	 * Initialize the test project. Will close the welcome view.
	 *
	 * @throws CoreException
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws CoreException {
		testProject = new TestCreaterepoProject();
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
	}

	/**
	 * Get the CreaterepoProject at the beginning of each test, as
	 * well as import some test RPMs.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Before
	public void setUp() throws CoreException, IOException {
		project = testProject.getCreaterepoProject();
		URL rpmURL = FileLocator.find(FrameworkUtil
				.getBundle(CreaterepoProjectTest.class), new Path(TEST_RPM_LOC1), null);
		File rpmFile = new File(FileLocator.toFileURL(rpmURL).getPath());
		project.importRPM(rpmFile);
		// there should be 1 rpm every setup
		assertEquals(1, project.getRPMs().size());
		initializeImportPage();
	}

	/**
	 * Test out the remove RPMs button.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testRemoveRPMs() throws CoreException {
		// run in UI thread because accessing the tree in the import RPMs page
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Tree tree = importPageBot.widget(WidgetMatcherFactory.widgetOfType(Tree.class));
				// current item count should be 1 (from the imported RPM)
				assertEquals(1, tree.getItemCount());
				importPageBot.button(Messages.ImportRPMsPage_buttonRemoveRPMs).click();
				// not selecting a treeitem should do nothing to the tree contents
				assertEquals(1, tree.getItemCount());
				tree.select(tree.getItem(0));
				importPageBot.button(Messages.ImportRPMsPage_buttonRemoveRPMs).click();
				// item count should be 0 after selecting a tree item and pressing remove
				assertEquals(0, tree.getItemCount());
				try {
					// make sure that the RPM was actually deleted from the project
					assertEquals(0, project.getRPMs().size());
				} catch (CoreException e) {
					fail("Failed to get the RPMs from project"); //$NON-NLS-1$
				}
			}
		});
	}

	/**
	 * Test to see if createrepo executed.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testCreaterepo() throws CoreException {
		// assume that there is creatrepo version of >= 0.9.8
		IStatus validVersion = Createrepo.isCorrectVersion(CreaterepoUtils.findConsole("test").newMessageStream()); //$NON-NLS-1$
		Assume.assumeTrue(validVersion.isOK());
		importPageBot.button(Messages.ImportRPMsPage_buttonCreateRepo).click();
		// make the bot wait until the download job shell closes before proceeding the tests
		bot.waitUntil(Conditions.shellCloses(bot.shell(Messages.Createrepo_jobName)));
		// assert that the content folder has more than just the RPM inside it
		assertTrue(project.getContentFolder().members().length > 1);
		// assert that the repodata folder exists within the content folder
		assertTrue(project.getContentFolder().findMember(ICreaterepoTestConstants.REPODATA_FOLDER).exists());
		// assert that the repomd.xml file was created (successful createrepo execution)
		IFolder repodata = (IFolder) project.getContentFolder().findMember(ICreaterepoTestConstants.REPODATA_FOLDER);
		assertTrue(repodata.findMember(ICreaterepoTestConstants.REPO_MD_NAME).exists());
	}

	/**
	 * Helper method to help setup the test by opening the .repo file.
	 */
	private void initializeImportPage() {
		// open the package explorer view
		bot.menu(ICreaterepoTestConstants.WINDOW).menu(ICreaterepoTestConstants.SHOW_VIEW).menu(ICreaterepoTestConstants.OTHER).click();
		SWTBotShell shell = bot.shell(ICreaterepoTestConstants.SHOW_VIEW);
		shell.activate();
		bot.tree().expandNode(ICreaterepoTestConstants.JAVA_NODE).select(ICreaterepoTestConstants.PACKAGE_EXPLORER);
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		SWTBotView view = bot.viewByTitle(ICreaterepoTestConstants.PACKAGE_EXPLORER);
		view.show();
		// select the repo file from the package explorer and open it
		Composite packageExplorer = (Composite)view.getWidget();
		Tree swtTree = bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), packageExplorer);
		SWTBotTree botTree = new SWTBotTree(swtTree);
		botTree.expandNode(TestCreaterepoProject.PROJECT_NAME).select(TestCreaterepoProject.REPO_NAME);
		bot.menu(ICreaterepoTestConstants.OPEN).click();
		// get a handle on the multipage editor that was opened
		SWTBotMultiPageEditor editor = bot.multipageEditorByTitle(TestCreaterepoProject.REPO_NAME);
		editor.show();
		// 3 = repository form page, metadata form page, repo file
		assertEquals(3, editor.getPageCount());
		// activate the pages to make sure they exist and work
		editor.activatePage(Messages.ImportRPMsPage_title);
		// make sure correct page is active
		assertEquals(Messages.ImportRPMsPage_title, editor.getActivePageTitle());
		importPageBot = editor.bot();
	}

}
