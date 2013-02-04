/*******************************************************************************
* Copyright (c) 2012 Red Hat, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Red Hat, Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.launch.PerfEventsTab;
import org.eclipse.linuxtools.internal.perf.launch.PerfLaunchConfigDelegate;
import org.eclipse.linuxtools.internal.perf.launch.PerfOptionsTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.osgi.framework.FrameworkUtil;

public class LaunchTest extends AbstractTest {

	protected ILaunchConfiguration config;
	protected PerfLaunchConfigDelegate delegate;
	protected ILaunch launch;
	protected ILaunchConfigurationWorkingCopy wc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest"); //$NON-NLS-1$
		config = createConfiguration(proj.getProject());

		delegate = new PerfLaunchConfigDelegate();
		launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);
		wc = config.getWorkingCopy();
		setProfileAttributes(wc);
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		wc.delete();
		super.tearDown();
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(PerfPlugin.LAUNCHCONF_ID);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		PerfEventsTab eventsTab = new PerfEventsTab();
		PerfOptionsTab optionsTab = new PerfOptionsTab();
		wc.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, false);
		wc.setAttribute(PerfPlugin.ATTR_ShowSourceDisassembly, true);
		eventsTab.setDefaults(wc);
		optionsTab.setDefaults(wc);
	}

	public void testDefaultRun () {
		if (PerfCore.checkPerfInPath()) {
			try {
				delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
			} catch (CoreException e) {
				fail();
			}
		}
	}

	public void testClockEventRun () {
		if (PerfCore.checkPerfInPath()) {
			try {
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(Arrays.asList(new String [] {"cpu-clock", "task-clock", "cycles"}));
				wc.setAttribute(PerfPlugin.ATTR_DefaultEvent, false);
				wc.setAttribute(PerfPlugin.ATTR_SelectedEvents, list);
				delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
			} catch (CoreException e) {
				fail();
			}
		}
	}

}
