/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractLinkedResourceMemcheckTest extends
        AbstractMemcheckTest {

    @Before
    public void linkedResourceSetUp() throws Exception {
        proj = createProject(getBundle(), "linkedTest"); //$NON-NLS-1$

        // delete source folder and replace it with a link to its bundle
        // location
        final Exception[] ex = new Exception[1];
        ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                try {
                    URL location = FileLocator.find(getBundle(), new Path(
                            "resources/linkedTest/src"), null); //$NON-NLS-1$
                    IFolder srcFolder = proj.getProject().getFolder("src"); //$NON-NLS-1$
                    srcFolder.delete(true, null);
                    srcFolder.createLink(FileLocator.toFileURL(location)
                            .toURI(), IResource.REPLACE, null);
                } catch (Exception e) {
                    ex[0] = e;
                }
            }

        }, null);

        if (ex[0] != null) {
            throw ex[0];
        }

        assertEquals(0, proj.getBinaryContainer().getBinaries().length);

        buildProject(proj);
    }

    @After
    public void cleanupLinkedResource() throws CoreException {
        deleteProject(proj);
    }

}
