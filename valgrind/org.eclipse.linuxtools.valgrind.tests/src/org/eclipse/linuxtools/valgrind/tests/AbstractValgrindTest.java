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
package org.eclipse.linuxtools.valgrind.tests;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.eclipse.linuxtools.valgrind.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.valgrind.launch.ValgrindOptionsTab;
import org.osgi.framework.Bundle;

public abstract class AbstractValgrindTest extends AbstractTest {

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ValgrindLaunchPlugin.LAUNCH_ID);
	}

	protected ILaunch doLaunch(ILaunchConfiguration config, String testName) throws Exception {
		URL location = FileLocator.find(getBundle(), new Path("valgrindFiles"), null); //$NON-NLS-1$
		File file = new File(FileLocator.toFileURL(location).toURI());
		IPath pathToFiles = new Path(file.getAbsolutePath()).append(testName);

		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(LaunchConfigurationConstants.ATTR_OUTPUT_DIR, pathToFiles.toOSString());
		Set<String> modes = new HashSet<String>(1);
		modes.add(ILaunchManager.PROFILE_MODE);
		wc.setPreferredLaunchDelegate(modes, ValgrindTestsPlugin.DELEGATE_ID);
		wc.doSave();

		return config.launch(ILaunchManager.PROFILE_MODE, null, true);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) throws CoreException {
		ILaunchConfigurationTab tab = new ValgrindOptionsTab();
		tab.setDefaults(wc);
		tab = ValgrindLaunchPlugin.getDefault().getToolPage(getToolID());
		tab.setDefaults(wc);
		wc.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, getToolID());
	}
	
	protected ICProject createProject(String projname) throws Exception {
		return createProject(getBundle(), projname);
	}
	
	protected abstract Bundle getBundle();
	
	protected abstract String getToolID();	

}