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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.rpm.createrepo.Createrepo;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
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

	private static final String TEST_RPM_LOC1 = ICreaterepoTestConstants.RPM_RESOURCE_LOC
			.concat(ICreaterepoTestConstants.RPM1);

	private static TestCreaterepoProject testProject;
	private static SWTWorkbenchBot bot;
	private static NullProgressMonitor monitor;
	private static SWTBotView navigator;
	private CreaterepoProject project;
	private SWTBot importPageBot;

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
		monitor = new NullProgressMonitor();
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
	 * Get the CreaterepoProject at the beginning of each test, as
	 * well as import some test RPMs.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Before
	public void setUp() throws CoreException, IOException {
		project = testProject.getCreaterepoProject();
		assertNotNull(project);
		URL rpmURL = FileLocator.find(FrameworkUtil
				.getBundle(CreaterepoProjectTest.class), new Path(TEST_RPM_LOC1), null);
		File rpmFile = new File(FileLocator.toFileURL(rpmURL).getPath());
		assertTrue(rpmFile.exists());
		project.importRPM(rpmFile);
		// there should be 1 rpm every setup
		assertEquals(1, project.getRPMs().size());
		initializeImportPage();
	}

	/**
	 * Test out the remove RPMs button.
	 */
	@Test
	public void testRemoveRPMs() {
		// run in UI thread because accessing the tree in the import RPMs page
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Tree tree = importPageBot.widget(WidgetMatcherFactory.widgetOfType(Tree.class));
				assertNotNull(tree);
				// current item count should be 1 (from the imported RPM)
				assertEquals(1, tree.getItemCount());
				importPageBot.button(Messages.ImportRPMsPage_buttonRemoveRPMs).click();
				// not selecting a treeitem should do nothing to the tree contents
				assertEquals(1, tree.getItemCount());
				// select the first item
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
		IStatus validVersion = Createrepo.isCorrectVersion();
		Assume.assumeTrue(validVersion.isOK());
		importPageBot.button(Messages.ImportRPMsPage_buttonCreateRepo).click();
		// make the bot wait until the download job shell closes before proceeding the tests
		importPageBot.waitUntil(Conditions.shellCloses(bot.shell(Messages.Createrepo_jobName)));
		// assert that the content folder has more than just the RPM inside it
		assertTrue(project.getContentFolder().members().length > 1);
		// assert that the repodata folder exists within the content folder
		assertTrue(project.getContentFolder().findMember(ICreaterepoTestConstants.REPODATA_FOLDER).exists());
		// assert that the repomd.xml file was created (successful createrepo execution)
		IFolder repodata = (IFolder) project.getContentFolder().findMember(ICreaterepoTestConstants.REPODATA_FOLDER);
		assertTrue(repodata.findMember(ICreaterepoTestConstants.REPO_MD_NAME).exists());
	}

	/**
	 * Test if deleting/adding an RPM into content folder updates the RPM list.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testResourceChangeListener() throws CoreException, IOException {
		// delete the contents of the content folder
		for (IResource resource : project.getContentFolder().members()) {
			resource.delete(true, monitor);
		}
		// run in UI thread because accessing the tree in the import RPMs page
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Tree tree = importPageBot.widget(WidgetMatcherFactory.widgetOfType(Tree.class));
				assertNotNull(tree);
				// check items in tree are gone
				assertEquals(0, tree.getItemCount());
			}
		});
		// import a file again into the content folder
		URL rpmURL = FileLocator.find(FrameworkUtil
				.getBundle(CreaterepoProjectTest.class), new Path(TEST_RPM_LOC1), null);
		final File rpmFile = new File(FileLocator.toFileURL(rpmURL).getPath());
		assertTrue(rpmFile.exists());
		project.importRPM(rpmFile);
		// run in UI thread because accessing the tree in the import RPMs page
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Tree tree = importPageBot.widget(WidgetMatcherFactory.widgetOfType(Tree.class));
				assertNotNull(tree);
				// check if items are in tree
				assertEquals(1, tree.getItemCount());
				assertEquals(rpmFile.getName(), tree.getItem(0).getText());
			}
		});
	}

	/**
	 * Helper method to help setup the test by opening the .repo file.
	 */
	private void initializeImportPage() {
		SWTBotMultiPageEditor editor = TestUtils.openRepoFile(bot, navigator);
		// activate repository page
		editor.activatePage(Messages.ImportRPMsPage_title);
		// make sure correct page is active
		assertEquals(Messages.ImportRPMsPage_title, editor.getActivePageTitle());
		importPageBot = editor.bot();
	}

}
