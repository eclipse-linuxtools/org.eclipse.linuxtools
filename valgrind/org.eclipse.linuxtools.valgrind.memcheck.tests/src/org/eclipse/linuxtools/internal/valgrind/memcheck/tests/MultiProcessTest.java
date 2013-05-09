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

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.valgrind.launch.LaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

public class MultiProcessTest extends AbstractMemcheckTest {
	ICProject refProj;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		refProj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
		proj = createProjectAndBuild("multiProcTest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		deleteProject(refProj);
		super.tearDown();
	}
	
	public void testNoExec() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testNoExec"); //$NON-NLS-1$

		IValgrindMessage[] messages = ValgrindUIPlugin.getDefault().getView().getMessages();
		assertEquals(1, messages.length);
		checkTestMessages(messages, "testNoExec"); //$NON-NLS-1$
	}
	
	public void testExec() throws Exception {
		ILaunchConfigurationWorkingCopy config = createConfiguration(proj.getProject()).getWorkingCopy();
		config.setAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, true);
		config.doSave();
		doLaunch(config, "testExec"); //$NON-NLS-1$

		IValgrindMessage[] messages = ValgrindUIPlugin.getDefault().getView().getMessages();
		assertEquals(4, messages.length);
		checkTestMessages(messages, "testExec"); //$NON-NLS-1$
	}
}
