/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.memcheck.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BasicMemcheckTest extends AbstractMemcheckTest {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}

	@Override
	@After
	public void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}
	@Test
	public void testNumErrors() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testNumErrors"); //$NON-NLS-1$

		IValgrindMessage[] messages = ValgrindUIPlugin.getDefault().getView().getMessages();
		assertEquals(3, messages.length);
		checkTestMessages(messages, "testNumErrors"); //$NON-NLS-1$
	}
}
