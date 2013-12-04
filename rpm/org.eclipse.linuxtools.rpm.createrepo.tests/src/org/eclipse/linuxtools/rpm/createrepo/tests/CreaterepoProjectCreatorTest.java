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
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProjectCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for project creation with CreaterepoProjectCreatorTest class.
 * This checks if the project creator is working and creating a project with
 * an empty content folder and un-initialized .repo file.
 */
public class CreaterepoProjectCreatorTest {

	private static final String PROJECT_NAME = "createrepo-test-project"; //$NON-NLS-1$
	private static final String REPO_NAME = "createrepo-test-repo.repo"; //$NON-NLS-1$
	private static final String CONTENT_FOLDER = "content"; //$NON-NLS-1$

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
	 * Test to see if the project has been properly created and the contents
	 * initialized.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testProjectContents() throws CoreException, IOException {
		assertTrue(project.exists());
		// 3 = .project + content folder + .repo file
		assertEquals(3, project.members().length);

		// contains content folder and repo file
		assertTrue(project.findMember(CONTENT_FOLDER).exists());
		assertTrue(project.findMember(REPO_NAME).exists());

		IFolder contentFolder = (IFolder) project.findMember(CONTENT_FOLDER);
		// content folder should be empty
		assertEquals(0, contentFolder.members().length);

		IFile repoFile = (IFile) project.findMember(REPO_NAME);
		// repo file should be empty because test did not go through project creation
		// to initialize .repo contents
		assertEquals(repoFile.getContents().available(), 0);
	}

}
