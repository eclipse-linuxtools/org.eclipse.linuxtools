/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMProjectCreator;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.junit.After;
import org.junit.Test;

public class RPMProjectCreatorTest {

    private IProject newProject;
    @After
    public void deleteProject() throws CoreException {
        if (newProject != null && newProject.exists()) {
            newProject.delete(true, new NullProgressMonitor());
        }
    }

    @Test
    public void createFlat() throws CoreException {
        RPMProjectCreator projectCreator = new RPMProjectCreator(RPMProjectLayout.FLAT);
        newProject = projectCreator.create("flatproject", Platform.getLocation(), new NullProgressMonitor());
        assertNotNull(newProject);
        assertEquals(1, newProject.getDescription().getNatureIds().length);
        assertEquals(IRPMConstants.RPM_NATURE_ID,
                newProject.getDescription().getNatureIds()[0]);
    }

    @Test
    public void createRPMBuild() throws CoreException {
        RPMProjectCreator projectCreator = new RPMProjectCreator();
        newProject = projectCreator.create("rpmbuild", Platform.getLocation(), new NullProgressMonitor());
        assertNotNull(newProject);
        assertEquals(1, newProject.getDescription().getNatureIds().length);
        assertEquals(IRPMConstants.RPM_NATURE_ID,
                newProject.getDescription().getNatureIds()[0]);
        assertTrue(newProject.exists(new Path("SOURCES")));
        assertTrue(newProject.exists(new Path("SPECS")));
    }

}
