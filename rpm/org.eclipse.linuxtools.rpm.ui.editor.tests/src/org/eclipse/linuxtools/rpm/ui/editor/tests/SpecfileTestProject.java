/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.core.utils.Utils;

public class SpecfileTestProject {
    private IProject project;

    public SpecfileTestProject() throws CoreException {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        project = workspaceRoot.getProject("TestSpecfileProject");
        if (!project.exists()) {
            project.create(null);
        }
        project.open(null);
    }

    public void dispose() throws CoreException {
        project.delete(true, true, null);
        try {
            Utils.runCommandToInputStream("rm", "-f", "/tmp/pkglist");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IFile createFile(String filename) throws CoreException {
        IFile testSpecfile = project.getFile(filename);
        if (!testSpecfile.exists()) {
            testSpecfile.create(null, true, null);
        }
        return testSpecfile;
    }

    protected IMarker[] getFailureMarkers() throws CoreException {
        return project.findMarkers(
                "org.eclipse.linuxtools.rpm.ui.editor.specfileerror", false,
                IResource.DEPTH_INFINITE);
    }

    public void refresh() throws CoreException {
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
    }
}
