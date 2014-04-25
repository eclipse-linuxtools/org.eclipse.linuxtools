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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.rpm.core.FlatBuildConfiguration;
import org.eclipse.linuxtools.internal.rpm.core.Messages;
import org.eclipse.linuxtools.internal.rpm.core.RPMBuildConfiguration;
import org.eclipse.linuxtools.internal.rpm.core.SpecfileVisitor;
import org.eclipse.linuxtools.internal.rpm.core.utils.FileDownloadJob;
import org.eclipse.linuxtools.internal.rpm.core.utils.RPM;
import org.eclipse.linuxtools.internal.rpm.core.utils.RPMBuild;

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

    /**
     * Returns the configuration (RPMBuild, FLAT) for this project.
     *
     * @return The project configuration.
     */
    public IProjectConfiguration getConfiguration() {
        return rpmConfig;
    }

    /**
     * Returns the .spec file of this project.
     *
     * @return The .spec file or null if one is not found.
     */
    public IResource getSpecFile() {
        IContainer specsFolder = getConfiguration().getSpecsFolder();
        IResource file = null;
        SpecfileVisitor specVisitor = new SpecfileVisitor();

        try {
            specsFolder.accept(specVisitor);
            List<IResource> installedSpecs = specVisitor.getSpecFiles();
            if(installedSpecs.size() > 0){
                file = installedSpecs.get(0);
            }
        } catch (CoreException e) {
            // ignore, failed to find .spec file.
        }
        return file;
    }

    /**
     * Import a SRPM into this RPM project using local file.
     *
     * @param externalFile The SRPM file.
     * @throws CoreException If there is problem with the .src.rpm file.
     */
    public void importSourceRPM(File externalFile) throws CoreException {
        // Copy original SRPM to workspace
        IFile srpmFile = getConfiguration().getSrpmsFolder().getFile(
                new Path(externalFile.getName()));
        try {
            srpmFile.create(new FileInputStream(externalFile), false, null);
        } catch (FileNotFoundException e) {
            String throwMessage = Messages
                    .getString("RPMCore.Error_trying_to_copy__") + //$NON-NLS-1$
                    rpmConfig.getSpecsFolder().getLocation().toOSString();
            IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
                    throwMessage, e);
            throw new CoreException(error);
        }

        // Install the SRPM
        RPM rpm = new RPM(getConfiguration());
        rpm.install(srpmFile);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);

        // Set the project nature
        RPMProjectNature.addRPMNature(project, null);

    }

    /**
     * Import a remote SRPM into this RPM project, by downloading the file
     * and calling {@link RPMProject#importSourceRPM(File)}.
     *
     * @param remoteFile URI to the remote SRPM file.
     * @param monitor The progress monitor.
     * @throws CoreException Thrown if the import failed.
     */
    public void importSourceRPM(URL remoteFile, IProgressMonitor monitor) throws CoreException {
        URLConnection content;
        try {
            content = remoteFile.openConnection();
        } catch (IOException e) {
            Status status = new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
                    e.getMessage(), e);
            throw new CoreException(status);
        }
        File tempFile = new File(
                System.getProperty("java.io.tmpdir"), remoteFile.toString().substring(remoteFile.toString().lastIndexOf('/') + 1)); //$NON-NLS-1$
        if (tempFile.exists()) {
            tempFile.delete();
        }
        final FileDownloadJob downloadJob = new FileDownloadJob(tempFile,
                content);
        downloadJob.run(monitor);
        importSourceRPM(tempFile);
    }

    /**
     * Build both source and binary rpms.
     * @param outStream The stream to right command output to.
     * @return The result of the command.
     * @throws CoreException If exception occurs during building.
     */
    public IStatus buildAll(OutputStream outStream) throws CoreException {
        RPMBuild rpmbuild = new RPMBuild(getConfiguration());
        IStatus result = rpmbuild.buildAll(getSpecFile(), outStream);

        getConfiguration().getBuildFolder().refreshLocal(
                IResource.DEPTH_INFINITE, null);
        getConfiguration().getRpmsFolder().refreshLocal(
                IResource.DEPTH_INFINITE, null);
        getConfiguration().getSrpmsFolder().refreshLocal(
                IResource.DEPTH_INFINITE, null);
        return result;
    }

    /**
     * Builds binary rpm.
     * @param out The stream to right command output to.
     * @return The result of the command.
     * @throws CoreException If exception occurs during building.
     */
    public IStatus buildBinaryRPM(OutputStream out) throws CoreException {
        RPMBuild rpmbuild = new RPMBuild(getConfiguration());
        IStatus result = rpmbuild.buildBinary(getSpecFile(), out);

        getConfiguration().getBuildFolder().refreshLocal(
                IResource.DEPTH_INFINITE, null);
        getConfiguration().getRpmsFolder().refreshLocal(
                IResource.DEPTH_INFINITE, null);
        return result;
    }

    /**
     * Builds source rpm.
     * @param out The stream to right command output to.
     * @return The result of the command.
     * @throws CoreException If exception occurs during building.
     */
    public IStatus buildSourceRPM(OutputStream out) throws CoreException {
        RPMBuild rpmbuild = new RPMBuild(getConfiguration());
        IStatus result = rpmbuild.buildSource(getSpecFile(), out);

        getConfiguration().getBuildFolder().refreshLocal(
                IResource.DEPTH_INFINITE, null);
        getConfiguration().getSrpmsFolder().refreshLocal(
                IResource.DEPTH_INFINITE, null);
        return result;
    }

    /**
     * Prepares sources for build (rpmbuild -bp).
     * @param out The stream to right command output to.
     * @return The result of the command.
     * @throws CoreException If exception occurs during building.
     */
    public IStatus buildPrep(OutputStream out) throws CoreException {
        RPMBuild rpmbuild = new RPMBuild(getConfiguration());
        IStatus result = rpmbuild.buildPrep(getSpecFile(), out);
        getConfiguration().getBuildFolder().refreshLocal(
                IResource.DEPTH_INFINITE, null);
        return result;
    }

}
