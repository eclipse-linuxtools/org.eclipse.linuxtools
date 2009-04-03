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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Bundle;

public abstract class AbstractTest extends TestCase {
	private static final String BIN_DIR = "Debug"; //$NON-NLS-1$
	protected ICProject proj;
	
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected ICProject createProjectAndBuild(Bundle bundle, String projname) throws CoreException, URISyntaxException,
			InvocationTargetException, InterruptedException, IOException {
		ICProject proj = createProject(bundle, projname);
		buildProject(proj);
		return proj;
	}

	public void buildProject(ICProject proj) throws CoreException {
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		final IProject curProject = proj.getProject();
		ISchedulingRule rule = wsp.getRuleFactory().buildRule();
		Job buildJob = new Job("project build job") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				try {
					curProject.build(
							IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
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

		curProject.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	public ICProject createProject(Bundle bundle, String projname)
			throws CoreException, URISyntaxException, IOException,
			InvocationTargetException, InterruptedException {
		ICProject proj = CProjectHelper.createCProject(projname , BIN_DIR);
		URL location = FileLocator.find(bundle, new Path("resources/" + projname), null); //$NON-NLS-1$
		File testDir = new File(FileLocator.toFileURL(location).toURI());
		
		ImportOperation op = new ImportOperation(proj.getProject().getFullPath(), testDir, FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {
			public String queryOverwrite(String pathString) {
				return ALL;
			}			
		});
		op.setCreateContainerStructure(false);
		op.run(null);
		return proj;
	}
	
	protected void deleteProject(ICProject cproject) {
		CProjectHelper.delete(cproject);
	}
	
	protected ILaunchConfiguration createConfiguration(IProject proj) {
		ILaunchConfiguration config = null;
		try {
			String projectName = proj.getName();
			// hard-coded to work around getBinaries() returning empty issue
			IResource bin = proj.findMember(new Path(BIN_DIR).append(projectName));
			String binPath = bin.getProjectRelativePath().toString();
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					getLaunchManager()
							.generateUniqueLaunchConfigurationNameFrom(
									projectName));
	
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, binPath);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
			wc.setMappedResources(new IResource[] {bin, proj});
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
			
			// Make launch run in foreground
			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
			
			setProfileAttributes(wc);
	
			config = wc.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}
	
	protected abstract ILaunchConfigurationType getLaunchConfigType();
	
	protected abstract void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) throws CoreException;
}
