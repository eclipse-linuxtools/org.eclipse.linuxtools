/*******************************************************************************
 * Copyright (c) 2005, 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.utils.RPM;
import org.eclipse.linuxtools.rpm.core.utils.RPMBuild;

/**
 * Basic RPM projects operations handler.
 *
 */
public class RPMProject {

	private IProject project;
	private IProjectConfiguration rpmConfig;

	/**
	 * Creates the rpm project for the given IProject and layout.
	 * 
	 * @param project The Eclipse project this RPMProject is represented by.
	 * @param projectLayout The layout of the rpm project
	 * @throws CoreException Thrown only in the RPMbuild layout case if a problem with some of the folders exist.
	 */
	public RPMProject(IProject project, RPMProjectLayout projectLayout)
			throws CoreException {
		this.project = project;
		switch (projectLayout) {
		case FLAT:
			rpmConfig = new FlatBuildConfiguration(this.project);
			break;
		case RPMBUILD:
		default:
			rpmConfig = new RPMBuildConfiguration(this.project);
			break;
		}
	}

	public IProjectConfiguration getConfiguration() {
		return rpmConfig;
	}

	public IResource getSpecFile() {
		IContainer specsFolder = getConfiguration().getSpecsFolder();
		IResource file = null;
		SpecfileVisitor specVisitor = new SpecfileVisitor();
		
		try {
			specsFolder.accept(specVisitor);
			List<IResource> installedSpecs = specVisitor.getSpecFiles();
			file = installedSpecs.get(0);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public void importSourceRPM(File externalFile) throws CoreException {
		// Copy original SRPM to workspace
		IFile srpmFile = getConfiguration().getSrpmsFolder().getFile(
				new Path(externalFile.getName()));
		try {
			srpmFile.create(new FileInputStream(externalFile), false, null);
		} catch (FileNotFoundException e) {
			String throw_message = Messages
					.getString("RPMCore.Error_trying_to_copy__") + //$NON-NLS-1$
					rpmConfig.getSpecsFolder().getLocation().toOSString();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}

		// Install the SRPM
		RPM rpm = new RPM(getConfiguration());
		rpm.install(srpmFile);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		// Set the project nature
		RPMProjectNature.addRPMNature(project, null);

	}

	public void buildAll(OutputStream outStream) throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildAll(getSpecFile(), outStream);

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getRpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getSrpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

	public void buildBinaryRPM(OutputStream out) throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildBinary(getSpecFile(), out);

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getRpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

	public void buildSourceRPM(OutputStream out) throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildSource(getSpecFile(), out);

		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
		getConfiguration().getSrpmsFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

	public void buildPrep(OutputStream out) throws CoreException {
		RPMBuild rpmbuild = new RPMBuild(getConfiguration());
		rpmbuild.buildPrep(getSpecFile(), out);
		getConfiguration().getBuildFolder().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}

}
