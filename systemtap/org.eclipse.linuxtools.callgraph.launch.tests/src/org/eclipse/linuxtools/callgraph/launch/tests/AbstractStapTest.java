/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public void setUp() throws Exception {
		launches = new ArrayList<ILaunch>();
	}

	@After
	public void tearDown() throws Exception {
		if (launches.size() > 0) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunches(launches.toArray(new ILaunch[launches.size()]));
			launches.clear();
		}
	}
}
