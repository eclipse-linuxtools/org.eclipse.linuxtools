/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.launch.Messages;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BasicHelgrindTest extends AbstractHelgrindTest {

    @Before
    public void prep() throws Exception {
        proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
    }

    @Override
    @After
    public void tearDown() throws CoreException {
        deleteProject(proj);
        super.tearDown();
    }

    @Ignore
    @Test
    public void testNumErrors() throws CoreException, URISyntaxException, IOException   {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testHelgrindGeneric"); //$NON-NLS-1$

        ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
        assertEquals(1, view.getMessages().length);
        assertEquals(view.getMessages()[0].getText(), Messages.getString("ValgrindOutputView.No_output")); //$NON-NLS-1$
    }
}
