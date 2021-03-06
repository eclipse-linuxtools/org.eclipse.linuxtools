/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProjectCreator;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProjectNature;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.ICreaterepoTestConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for project creation with CreaterepoProjectCreatorTest class.
 * This checks if the project creator is working, but does not check for
 * initializing of .repo file contents and the content folder. These are done
 * via the wizard (SWTBot test should handle this).
 */
public class CreaterepoProjectCreatorTest {

    private static IWorkspaceRoot root;
    private static NullProgressMonitor monitor;
    private IProject project;

    /**
     * Initialize workspace root and progress monitor.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        root = ResourcesPlugin.getWorkspace().getRoot();
        monitor = new NullProgressMonitor();
    }

    /**
     * Create the project using CreaterepoProjectCreator.
     *
     * @throws CoreException
     */
    @Before
    public void setUp() throws CoreException{
        if (project == null || !project.exists()) {
            project = CreaterepoProjectCreator.create(ICreaterepoTestConstants.PROJECT_NAME,
                    root.getLocation(), ICreaterepoTestConstants.REPO_NAME, monitor);
        }
        assertNotNull(project);
        assertTrue(project.exists());
    }

    /**
     * Forcefully delete the project if it exists.
     *
     * @throws CoreException
     */
    @After
    public void tearDown() throws CoreException {
        if (project != null && project.exists()) {
            project.delete(true, true, monitor);
        }
        assertFalse(project.exists());
    }

    /**
     * Test to see if the project has been properly created. Content folder
     * should not appear due to CreaterepoWizard handling its creation. Repo
     * file should be empty for the same reason.
     *
     * @throws CoreException
     * @throws IOException
     */
    @Test
    public void testProjectContents() throws CoreException, IOException {
        // 3 = .project + .repo file + .settings folder
        assertEquals(3, project.members().length);

        // contains the repo file
        assertTrue(project.findMember(ICreaterepoTestConstants.REPO_NAME).exists());

        IFile repoFile = (IFile) project.findMember(ICreaterepoTestConstants.REPO_NAME);
        // repo file should be empty because test did not go through project creation
        // to initialize .repo contents
        assertEquals(0, repoFile.getContents().available());
    }

    /**
     * Test to see if the project has the proper nature.
     *
     * @throws CoreException
     */
    @Test
    public void testProjectNature() throws CoreException {
        assertTrue(project.hasNature(CreaterepoProjectNature.CREATEREPO_NATURE_ID));
    }

}
