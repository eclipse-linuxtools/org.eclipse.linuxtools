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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
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

public class TestLaunching extends AbstractTest {
	private ILaunchConfiguration config;
	private Shell testShell;

	@Before
	public void setUp() throws Exception {
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "primeTest"); //$NON-NLS-1$
		config = createConfiguration(proj.getProject());
		testShell = new Shell(Display.getDefault());
		testShell.setLayout(new GridLayout());
	}

	@After
	public void tearDown() throws Exception {
		testShell.dispose();
		deleteProject(proj);
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(OprofileLaunchPlugin.ID_LAUNCH_PROFILE);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		OprofileTestingEventConfigTab configTab = new OprofileTestingEventConfigTab();
		OprofileSetupTab setupTab = new OprofileSetupTab();
		configTab.setOprofileProject(proj.getProject());
		configTab.setDefaults(wc);
		setupTab.setDefaults(wc);
	}
	@Test
	public void testDefaultLaunch() throws CoreException {
		TestingOprofileLaunchConfigurationDelegate delegate = new TestingOprofileLaunchConfigurationDelegate();
		ILaunch launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);

		LaunchTestingOptions options = new LaunchTestingOptions();
		options.setOprofileProject(proj.getProject());
		options.loadConfiguration(config);
		assertTrue(options.isValid());
		assertTrue(options.getBinaryImage().isEmpty());
		assertTrue(options.getKernelImageFile().isEmpty());
		assertEquals(OprofileDaemonOptions.SEPARATE_NONE, options.getSeparateSamples());
		Oprofile.OprofileProject.setProfilingBinary(Oprofile.OprofileProject.OPCONTROL_BINARY);
		delegate.launch(config, ILaunchManager.PROFILE_MODE, launch, null);
	}
	@Test
	public void testEventLaunch() throws CoreException {
		TestingOprofileLaunchConfigurationDelegate delegate = new TestingOprofileLaunchConfigurationDelegate();
		ILaunch launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);

		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, false);
		wc.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_ENABLED(0), true);
		wc.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_COUNT(0), 100000);
		wc.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_EVENT(0, 0),	"FAKE_EVENT"); //$NON-NLS-1$
		wc.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_KERNEL(0), true);
		wc.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_PROFILE_USER(0), true);
		wc.setAttribute(OprofileLaunchPlugin.ATTR_COUNTER_UNIT_MASK(0), 0);
		wc.doSave();
		LaunchTestingOptions options = new LaunchTestingOptions();
		options.setOprofileProject(proj.getProject());
		options.loadConfiguration(config);
		assertTrue(options.isValid());
		assertTrue(options.getBinaryImage().isEmpty());
		assertTrue(options.getKernelImageFile().isEmpty());
		assertEquals(OprofileDaemonOptions.SEPARATE_NONE, options.getSeparateSamples());

		Oprofile.OprofileProject.setProfilingBinary(Oprofile.OprofileProject.OPCONTROL_BINARY);
		delegate.launch(config, ILaunchManager.PROFILE_MODE, launch, null);
	}
}
