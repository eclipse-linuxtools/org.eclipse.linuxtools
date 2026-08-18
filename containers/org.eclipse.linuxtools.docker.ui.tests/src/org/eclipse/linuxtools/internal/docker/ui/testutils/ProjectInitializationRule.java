/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit {@link Extension} to initialize a {@link IProject} in the test
 * workspace, by reading the {@link RunWithProject} annotation on the test
 * method.
 */
public class ProjectInitializationRule implements BeforeEachCallback {

	private IProject project = null;

	public IProject getProject() {
		return this.project;
	}

	@Override
	public void beforeEach(final ExtensionContext context) throws Exception {
		final String projectName = getProjectName(context);
		final IWorkspace junitWorkspace = ResourcesPlugin.getWorkspace();
		this.project = getTargetWorkspaceProject(getSampleProjectPath(projectName), junitWorkspace);
	}

	/**
	 * @return the name of the project to setup before running the JUnit test.
	 */
	private static String getProjectName(final ExtensionContext context) {
		final RunWithProject runWithProjectMethodAnnotation = context.getRequiredTestMethod()
				.getAnnotation(RunWithProject.class);
		if (runWithProjectMethodAnnotation != null) {
			return runWithProjectMethodAnnotation.value();
		}
		final RunWithProject runWithProjectTypeAnnotation = context.getRequiredTestClass()
				.getAnnotation(RunWithProject.class);
		if (runWithProjectTypeAnnotation != null) {
			return runWithProjectTypeAnnotation.value();
		}
		return fail("No '@RunWithProject' annotation found while running test "
				+ context.getRequiredTestClass().getName() + "." + context.getRequiredTestMethod().getName());
	}

	/**
	 * @param projectName
	 *            the name of the project
	 * @return the {@link IPath} to the given project, in an absolute form.
	 */
	public static IPath getSampleProjectPath(final String projectName) {
		if (System.getProperty("user.dir") != null) {
			return new Path(System.getProperty("user.dir")).append("projects").append(projectName).makeAbsolute();
		}
		return fail("The sample project was not found in the launcher workspace under name '" + projectName + "'");
	}

	/**
	 * Creates or opens the project in the target/JUnit workspace.
	 *
	 * @param projectSourcePath
	 *            the absolute path to the project
	 * @param targetWorkspace
	 *            the target workspace in which the project should be created
	 * @return the project
	 * @throws CoreException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public static IProject getTargetWorkspaceProject(final IPath projectSourcePath, final IWorkspace targetWorkspace)
			throws CoreException, InvocationTargetException, InterruptedException {
		final IPath dotProjectPath = projectSourcePath.addTrailingSeparator().append(".project");
		final IProjectDescription description = targetWorkspace.loadProjectDescription(dotProjectPath);
		final String projectName = description.getName();
		final IProject project = targetWorkspace.getRoot().getProject(projectName);
		if (project.exists()
				&& !targetWorkspace.getRoot().getFile(project.getFile(".project").getFullPath()).exists()) {
			project.delete(true, null);
		} else if (project.exists() && !project.isOpen()) {
			project.open(null);
		}
		if (!project.exists()) {
			createProject(description, projectName, targetWorkspace, project);
		}
		// Always re-import, not just when the project is first created: tests are
		// free to delete resources out of the workspace copy (and some do, to check
		// what happens when a Dockerfile goes missing), and without this they would
		// stay deleted for every later test that asks for the same project.
		importProjectContent(projectSourcePath, project);
		return project;
	}

	private static void importProjectContent(final IPath projectSourcePath, final IProject project)
			throws InvocationTargetException, InterruptedException {
		final SyncFileSystemStructureProvider syncFileSystemStructureProvider = new SyncFileSystemStructureProvider.Builder(
				projectSourcePath, project.getLocation()).ignoreRelativeSourcePaths(".svn", ".git", "target", "bin")
						.build();
		final List<File> filesToImport = syncFileSystemStructureProvider.getChildren(projectSourcePath.toFile());
		if (filesToImport != null && !filesToImport.isEmpty()) {
			final ImportOperation operation = new ImportOperation(project.getFullPath(), projectSourcePath.toFile(),
					syncFileSystemStructureProvider, pathString -> IOverwriteQuery.YES, filesToImport);
			operation.setContext(null);
			// need to overwrite modified files
			operation.setOverwriteResources(true);
			operation.setCreateContainerStructure(false);
			operation.run(null);
		}
	}

	/**
	 * @param description
	 * @param projectName
	 * @param workspace
	 * @param project
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	static void createProject(final IProjectDescription description, final String projectName,
			final IWorkspace workspace, final IProject project)
			throws InvocationTargetException, OperationCanceledException, CoreException, InterruptedException {
		// import from file system

		// import project from location copying files - use default project
		// location for this workspace
		// if location is null, project already exists in this location or
		// some error condition occurred.
		final IProjectDescription desc = workspace.newProjectDescription(projectName);
		desc.setBuildSpec(description.getBuildSpec());
		desc.setComment(description.getComment());
		desc.setDynamicReferences(description.getDynamicReferences());
		desc.setNatureIds(description.getNatureIds());
		desc.setReferencedProjects(description.getReferencedProjects());
		try {
			project.create(desc, null);
			project.open(IResource.BACKGROUND_REFRESH, null);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
		buildProject(project);
	}

	/**
	 * Builds the given project
	 *
	 * @param project
	 * @throws CoreException
	 * @throws OperationCanceledException
	 * @throws InterruptedException
	 */
	public static void buildProject(final IProject project)
			throws CoreException, OperationCanceledException, InterruptedException {
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
	}
}
