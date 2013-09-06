/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.launch.Messages;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BasicHelgrindTest extends AbstractHelgrindTest {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}

	@Override
	@After
	public void tearDown() throws CoreException {
		deleteProject(proj);
		super.tearDown();
	}
	@Test
	public void testNumErrors() throws CoreException, URISyntaxException, IOException   {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testHelgrindGeneric"); //$NON-NLS-1$

		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		assertEquals(1, view.getMessages().length);
		assertEquals(view.getMessages()[0].getText(), Messages.getString("ValgrindOutputView.No_output")); //$NON-NLS-1$
	}
}
