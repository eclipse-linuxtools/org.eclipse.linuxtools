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
package org.eclipse.linuxtools.internal.rpm.createrepo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * Utility class to help create a createrepo project.
 */
public class CreaterepoProjectCreator {

	/**
	 * Create a createrepo project given a project name and the progress
	 * monitor. The new project will contain an empty repodata folder.
	 *
	 * @param projectName The name of the project.
	 * @param locationPath The location path of the project
	 * @param monitor The progress monitor.
	 * @return The newly created project.
	 * @throws CoreException Thrown when creating a project fails.
	 */
	public static IProject create(String projectName, IPath locationPath,
			String repoName, IProgressMonitor monitor) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		IProjectDescription description = ResourcesPlugin.getWorkspace()
				.newProjectDescription(projectName);
		if (!Platform.getLocation().equals(locationPath)) {
			description.setLocation(locationPath);
		}
		description.setNatureIds(new String[] {CreaterepoProjectNature.CREATEREPO_NATURE_ID});
		project.create(description, monitor);
		project.open(monitor);
		IFile repoFile = project.getFile(repoName);
		InputStream stream = new ByteArrayInputStream(ICreaterepoConstants.EMPTY_STRING.getBytes());
		if (!repoFile.exists()) {
			repoFile.create(stream, true, monitor);
		}
		return project;
	}

}
