/*******************************************************************************
 * Copyright (c) 2013, 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0s
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.internal.rpm.createrepo.Createrepo;
import org.eclipse.linuxtools.internal.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.internal.rpm.createrepo.form.tests.TestCreaterepoProject;
import org.eclipse.ui.console.MessageConsole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Tests for Createrepo class and general createrepo command
 * execution. Assumes system has "createrepo" command.
 */
public class CreaterepoTest {

    private static TestCreaterepoProject testProject;
    private CreaterepoProject project;
    private MessageConsole console;

    /**
     * Initialize the test project. Will fail immediately if it cannot find
     * the createrepo command.
     *
     * @throws CoreException
     */
    @BeforeClass
    public static void setUpBeforeClass() throws CoreException {
        testProject = new TestCreaterepoProject();
        assertTrue(testProject.getProject().exists());
    }

    /**
     * Delete the project when tests are done.
     *
     * @throws CoreException
     */
    @AfterClass
    public static void tearDownAfterClass() throws CoreException {
		if (testProject != null) {
			testProject.dispose();
			assertFalse(testProject.getProject().exists());
		}
    }

    /**
     * Get the CreaterepoProject at the beginning of each test, as
     * well as create the console.
     *
     * @throws CoreException
     */
    @Before
    public void setUp() throws CoreException {
        project = testProject.getCreaterepoProject();
        assertNotNull(project);
        console = new MessageConsole("testConsole", null, null, true); //$NON-NLS-1$
    }

    /**
     * Clear the preferences after each test and destroy the console.
     *
     * @throws BackingStoreException
     */
    @After
    public void tearDown() throws BackingStoreException {
        IEclipsePreferences pref = project.getEclipsePreferences();
        pref.clear();
        pref.flush();
        assertEquals(0, pref.keys().length);
        console.destroy();
    }

    /**
     * Test a simple createrepo execution taking in no extra commands.
     */
    @Test
    public void testSimpleCreaterepoExecution() {
        Createrepo command = new Createrepo();
		IStatus status = command.execute(console.newMessageStream(), project, new ArrayList<>());
        if (status.getCode() == IStatus.ERROR) {
            fail("Possibly failed due to system not having the 'createrepo' command, or it cannot be found."); //$NON-NLS-1$
        }
        assertTrue(status.isOK());
    }

}
