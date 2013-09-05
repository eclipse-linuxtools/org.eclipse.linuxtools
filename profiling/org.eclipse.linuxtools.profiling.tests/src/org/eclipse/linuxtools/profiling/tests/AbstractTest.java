/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Intel Corporation - build job code
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.linuxtools.internal.profiling.tests.CProjectHelper;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public abstract class AbstractTest {
	private static final String BIN_DIR = "Debug"; //$NON-NLS-1$
	private static final String IMPORTED_SOURCE_FILE = "primeTest.c"; //$NON-NLS-1$
	protected ICProject proj;

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Create a CDT project outside the default workspace.
	 *
	 * @param bundle			The plug-in bundle.
	 * @param projname			The name of the project.
	 * @param absProjectPath	Absolute path to the directory to which the project should be mapped
	 * 							outside the workspace.
	 * @return					A new external CDT project.
	 * @throws CoreException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	protected IProject createExternalProject(Bundle bundle,
			final String projname, final Path absProjectPath)
					throws CoreException, URISyntaxException, IOException,
					InvocationTargetException, InterruptedException {

		IProject externalProject;
		// Turn off auto-building
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription wspDesc = workspace.getDescription();
		wspDesc.setAutoBuilding(false);
		workspace.setDescription(wspDesc);

		// Create external project
		IWorkspaceRoot root = workspace.getRoot();
		externalProject = root.getProject(projname);
		IProjectDescription description = workspace
				.newProjectDescription(projname);
		URI fileProjectURL = new URI("file://" + absProjectPath.toString());
		description.setLocationURI(fileProjectURL);
		externalProject = CCorePlugin.getDefault().createCDTProject(
				description, externalProject, new NullProgressMonitor());
		assertNotNull(externalProject);
		externalProject.open(null);

		try {
			// CDT opens the Project with BACKGROUND_REFRESH enabled which causes the
			// refresh manager to refresh the project 200ms later. This Job interferes
			// with the resource change handler firing see: bug 271264
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		} catch (Exception e) {
			// Ignore
		}
		assertTrue(externalProject.isOpen());

		// Import boiler-plate files which can then be built and profiled
		URL location = FileLocator.find(bundle, new Path(
				"resources/" + projname), null); //$NON-NLS-1$
		File testDir = new File(FileLocator.toFileURL(location).toURI());
		ImportOperation op = new ImportOperation(externalProject.getFullPath(),
				testDir, FileSystemStructureProvider.INSTANCE,
				new IOverwriteQuery() {
			public String queryOverwrite(String pathString) {
				return ALL;
			}
		});
		op.setCreateContainerStructure(false);
		op.run(null);

		IStatus status = op.getStatus();
		if (!status.isOK()) {
			throw new CoreException(status);
		}
		// Make sure import went well
		assertNotNull(externalProject.findMember(new Path(IMPORTED_SOURCE_FILE)));

		// Index the project
		IIndexManager indexMgr = CCorePlugin.getIndexManager();
		indexMgr.joinIndexer(IIndexManager.FOREVER, new NullProgressMonitor());

		// These natures must be enabled at this point to continue
		assertTrue(externalProject
				.isNatureEnabled(ScannerConfigNature.NATURE_ID));
		assertTrue(externalProject
				.isNatureEnabled(ManagedCProjectNature.MNG_NATURE_ID));

		return externalProject;
	}

	/**
	 * Create and build a project outside the default workspace
	 *
	 * @param bundle			The plug-in bundle.
	 * @param projname			The name of the project.
	 * @param absProjectPath	Absolute path to the directory to which the project should be mapped
	 * 							outside the workspace.
	 * @return					A new external CDT project with binaries built.
	 * @throws CoreException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	protected IProject createExternalProjectAndBuild(Bundle bundle,
			String projname, Path absProjectPath) throws CoreException,
			URISyntaxException, InvocationTargetException,
			InterruptedException, IOException {
		IProject proj = createExternalProject(bundle, projname, absProjectPath);
		buildProject(proj);
		return proj;
	}

	protected ICProject createProjectAndBuild(Bundle bundle, String projname)
			throws CoreException, URISyntaxException,
			InvocationTargetException, InterruptedException, IOException {
		ICProject proj = createProject(bundle, projname);
		buildProject(proj);
		return proj;
	}

	// Wrapper to allow ICProject parameters, since buildProject doesn't really use
	// ICProject specifics.
	protected void buildProject(ICProject project) throws CoreException {
		buildProject(project.getProject());
	}

	protected void buildProject(IProject proj) throws CoreException {
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		final IProject curProject = proj;
		ISchedulingRule rule = wsp.getRuleFactory().buildRule();
		Job buildJob = new Job("project build job") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					curProject.build(
							IncrementalProjectBuilder.FULL_BUILD, null);
				} catch (CoreException e) {
					fail(e.getStatus().getMessage());
				} catch (OperationCanceledException e) {
					fail(NLS.bind(Messages.getString("AbstractTest.Build_cancelled"), curProject.getName(), e.getMessage())); //$NON-NLS-1$
				}
				return Status.OK_STATUS;
			}
		};
		buildJob.setRule(rule);

		buildJob.schedule();

		try {
			buildJob.join();
		} catch (InterruptedException e) {
			fail(NLS.bind(Messages.getString("AbstractTest.Build_interrupted"), curProject.getName(), e.getMessage())); //$NON-NLS-1$
		}

		IStatus status = buildJob.getResult();
		if (status.getCode() != IStatus.OK) {
			fail(NLS.bind(Messages.getString("AbstractTest.Build_failed"), curProject.getName(), status.getMessage())); //$NON-NLS-1$
		}

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				curProject.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
		};

		wsp.run(runnable, wsp.getRoot(), IWorkspace.AVOID_UPDATE, null);
	}

	protected ICProject createProject(Bundle bundle, String projname)
			throws CoreException, URISyntaxException, IOException,
			InvocationTargetException, InterruptedException {
		// Turn off auto-building
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription desc = wsp.getDescription();
		desc.setAutoBuilding(false);
		wsp.setDescription(desc);

		ICProject proj = CProjectHelper.createCProject(projname, BIN_DIR);
		URL location = FileLocator.find(bundle, new Path("resources/" + projname), null); //$NON-NLS-1$
		File testDir = new File(FileLocator.toFileURL(location).toURI());

		IProject project = proj.getProject();
		// Add these natures before project is imported due to #273079
		ManagedCProjectNature.addManagedNature(project, null);
		ScannerConfigNature.addScannerConfigNature(project);

		ImportOperation op = new ImportOperation(project.getFullPath(), testDir, FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {
			public String queryOverwrite(String pathString) {
				return ALL;
			}
		});
		op.setCreateContainerStructure(false);
		op.run(null);

		IStatus status = op.getStatus();
		if (!status.isOK()) {
			throw new CoreException(status);
		}

		// Index the project
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.reindex(proj);
		indexManager.joinIndexer(IIndexManager.FOREVER, new NullProgressMonitor());

		// These natures must be enabled at this point to continue
		assertTrue(project.isNatureEnabled(ScannerConfigNature.NATURE_ID));
		assertTrue(project.isNatureEnabled(ManagedCProjectNature.MNG_NATURE_ID));

		return proj;
	}

	protected void deleteProject(final ICProject cproject) throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				CProjectHelper.delete(cproject);
			}
		}, null);
	}

	protected ILaunchConfiguration createConfiguration(IProject proj) throws CoreException {
		String projectName = proj.getName();
		String binPath = "";

		ILaunchConfigurationType configType = getLaunchConfigType();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
				getLaunchManager()
				.generateLaunchConfigurationName(
						projectName));

		if (proj.getLocation()==null) {
			IFileStore fileStore = null;
			try {
				fileStore = EFS.getStore(new URI(proj.getLocationURI() + BIN_DIR + IPath.SEPARATOR + projectName));
			} catch (URISyntaxException e) {
				fail(NLS.bind(Messages.getString("AbstractTest.No_binary"), projectName)); //$NON-NLS-1$
			}
			if ((fileStore instanceof LocalFile)) {
				fail(NLS.bind(Messages.getString("AbstractTest.No_binary"), projectName)); //$NON-NLS-1$
			}
			binPath = EFSExtensionManager.getDefault().getPathFromURI(proj.getLocationURI())+ BIN_DIR + IPath.SEPARATOR + proj.getName();
		} else {
			IResource bin = proj.findMember(new Path(BIN_DIR).append(projectName));
			if (bin == null) {
				fail(NLS.bind(Messages.getString("AbstractTest.No_binary"), projectName)); //$NON-NLS-1$
			}
			binPath = bin.getProjectRelativePath().toString();
			wc.setMappedResources(new IResource[] {bin, proj});
		}

		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, binPath);
		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);

		// Make launch run in foreground
		wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);

		setProfileAttributes(wc);

		ILaunchConfiguration config = wc.doSave();
		return config;
	}

	protected abstract ILaunchConfigurationType getLaunchConfigType();

	protected abstract void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) throws CoreException;

}
