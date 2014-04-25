/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMProjectNature;
import org.junit.BeforeClass;
import org.junit.Test;

public class RPMProjectNatureTest {

    static IWorkspace workspace;
    static IWorkspaceRoot root;
    static NullProgressMonitor monitor;

    @BeforeClass
    public static void setUp() throws Exception {
        IWorkspaceDescription desc;
        workspace = ResourcesPlugin.getWorkspace();
        if (workspace == null) {
            fail("Workspace was not setup");
        }
        root = workspace.getRoot();
        monitor = new NullProgressMonitor();
        if (root == null) {
            fail("Workspace root was not setup");
        }
        desc = workspace.getDescription();
        desc.setAutoBuilding(false);
        workspace.setDescription(desc);
    }

    @Test
    public void testAddRPMProjectNature() throws Exception {
        IProject testProject = root.getProject("testProject");
        testProject.create(monitor);
        testProject.open(monitor);
        RPMProjectNature.addRPMNature(testProject, monitor);
        assertTrue(testProject.hasNature(IRPMConstants.RPM_NATURE_ID));
        testProject.delete(true, false, monitor);
    }

}
