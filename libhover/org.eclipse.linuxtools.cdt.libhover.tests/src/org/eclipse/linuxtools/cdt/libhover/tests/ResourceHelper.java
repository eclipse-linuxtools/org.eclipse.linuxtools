/*******************************************************************************
 * Copyright (c) 2009, 2018 Andrew Gvozdev and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *     James Blackburn (Broadcom Corp.)
 *     Liviu Ionescu - bug 392416
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.libhover.tests;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
            if (r.exists()) {
                try {
                    r.delete(true, NULL_MONITOR);
                } catch (CoreException e) {
                    // Ignore
                }
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
        if (!f.getAbsolutePath().startsWith(
                ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath())) {
            throw new IllegalArgumentException("File must exist within the workspace!");
        }

        if (f.isDirectory()) {
            for (File f1 : f.listFiles()) {
                deleteRecursive(f1);
            }
        }
        f.delete();
    }
}
