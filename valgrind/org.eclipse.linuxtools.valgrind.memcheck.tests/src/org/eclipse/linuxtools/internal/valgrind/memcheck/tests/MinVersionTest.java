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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.linuxtools.internal.valgrind.tests.ValgrindStubCommand;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MinVersionTest extends AbstractMemcheckTest {

	static class ValgrindIncorrectVersion extends ValgrindStubCommand {
		@Override
		public String whichVersion(IProject project) {
			return "valgrind-3.2.1"; //$NON-NLS-1$
		}
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$

		saveVersion();
	}

	private void saveVersion() {
		ValgrindLaunchPlugin.getDefault().setValgrindCommand(
				new ValgrindIncorrectVersion());
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		restoreVersion();

		deleteProject(proj);
		super.tearDown();
	}

	private void restoreVersion() {
		ValgrindLaunchPlugin.getDefault().setValgrindCommand(
				new ValgrindCommand());
	}

	@Test
	public void testLaunchBadVersion() throws Exception {
		// Put this back so we can make a valid config
		restoreVersion();
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		// For some reason we downgraded
		saveVersion();

		try {
			doLaunch(config, "testDefaults"); //$NON-NLS-1$
		} catch (CoreException e) {
			assertNotNull(e);
		}

	}

	@Test
	public void testTabsBadVersion() throws Exception {
		Shell testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
		ValgrindOptionsTab tab = new ValgrindOptionsTab();

		ILaunchConfiguration config = getLaunchConfigType().newInstance(
				null,
				getLaunchManager().generateLaunchConfigurationName(
						proj.getProject().getName()));
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		tab.setDefaults(wc);
		tab.createControl(testShell);
		tab.initializeFrom(config);
		tab.performApply(wc);

		assertFalse(tab.isValid(config));
		assertNotNull(tab.getErrorMessage());

		testShell.dispose();
	}

}
