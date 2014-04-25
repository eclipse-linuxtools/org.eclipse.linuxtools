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

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SignalTest extends AbstractMemcheckTest {

    @Before
    public void prep() throws Exception {
        proj = createProjectAndBuild("segvtest"); //$NON-NLS-1$
    }

    @Override
    @After
    public void tearDown() throws CoreException {
        deleteProject(proj);
        super.tearDown();
    }

    @Test
    public void testSegfaultHandle() throws Exception {
        ILaunchConfiguration config = createConfiguration(proj.getProject());
        doLaunch(config, "testSegfault"); //$NON-NLS-1$

        ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
        IValgrindMessage[] messages = view.getMessages();
        assertTrue(messages.length > 0);
        assertTrue(messages[0].getText().contains("SIGSEGV")); //$NON-NLS-1$
    }
}
