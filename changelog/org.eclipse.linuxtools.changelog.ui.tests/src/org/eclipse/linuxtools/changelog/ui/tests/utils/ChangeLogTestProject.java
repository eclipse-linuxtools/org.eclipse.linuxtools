/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import java.io.InputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Test fixture for the changelog plug-in tests.
 * 
 * Functionality:
 *  - Create a new project in the current workspace
 *
 */
public class ChangeLogTestProject {

	private IProject testProject;
	
	/**
	 * Create a new ChangelogTestProject
	 * 
	 * @param projectName The name of the project to be created.
	 * @throws Exception
	 */
	public ChangeLogTestProject(String projectName) throws Exception {
		testProject = createNewProject(projectName);
	}
	
	/**
	 * @return the testProject
	 */
	public IProject getTestProject() {
		return testProject;
	}

	/**
	 * @param testProject the testProject to set
	 */
	public void setTestProject(IProject testProject) {
		this.testProject = testProject;
	}
	
	/**
	 * Adds a file to this project at the specified <code>path</code>
	 * and the provided <code>filename</code>. If segments of <code>path</code>
	 * do not exist, they will be created.
	 * 
	 * @param destPath The path relative to the project (use '/' as path separator).
	 * @param filename The name of the to be created file
	 * @param fileInputStream A stream to the new files content.
	 */
	public IFile addFileToProject(String destPath, String filename, InputStream fileInputStream) throws CoreException {
		String[] pathSegments = destPath.split("/");
		
		IContainer parent = this.testProject;
		for (String segment: pathSegments) {
			if (segment.equals("")) {
				continue; // ignore
			}
			IResource segmentResource = parent.findMember(new Path(IPath.SEPARATOR + segment));
			if (segmentResource == null) {
				// create folder
				IFolder newFolder = parent.getFolder(new Path(segment));
				newFolder.create(false, true, null);
				parent = newFolder;
			} else {
				// resource existed
				parent = (IContainer)segmentResource;
			}
		}
		// Finally add the file
		IFile newFile = parent.getFile(new Path(filename));
		if (fileInputStream == null) {
			throw new IllegalStateException("fileInputStream must not be null");
		}
		newFile.create(fileInputStream, false, null);
		
		// refresh project
		this.testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		
		return newFile;
	}
	
	/**
	 * Add the Java nature to this project. I.e. make it a Java project.
	 */
	public IJavaProject addJavaNature() throws CoreException {
		IProjectDescription description = this.testProject.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = JavaCore.NATURE_ID;
		description.setNatureIds(newNatures);
		this.testProject.setDescription(description, null);
		return JavaCore.create(this.testProject);
	}

	/**
	 * Add the C nature to this project. I.e. make it a C project.
	 * @throws CoreException 
	 */
	public void addCNature() throws CoreException {
		this.testProject = CCorePlugin.getDefault().createCDTProject(
				testProject.getDescription(), testProject, null);
	}
	
	/**
	 * Add the C++ nature to this project. I.e. make it a C++ project.
	 * @throws CoreException
	 */
	public void addCCNature() throws CoreException {
		addCNature();
		CCorePlugin.getDefault().convertProjectFromCtoCC(testProject, null);
	}

	/**
	 * Create a new Eclipse project in the current workspace
	 * 
	 * @param name
	 * @throws CoreException if project creation fails for some reason.
	 * @return The newly created project.
	 */
	private IProject createNewProject(String name) throws CoreException {
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
		.getProject(name);
		newProject.create(null);
		newProject.open(null); // needs to be open
		return newProject;
	}
}
