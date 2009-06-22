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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.valgrind.tests.ValgrindTestOptionsTab;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Version;

public class MinVersionTest extends AbstractMemcheckTest {
	private static final Version VER_3_2_1 = new Version(3, 2, 1);
	private Version verSave;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
		
		verSave = getPlugin().getValgrindVersion();
		getPlugin().setValgrindVersion(VER_3_2_1);
	}
	
	@Override
	protected void tearDown() throws Exception {
		getPlugin().setValgrindVersion(verSave);
		
		deleteProject(proj);
		super.tearDown();
	}
	
	public void testLaunchBadVersion() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		CoreException ce = null;
		
		try {
			doLaunch(config, "testDefaults"); //$NON-NLS-1$
		} catch (CoreException e) {
			ce = e;
		}
		
		assertNotNull(ce);
	}
	
	public void testTabsBadVersion() throws Exception {
		Shell testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
		ValgrindTestOptionsTab tab = new ValgrindTestOptionsTab();
		
		ILaunchConfiguration config = createConfiguration(proj.getProject());
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
