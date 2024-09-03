/*******************************************************************************
* Copyright (c) 2012, 2018 Red Hat, Inc.
*
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*    Red Hat, Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import java.util.ArrayList;
import java.util.Collections;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.FrameworkUtil;

public class LaunchTest extends AbstractTest {

    protected ILaunchConfiguration config;
    protected PerfLaunchConfigDelegate delegate;
    protected ILaunch launch;
    protected ILaunchConfigurationWorkingCopy wc;

    @BeforeEach
    public void setUp() throws Exception {
        proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest"); //$NON-NLS-1$
        config = createConfiguration(proj.getProject());

        delegate = new PerfLaunchConfigDelegate();
        launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);
        wc = config.getWorkingCopy();
        setProfileAttributes(wc);
    }

    @AfterEach
    public void tearDown() throws CoreException {
        deleteProject(proj);
        wc.delete();
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

    @Test
    public void testDefaultRun() throws CoreException {
        if (PerfCore.checkPerfInPath(null)) {
            delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
        }
    }

    @Test
    public void testClockEventRun() throws CoreException {
        if (PerfCore.checkPerfInPath(null)) {
            ArrayList<String> list = new ArrayList<>();
            Collections.addAll(list, "cpu-clock", "task-clock", "cycles");
            wc.setAttribute(PerfPlugin.ATTR_DefaultEvent, false);
            wc.setAttribute(PerfPlugin.ATTR_SelectedEvents, list);
            delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
        }
    }

}
