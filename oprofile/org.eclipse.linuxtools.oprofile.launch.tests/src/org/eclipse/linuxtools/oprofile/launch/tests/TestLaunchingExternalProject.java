/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.oprofile.launch.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile.OprofileProject;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileSetupTab;
import org.eclipse.linuxtools.oprofile.launch.tests.utils.LaunchTestingOptions;
import org.eclipse.linuxtools.oprofile.launch.tests.utils.OprofileTestingEventConfigTab;
import org.eclipse.linuxtools.oprofile.launch.tests.utils.TestingOprofileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

public class TestLaunchingExternalProject extends AbstractTest {

	private final Path EXTERNAL_PROJECT_PATH = new Path("/tmp/eclipse-oprofile-ext_project_test"); //$NON-NLS-1$
	private final String PROJECT_NAME = "primeTest"; //$NON-NLS-1$
	private ILaunchConfiguration config;
	private Shell testShell;
	private IProject externalProject;	// external project to work with

	@Before
	public void setUp() throws Exception {
		// Setup a temporary workspace external path
		File tempExternalProjectPath = EXTERNAL_PROJECT_PATH.toFile();
		// create directory if not existing
		if (!tempExternalProjectPath.exists()) {
			tempExternalProjectPath.mkdir();
		}
		externalProject = createExternalProjectAndBuild(FrameworkUtil.getBundle(this.getClass()),
				PROJECT_NAME, EXTERNAL_PROJECT_PATH);
		config = createConfiguration(externalProject);
		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
	}

	@After
	public void tearDown() throws Exception {
		testShell.dispose();
		externalProject.delete(true, null); // delete project
		// cleanup temporary directory
		File tempExternalProjectPath = EXTERNAL_PROJECT_PATH.toFile();
		if (tempExternalProjectPath.exists()) {
			tempExternalProjectPath.delete();
		}
	}

	// Implemented abstract method of AbstractTest
	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(OprofileLaunchPlugin.ID_LAUNCH_PROFILE);
	}

	// Implemented abstract method of AbstractTest
	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		OprofileTestingEventConfigTab configTab = new OprofileTestingEventConfigTab();
		configTab.setOprofileProject(externalProject);
		OprofileSetupTab setupTab = new OprofileSetupTab();
		configTab.setDefaults(wc);
		setupTab.setDefaults(wc);
	}

	/**
	 * Testcase for Eclipse BugZilla 321905/RedHat BZ
	 *
	 * @throws CoreException
	 */
	@Test
	public void testLaunchExternalProject() throws CoreException {
		LaunchTestingOptions options = new LaunchTestingOptions();
		options.setOprofileProject(externalProject);
		options.loadConfiguration(config);

		TestingOprofileLaunchConfigurationDelegate delegate = new TestingOprofileLaunchConfigurationDelegate();
		ILaunch launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);

		assertTrue(options.isValid());
		assertEquals("", options.getBinaryImage()); //$NON-NLS-1$
		assertEquals("", options.getKernelImageFile()); //$NON-NLS-1$
		assertEquals(OprofileDaemonOptions.SEPARATE_NONE, options.getSeparateSamples());

		OprofileProject.setProfilingBinary(OprofileProject.OPCONTROL_BINARY);
		delegate.launch(config, ILaunchManager.PROFILE_MODE, launch, null);
		assertTrue(delegate.eventsIsNull);
		assertNotNull(delegate._options);
		assertTrue(delegate._options.getBinaryImage().length() > 0);
		assertEquals(EXTERNAL_PROJECT_PATH.toOSString() + IPath.SEPARATOR + "Debug" + IPath.SEPARATOR + PROJECT_NAME, delegate._options.getBinaryImage()); //$NON-NLS-1$
	}
}
