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
package org.eclipse.linuxtools.valgrind.memcheck.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;

public class BasicTest extends AbstractTest {
	
	@Override
	protected void setUp() throws Exception {
		proj = createProject("basicTest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
	}
	
	public void testTest() throws Exception {
		assertNotNull(proj.getBinaryContainer().getBinaries()[0]);
	}
	
	public void testErrors() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		config.launch(ILaunchManager.PROFILE_MODE, null, true);
				
		MemcheckViewPart view = (MemcheckViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		assertEquals(3, view.getErrors().length);
	}


}
