/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.utils.tests;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.junit.Test;

public class RPMQueryTest {

    private static final String[] testWrongNVRs = {
            "%{?scl_prefix}eclipse-jgit-3.0.0-1.fc20",
            "%{?scl_prefix}eclipse-egit-2.3.1-1.fc20",
            "eclipse-egit-github-2.3.0-5.fc20",
            "eclipse-fedorapackager-0.4.1-6.fc20",
            "%{?scl_prefix}eclipse-jgit-3.0.0-1.fc20",
            "fedora-packager-0.5.10.1-3.fc20"
            };

    private static final String[] testCorrectNVRs = {
            "eclipse-jgit-3.0.0-1.fc20",
            "eclipse-egit-2.3.1-1.fc20",
            "eclipse-egit-github-2.3.0-5.fc20",
            "eclipse-fedorapackager-0.4.1-6.fc20",
            "eclipse-jgit-3.0.0-1.fc20",
            "fedora-packager-0.5.10.1-3.fc20"
            };

    @Test
    public void testEval() throws CoreException {
        // check eval for string without macro
        assertEquals("should be same", RPMQuery.eval("should be same").trim());
        // check eval for macro only
        assertEquals("/usr/share", RPMQuery.eval("%{_datadir}").trim());
        // check eval for macro and string
        assertEquals("/usr/share/eclipse", RPMQuery.eval("%{_datadir}/eclipse")
                .trim());
        // check eval for conditional undefined macro
        assertEquals("eclipse", RPMQuery.eval("%{?scl_prefix}eclipse").trim());
    }

    // Test evaluating NVRs when displaying bodhi update dialog
    @Test
    public void testNVREval() throws CoreException {
        String toEval;
        for (int i = 0; i < testCorrectNVRs.length; i++) {
            toEval = RPMQuery.eval(testWrongNVRs[i]).trim();
            assertNotNull(toEval);
            assertEquals(toEval, testCorrectNVRs[i]);
        }
    }

}
