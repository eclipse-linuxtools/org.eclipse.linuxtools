/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BasicCachegrindTest extends AbstractCachegrindTest {

    @BeforeEach
    public void prep() throws Exception {
        proj = createProjectAndBuild("cpptest"); //$NON-NLS-1$
    }

    @Override
    @AfterEach
    public void tearDown() throws CoreException {
        deleteProject(proj);
        super.tearDown();
    }

    @Test
    public void testNumPIDs() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testNumPIDs"); //$NON-NLS-1$

        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin
                .getDefault().getView().getDynamicView();
        assertEquals(1, view.getOutputs().length);
    }

    @Test
    public void testFileNames() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testFileNames"); //$NON-NLS-1$

        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin
                .getDefault().getView().getDynamicView();
        CachegrindOutput output = view.getOutputs()[0];
        CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
        assertNotNull(file);
        file = getFileByName(output, "cpptest.h"); //$NON-NLS-1$
        assertNotNull(file);
    }

    @Test
    public void testNumFunctions() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testNumFunctions"); //$NON-NLS-1$

        CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin
                .getDefault().getView().getDynamicView();
        CachegrindOutput output = view.getOutputs()[0];
        CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
        assertNotNull(file);
        assertEquals(8, file.getFunctions().length);
    }
}
