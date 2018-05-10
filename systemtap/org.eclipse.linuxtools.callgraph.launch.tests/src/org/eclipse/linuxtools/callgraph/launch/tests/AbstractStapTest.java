/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph.launch.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.internal.callgraph.launch.SystemTapOptionsTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.junit.After;
import org.junit.Before;
import org.osgi.framework.FrameworkUtil;

public class AbstractStapTest extends AbstractTest {

    @Override
    protected ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(PluginConstants.CONFIGURATION_TYPE_ID);
    }

    @Override
    protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
            ILaunchConfigurationTab tab = new SystemTapOptionsTab();
            tab.setDefaults(wc);
    }


    protected ICProject createProjectAndBuild(String projname) throws Exception {
        return createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), projname);
    }

    public void killStap() {
        try {
            RuntimeProcessFactory.getFactory().exec("kill stap", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<ILaunch> launches;

    @Before
    public void setUp()  {
        launches = new ArrayList<>();
    }

    @After
    public void tearDown() {
        if (!launches.isEmpty()) {
            DebugPlugin.getDefault().getLaunchManager().removeLaunches(launches.toArray(new ILaunch[launches.size()]));
            launches.clear();
        }
    }
}
