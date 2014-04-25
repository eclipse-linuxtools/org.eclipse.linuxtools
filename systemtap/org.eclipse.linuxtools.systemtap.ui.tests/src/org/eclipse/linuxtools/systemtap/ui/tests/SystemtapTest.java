/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.tests;

import java.io.IOException;

import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.junit.BeforeClass;

/**
 * Generic utilities for systemtap tests.
 */
public class SystemtapTest {
    public static boolean stapInstalled;

    @BeforeClass
    public static void checkStapInstalled() throws IOException {
        stapInstalled = SystemtapTest.stapInstalled();
    }

    /**
     * Check that stap is installed
     *
     * @return true if stap is installed, false otherwise.
     * @throws IOException
     */
    protected static boolean stapInstalled() throws IOException {
        Process process = RuntimeProcessFactory.getFactory().exec(
                new String[] { "stap", "-V" }, null); //$NON-NLS-1$ //$NON-NLS-2$
        return (process != null);
    }
}
