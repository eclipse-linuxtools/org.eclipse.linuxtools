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
package org.eclipse.linuxtools.internal.valgrind.cachegrind.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.internal.valgrind.launch.LaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiProcessTest extends AbstractCachegrindTest {
	private ICProject refProj;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		refProj = createProjectAndBuild("cpptest"); //$NON-NLS-1$
		proj = createProjectAndBuild("multiProcTest"); //$NON-NLS-1$
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		deleteProject(proj);
		deleteProject(refProj);
		super.tearDown();
	}

	@Test
	public void testNoExec() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testNoExec"); //$NON-NLS-1$

		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin
				.getDefault().getView().getDynamicView();
		assertEquals(1, view.getOutputs().length);
	}

	@Test
	public void testNumPids() throws Exception {
		ILaunchConfigurationWorkingCopy config = createConfiguration(
				proj.getProject()).getWorkingCopy();
		config.setAttribute(
				LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, true);
		config.doSave();
		doLaunch(config, "testExec"); //$NON-NLS-1$

		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin
				.getDefault().getView().getDynamicView();
		assertEquals(2, view.getOutputs().length);
	}

	@Test
	public void testFileNames() throws Exception {
		ILaunchConfigurationWorkingCopy config = createConfiguration(
				proj.getProject()).getWorkingCopy();
		config.setAttribute(
				LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, true);
		config.doSave();
		doLaunch(config, "testExec"); //$NON-NLS-1$

		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin
				.getDefault().getView().getDynamicView();

		int pidIx = 0;
		CachegrindOutput output = view.getOutputs()[pidIx];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		if (file == null) {
			pidIx = 1;
			output = view.getOutputs()[pidIx];
			file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		}
		assertNotNull(file);
		file = getFileByName(output, "cpptest.h"); //$NON-NLS-1$
		assertNotNull(file);

		// test other pid
		pidIx = (pidIx + 1) % 2;
		output = view.getOutputs()[pidIx];
		file = getFileByName(output, "parent.cpp"); //$NON-NLS-1$
		assertNotNull(file);
	}

	@Test
	public void testNumFunctions() throws Exception {
		ILaunchConfigurationWorkingCopy config = createConfiguration(
				proj.getProject()).getWorkingCopy();
		config.setAttribute(
				LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, true);
		config.doSave();
		doLaunch(config, "testExec"); //$NON-NLS-1$

		CachegrindViewPart view = (CachegrindViewPart) ValgrindUIPlugin
				.getDefault().getView().getDynamicView();

		int pidIx = 0;
		CachegrindOutput output = view.getOutputs()[pidIx];
		CachegrindFile file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		if (file == null) {
			pidIx = 1;
			output = view.getOutputs()[pidIx];
			file = getFileByName(output, "cpptest.cpp"); //$NON-NLS-1$
		}
		assertNotNull(file);
		assertEquals(8, file.getFunctions().length);

		// test other pid
		pidIx = (pidIx + 1) % 2;
		output = view.getOutputs()[pidIx];
		file = getFileByName(output, "parent.cpp"); //$NON-NLS-1$
		assertNotNull(file);
		assertEquals(6, file.getFunctions().length);
	}
}
