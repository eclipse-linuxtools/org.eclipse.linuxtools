/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rdt.proxy;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteResource;

public class RDTFileProxy implements IRemoteFileProxy {

    private IProject project;
    private IRemoteFileService manager;
    private IRemoteResource remoteRes;

    private void initialize(URI uri) throws CoreException {
        IRemoteConnection connection = RDTProxyManager.getConnection(uri);
        if (connection != null) {
            manager = connection.getService(IRemoteFileService.class);
        } else {
            throw new CoreException(Status.error(Activator.getResourceString("Connection.error"))); //$NON-NLS-1$
        }
    }

    public RDTFileProxy(URI uri) throws CoreException {
        initialize(uri);
    }

    public RDTFileProxy(IProject project) throws CoreException {
        this.project = project;
        URI uri = project.getLocationURI();
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = workspaceRoot.findMember(project.getName());
        if (resource != null) {
            remoteRes = resource.getAdapter(IRemoteResource.class);
            if (project.hasNature(RDTProxyManager.SYNC_NATURE)) {
                uri = remoteRes.getActiveLocationURI();
            }
        }
        initialize(uri);
    }

    @Override
    public URI toURI(IPath path) {
        return manager.toURI(path);
    }

    @Override
    public URI toURI(String path) {
        return manager.toURI(path);
    }

    @Override
    public String toPath(URI uri) {
        return manager.toPath(uri);
    }

    @Override
    public String getDirectorySeparator() {
        return manager.getDirectorySeparator();
    }

    @Override
    public IFileStore getResource(String path) {
        return manager.getResource(path);
    }

    @Override
    public URI getWorkingDir() {
        try {
            if (project.hasNature(RDTProxyManager.SYNC_NATURE))
                return remoteRes.getActiveLocationURI();
        } catch (CoreException e) {
            return project.getLocationURI();
        }
        return project.getLocationURI();
    }

}
