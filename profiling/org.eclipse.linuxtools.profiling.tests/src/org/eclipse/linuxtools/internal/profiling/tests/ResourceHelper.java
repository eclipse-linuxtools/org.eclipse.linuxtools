/*******************************************************************************
 * Copyright (c) 2009, 2011, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *     James Blackburn (Broadcom Corp.)
 *     Jeff Johnston (Red Hat Inc.) - Modified for use in Linux Tools project
 *******************************************************************************/

package org.eclipse.linuxtools.internal.profiling.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This class contains utility methods for creating resources
 * such as projects, files, folders etc. which are being used
 * in test fixture of unit tests.
 *
 * Some classes with similar idea worth to look at:
 * org.eclipse.core.filebuffers.tests.ResourceHelper,
 * org.eclipse.cdt.ui.tests.text.ResourceHelper.
 *
 * @since 6.0
 */

public class ResourceHelper {
    private final static IProgressMonitor NULL_MONITOR = new NullProgressMonitor();
    private final static Set<String> externalFilesCreated = new HashSet<>();
    private final static Set<IResource> resourcesCreated = new HashSet<>();


    /**
     * Creates new folder from workspace root. The folder name
     * can include relative path as a part of the name.
     * Nonexistent parent directories are being created as per {@link File#mkdirs()}.
     * The intention of the method is to create folders which do not belong to any project.
     *
     * @param name - folder name.
     * @return absolute location of the folder on the file system.
     */
    public static IPath createWorkspaceFolder(String name) throws CoreException {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IPath fullPath = workspaceRoot.getLocation().append(name);
        java.io.File folder = new java.io.File(fullPath.toOSString());
        if (!folder.exists()) {
            boolean result = folder.mkdirs();
            assertTrue(result);
        }
        assertTrue(folder.exists());

        externalFilesCreated.add(fullPath.toOSString());
        workspaceRoot.refreshLocal(IResource.DEPTH_INFINITE, NULL_MONITOR);
        return fullPath;
    }

    /**
     * Creates new temporary folder with generated name from workspace root.
     *
     * @return absolute location of the folder on the file system.
     */
    public static IPath createTemporaryFolder() throws CoreException {
        return ResourceHelper.createWorkspaceFolder("tmp/"+System.currentTimeMillis()+'.'+UUID.randomUUID());
    }


    /**
     * Clean-up any files created as part of a unit test.
     * This method removes *all* Workspace IResources and any external
     * files / folders created with the #createWorkspaceFile #createWorkspaceFolder
     * methods in this class
     */
    public static void cleanUp() throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        root.refreshLocal(IResource.DEPTH_INFINITE, NULL_MONITOR);

        // Delete all external files & folders created using ResourceHelper
        for (String loc : externalFilesCreated) {
            File f = new File(loc);
            if (f.exists())
                deleteRecursive(f);
        }
        externalFilesCreated.clear();

        // Remove IResources created by this helper
        for (IResource r : resourcesCreated) {
            if (r.exists())
                try {
                    r.delete(true, NULL_MONITOR);
                } catch (CoreException e) {
                    // Ignore
                }
        }
        resourcesCreated.clear();
    }


    /**
     * Recursively delete a directory / file
     *
     * For safety this method only deletes files created under the workspace
     *
     * @param file
     */
    private static final void deleteRecursive(File f) throws IllegalArgumentException {
        // Ensure that the file being deleted is a child of the workspace
        // root to prevent anything nasty happening
        if (! f.getAbsolutePath().startsWith(
                ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath()))
            throw new IllegalArgumentException("File must exist within the workspace!");

        if (f.isDirectory())
            for (File f1 : f.listFiles())
                deleteRecursive(f1);
        f.delete();
    }
}
