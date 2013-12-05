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
package org.eclipse.linuxtools.rpm.createrepo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProjectCreator;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Tests for CreaterepoProject.
 */
public class CreaterepoProjectTest {

	private static final String PROJECT_NAME = "createrepo-test-project"; //$NON-NLS-1$
	private static final String REPO_NAME = "createrepo-test-repo.repo"; //$NON-NLS-1$

	private static final String TEST_RPM1 = "eclipse-egit-github-3.0.0-2.fc19.noarch.rpm"; //$NON-NLS-1$
	private static final String TEST_RPM_LOC1 = ICreaterepoTestConstants.RPM_RESOURCE_LOC
			.concat(TEST_RPM1);
	private static final String TEST_RPM2 = "hello-2.8-1.fc19.src.rpm"; //$NON-NLS-1$
	private static final String TEST_RPM_LOC2 = ICreaterepoTestConstants.RPM_RESOURCE_LOC
			.concat(TEST_RPM2);

	private static IWorkspaceRoot root;
	private static NullProgressMonitor monitor;
	private IProject project;

	/**
	 * Initialize workspace root and progress monitor.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		root = ResourcesPlugin.getWorkspace().getRoot();
		monitor = new NullProgressMonitor();
	}

	/**
	 * Create the project using CreaterepoProjectCreator.
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		if (project == null || !project.exists()) {
			project = CreaterepoProjectCreator.create(PROJECT_NAME, root.getLocation(), REPO_NAME, monitor);
		}
	}

	/**
	 * Forcefully delete the project if it exists.
	 *
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (project != null && project.exists()) {
			project.delete(true, monitor);
		}
	}

	/**
	 * Test if initializing createrepo project is successful. This means that
	 * content folder is initialized but not created (done by wizard, import, or
	 * execute commands) and that .repo file exists.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testInitialize() throws CoreException {
		assertTrue(project.exists());
		CreaterepoProject createrepoProject = new CreaterepoProject(project, (IFile)project.findMember(REPO_NAME));
		// content folder is defined, but not created (wizard does that)
		assertTrue(createrepoProject.getContentFolder() != null);
		assertFalse(createrepoProject.getContentFolder().exists());
		// repo file is found and exists
		assertTrue(createrepoProject.getRepoFile() != null);
		assertTrue(createrepoProject.getRepoFile().exists());
		assertEquals(REPO_NAME, createrepoProject.getRepoFile().getName());
	}

	/**
	 * Test if initializing createrepo project is successful
	 * without specifying .repo file.
	 *
	 * @throws CoreException
	 * @throws BackingStoreException
	 */
	@Test
	public void testInitializeNoRepoFileSpecfied() throws CoreException, BackingStoreException {
		assertTrue(project.exists());
		// repo file will be found rather than initialized
		CreaterepoProject createrepoProject = new CreaterepoProject(project);
		// content folder is defined, but not created (wizard does that)
		assertTrue(createrepoProject.getContentFolder() != null);
		assertFalse(createrepoProject.getContentFolder().exists());
		// repo file is found and exists
		assertTrue(createrepoProject.getRepoFile() != null);
		assertTrue(createrepoProject.getRepoFile().exists());
		assertEquals(REPO_NAME, createrepoProject.getRepoFile().getName());
	}

	/**
	 * Test if importing RPMs from external source successfully saves into
	 * the "content" folder.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testImportRPM() throws CoreException, IOException {
		CreaterepoProject createrepoProject = new CreaterepoProject(project);

		// test for file
		URL rpmURL = FileLocator.find(FrameworkUtil
				.getBundle(CreaterepoProjectTest.class), new Path(TEST_RPM_LOC1), null);
		File rpmFile = new File(FileLocator.toFileURL(rpmURL).getPath());
		createrepoProject.importRPM(rpmFile);
		assertTrue(createrepoProject.getContentFolder() != null);
		assertTrue(createrepoProject.getContentFolder().exists());
		assertEquals(1, createrepoProject.getContentFolder().members().length);
		assertTrue(createrepoProject.getContentFolder().findMember(TEST_RPM1).exists());

		// test for duplicate file
		rpmURL = FileLocator.find(FrameworkUtil
				.getBundle(CreaterepoProjectTest.class), new Path(TEST_RPM_LOC1), null);
		rpmFile = new File(FileLocator.toFileURL(rpmURL).getPath());
		createrepoProject.importRPM(rpmFile);
		assertEquals(1, createrepoProject.getContentFolder().members().length);
		assertTrue(createrepoProject.getContentFolder().findMember(TEST_RPM1).exists());

		// test for new file
		rpmURL = FileLocator.find(FrameworkUtil
				.getBundle(CreaterepoProjectTest.class), new Path(TEST_RPM_LOC2), null);
		rpmFile = new File(FileLocator.toFileURL(rpmURL).getPath());
		createrepoProject.importRPM(rpmFile);
		assertEquals(2, createrepoProject.getContentFolder().members().length);
		assertTrue(createrepoProject.getContentFolder().findMember(TEST_RPM2).exists());
	}

	/**
	 * Test if getting the RPMs is successful.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testGetRPMs() throws CoreException, IOException {
		CreaterepoProject createrepoProject = new CreaterepoProject(project);
		URL rpmURL = FileLocator.find(FrameworkUtil
				.getBundle(CreaterepoProjectTest.class), new Path(TEST_RPM_LOC1), null);
		File rpmFile = new File(FileLocator.toFileURL(rpmURL).getPath());
		createrepoProject.importRPM(rpmFile);
		rpmURL = FileLocator.find(FrameworkUtil
				.getBundle(CreaterepoProjectTest.class), new Path(TEST_RPM_LOC2), null);
		rpmFile = new File(FileLocator.toFileURL(rpmURL).getPath());
		createrepoProject.importRPM(rpmFile);

		List<IResource> rpms = createrepoProject.getRPMs();
		assertEquals(2, rpms.size());
	}

	/**
	 * Simple test execution of createrepo. This checks to see if the "content" folder
	 * was created while executing and that the execution is successful if repomd.xml was created
	 * under the repodata folder.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testSimpleExecute() throws CoreException {
		CreaterepoProject createrepoProject = new CreaterepoProject(project);
		assertTrue(!createrepoProject.getContentFolder().exists());
		IStatus status = createrepoProject.createrepo(CreaterepoUtils.findConsole("test").newMessageStream()); //$NON-NLS-1$

		// check if  executing has an OK status and that content folder is created with the repodata contents
		assertEquals(Status.OK_STATUS, status);
		assertTrue(createrepoProject.getContentFolder().exists());
		assertTrue(createrepoProject.getContentFolder().members().length > 0);

		// check if the repodata folder exists and repomd.xml exists within it
		assertTrue(createrepoProject.getContentFolder().findMember(
				ICreaterepoTestConstants.REPODATA_FOLDER).exists());
		IFolder repodataFolder = (IFolder) createrepoProject.getContentFolder()
				.findMember(ICreaterepoTestConstants.REPODATA_FOLDER);
		// repodata should have at least more than 1 file: repomd.xml + archives
		assertTrue(repodataFolder.members().length > 1);
		assertTrue(repodataFolder.findMember(ICreaterepoTestConstants.REPO_MD_NAME)
				.exists());
	}

}
