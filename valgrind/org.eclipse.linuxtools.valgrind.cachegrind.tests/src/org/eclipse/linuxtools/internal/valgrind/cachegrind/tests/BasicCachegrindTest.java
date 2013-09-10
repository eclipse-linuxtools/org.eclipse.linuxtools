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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindViewPart;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindFile;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.model.CachegrindOutput;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BasicCachegrindTest extends AbstractCachegrindTest {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("cpptest"); //$NON-NLS-1$
	}

	@Override
	@After
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
