/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.internal.rpm.createrepo.Createrepo;
import org.eclipse.linuxtools.rpm.core.utils.BufferedProcessInputStream;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.tests.TestCreaterepoProject;
import org.eclipse.ui.console.MessageConsole;
import org.junit.After;
import org.junit.AfterClass;
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
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws CoreException, IOException, InterruptedException {
		BufferedProcessInputStream bpis = Utils.runCommandToInputStream("which", "createrepo"); //$NON-NLS-1$ //$NON-NLS-2$
		if (bpis.getExitValue() == 1) {
			fail("Failed due to system not having the 'createrepo' command, or it cannot be found."); //$NON-NLS-1$
		}
		testProject = new TestCreaterepoProject();
	}

	/**
	 * Delete the project when tests are done.
	 *
	 * @throws CoreException
	 */
	@AfterClass
	public static void tearDownAfterClass() throws CoreException {
		testProject.dispose();
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
		console.destroy();
	}

	/**
	 * Test a simple createrepo execution taking in no extra commands.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testSimpleCreaterepoExecution() throws CoreException {
		Createrepo command = new Createrepo();
		IStatus status = command.execute(console.newMessageStream(),
				project, new ArrayList<String>());
		if (status.getCode() == IStatus.ERROR) {
			fail("Possibly failed due to system not having the 'createrepo' command, or it cannot be found."); //$NON-NLS-1$
		}
		assertEquals(IStatus.OK, status.getCode());
	}

}
