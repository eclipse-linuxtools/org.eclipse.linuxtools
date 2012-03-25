/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 ************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * Utility class to ease creation of RPM projects.
 * 
 */
public class RPMProjectCreator {
	private RPMProjectLayout layout;
	private IProject latest;

	/**
	 * Creates the utility class and sets the layout that will be used.
	 * 
	 * @param layout
	 *            The layout of the projects to be created.
	 */
	public RPMProjectCreator(RPMProjectLayout layout) {
		this.layout = layout;
	}
	
	/**
	 * Creates the utility class with the default(RPMBuild) layout.
	 */
	public RPMProjectCreator() {
		this(RPMProjectLayout.RPMBUILD);
	}

	/**
	 * Creates a project with the given name in the given location.
	 * @param projectName The name of the project.
	 * @param projectPath The parent location of the project.
	 * @param monitor Progress monitor to report back status.
	 */
	public void create(String projectName, IPath projectPath,
			IProgressMonitor monitor) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(projectName);
			IProjectDescription description = ResourcesPlugin.getWorkspace()
					.newProjectDescription(project.getName());
			if (!Platform.getLocation().equals(projectPath))
				description.setLocation(projectPath);
			description
					.setNatureIds(new String[] { IRPMConstants.RPM_NATURE_ID });
			project.create(description, monitor);
			monitor.worked(10);
			project.open(monitor);
			if (layout.equals(RPMProjectLayout.RPMBUILD)) {
				createDirs(monitor, project);
			}
			latest=project;
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the latest project created.
	 * @return The last created project.
	 */
	public IProject getLatestProject(){
		return latest;
	}

	private void createDirs(IProgressMonitor monitor, IProject project)
			throws CoreException {
		project.getFolder(IRPMConstants.SPECS_FOLDER).create(true, true,
				monitor);
		project.getFolder(IRPMConstants.SOURCES_FOLDER).create(true, true,
				monitor);
		IFolder buildFolder = project.getFolder(IRPMConstants.BUILD_FOLDER);
		buildFolder.create(true, true, monitor);
		buildFolder.setHidden(true);
		project.getFolder(IRPMConstants.RPMS_FOLDER)
				.create(true, true, monitor);
		project.getFolder(IRPMConstants.SRPMS_FOLDER).create(true, true,
				monitor);
	}
}
