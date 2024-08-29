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
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.callgraph.launch.tests;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SystemTapErrorHandlerTest  {

    private SystemTapErrorHandler errHandler;
    private String errorString;

    @BeforeEach
    public void setUp() {
        errHandler = new SystemTapErrorHandler();
    }

    @Test
    public void testErrorNotRecognized(){

        errorString = "This error will not be caught \n" +
                "Not even this one \n" +
                "Unrecognized \n" +
                "Not found \n" +
                "Error";

        errHandler.handle(new NullProgressMonitor(), errorString);

        assertFalse(errHandler.isErrorRecognized());
    }

    @Test
    public void testErrorRecognized(){

        errorString = "As long as the word stapusr or stapdev is here, error is recognized";

        errHandler.handle(new NullProgressMonitor(), errorString);

        assertTrue(errHandler.isErrorRecognized());
    }

    @Test
    public void testUserGroupError(){

        errorString = "ERROR: You are trying to run systemtap as a normal user.\n" +
            "You should either be root, or be part of the group \"stapusr\" and " +
            "possibly one of the groups \"stapsys\" or \"stapdev\".";

        errHandler.handle(new NullProgressMonitor(), errorString);

        assertTrue(errHandler.isErrorRecognized());
        assertTrue(errHandler.getErrorMessage().contains("Please add yourself to the 'stapdev' or 'stapusr' group in order to run stap."));
    }

    @Test
    public void testDebugInfoError(){

        errorString = "missing [architecture] kernel/module debuginfo under '[kernel-build-tree]'";

        errHandler.handle(new NullProgressMonitor(), errorString);

        assertTrue(errHandler.isErrorRecognized());
        assertTrue(errHandler.getErrorMessage().contains("No debuginfo could be found. Make sure you have yum-utils installed, and run debuginfo-install kernel as root."));
    }

    @Test
    public void testUprobesError(){

        errorString = "SystemTap's version of uprobes is out of date. As root, or a member of the 'root' group, run \"make -C /usr/local/share/systemtap/runtime/uprobes\".";

        errHandler.handle(new NullProgressMonitor(), errorString);

        assertTrue(errHandler.isErrorRecognized());
        System.out.println(errHandler.getErrorMessage());
        assertTrue(errHandler.getErrorMessage().contains("SystemTap's version of uprobes is out of date."));
        assertTrue(errHandler.getErrorMessage().contains("make -C /usr/local/share/systemtap/runtime/uprobes\"."));
    }
}
