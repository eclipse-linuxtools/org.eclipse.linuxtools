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
package org.eclipse.linuxtools.valgrind.memcheck.tests;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.valgrind.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;

public class MultiProcessTest extends AbstractMemcheckTest {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createProjectAndBuild("basicTest"); //$NON-NLS-1$
		proj = createProjectAndBuild("multiProcTest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(proj);
	}
	
	public void testNoExec() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testNoExec"); //$NON-NLS-1$

		MemcheckViewPart view = (MemcheckViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		assertEquals(1, view.getErrors().length);
	}
	
	public void testExec() throws Exception {
		ILaunchConfigurationWorkingCopy config = createConfiguration(proj.getProject()).getWorkingCopy();
		config.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, true);
		config.doSave();
		doLaunch(config, "testExec"); //$NON-NLS-1$

		MemcheckViewPart view = (MemcheckViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		assertEquals(4, view.getErrors().length);
	}
}
