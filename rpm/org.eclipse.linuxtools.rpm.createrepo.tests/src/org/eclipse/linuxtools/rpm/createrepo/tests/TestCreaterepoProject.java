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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProjectNature;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.linuxtools.rpm.createrepo.IRepoFileConstants;

/**
 * A class to create a test createrepo project. This will create the repo file
 * and content folder without having to go through the project wizard. It also
 * initializes the project nature and initializes the repo file's contents to
 * being empty.
 */
public class TestCreaterepoProject {

	/*
	 * Test names for the project and .repo file.
	 */
	public static final String PROJECT_NAME = "createrepo-test-project"; //$NON-NLS-1$
	public static final String REPO_NAME = "createrepo-test-repo.repo"; //$NON-NLS-1$

	private static NullProgressMonitor monitor;
	private IProject project;

	/**
	 * Instantiating class creates project, content folder, and .repo file.
	 *
	 * @throws CoreException
	 */
	public TestCreaterepoProject() throws CoreException {
		monitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(PROJECT_NAME);
		IProjectDescription description = ResourcesPlugin.getWorkspace()
				.newProjectDescription(PROJECT_NAME);
		description.setNatureIds(new String[] {CreaterepoProjectNature.CREATEREPO_NATURE_ID});
		if (!project.exists()) {
			project.create(description, monitor);
		}
		project.open(monitor);
		init();
	}

	/**
	 * Create the content folder and the .repo file.
	 *
	 * @throws CoreException
	 */
	private void init() throws CoreException {
		createFolder(ICreaterepoConstants.CONTENT_FOLDER);
		createFile(REPO_NAME);
	}

	/**
	 * Create a folder in the project.
	 *
	 * @param folderName The name of the folder.
	 * @return The folder that was created.
	 * @throws CoreException
	 */
	public IFolder createFolder(String folderName) throws CoreException {
		IFolder folder = project.getFolder(folderName);
		if (!folder.exists()) {
			folder.create(false, true, monitor);
		}
		return folder;
	}

	/**
	 * Create a file in the project. Initialize empty content.
	 *
	 * @param fileName The name of the file.
	 * @return The file that was created.
	 * @throws CoreException
	 */
	public IFile createFile(String fileName) throws CoreException {
		IFile file = project.getFile(fileName);
		InputStream stream = new ByteArrayInputStream(ICreaterepoConstants.EMPTY_STRING.getBytes());
		if (!file.exists()) {
			file.create(stream, true, monitor);
		}
		return file;
	}

	/**
	 * Delete the project and all its contents.
	 *
	 * @throws CoreException
	 */
	public void dispose() throws CoreException {
		project.delete(true, true, monitor);
	}

	/**
	 * Refresh the project.
	 *
	 * @throws CoreException
	 */
	public void refresh() throws CoreException {
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * Return a new instance of a CreaterepoProject.
	 *
	 * @return A new CreaterepoProject.
	 * @throws CoreException
	 */
	public CreaterepoProject getCreaterepoProject() throws CoreException {
		return new CreaterepoProject(project, project.getFile(REPO_NAME));
	}

	/**
	 * Return the current project instance;
	 *
	 * @return The current project instance.
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Set the file contents of the repofile if it exists, or create a repofile
	 * with the specified contents if it does not exist.
	 *
	 * @param id The repository ID.
	 * @param name The human-readable description of the repository.
	 * @param url The baseurl of the repository.
	 * @throws CoreException
	 */
	public void setRepoFileContents(String id, String name, String url) throws CoreException {
		String contents = String.format("[%s]\n", id); //$NON-NLS-1$
		contents = contents.concat(String.format("%s=%s\n", IRepoFileConstants.NAME, name)); //$NON-NLS-1$
		contents = contents.concat(String.format("%s=%s\n", IRepoFileConstants.BASE_URL, url)); //$NON-NLS-1$
		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		IFile repoFile = project.getFile(REPO_NAME);
		if (repoFile.exists()) {
			repoFile.setContents(stream, true, true, monitor);
		} else {
			repoFile.create(stream, true, monitor);
		}
	}

}
