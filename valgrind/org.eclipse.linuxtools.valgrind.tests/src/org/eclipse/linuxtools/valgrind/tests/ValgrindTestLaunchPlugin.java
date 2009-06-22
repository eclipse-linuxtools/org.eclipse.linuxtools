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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class ValgrindTestLaunchPlugin extends ValgrindLaunchPlugin {

	protected Bundle testBundle;
	protected IValgrindToolPage toolPage;
	
	private static ValgrindTestLaunchPlugin instance;

	public ValgrindTestLaunchPlugin() {
		instance = this;
	}

	public static ValgrindTestLaunchPlugin getDefault() {
		if (instance == null) {
			instance = new ValgrindTestLaunchPlugin();
		}
		return instance;
	}
	
	public void setTestBundle(Bundle testBundle) {
		this.testBundle = testBundle;
	}
	
	public Bundle getTestBundle() {
		return testBundle;
	}
	
	public void setToolPage(IValgrindToolPage toolPage) {
		this.toolPage = toolPage;
	}
	
	@Override
	public ILaunch getCurrentLaunch() {
		return ValgrindLaunchPlugin.getDefault().getCurrentLaunch();
	}
	
	@Override
	public void setCurrentLaunch(ILaunch launch) {
		ValgrindLaunchPlugin.getDefault().setCurrentLaunch(launch);
	}
	
	@Override
	public ILaunchConfiguration getCurrentLaunchConfiguration() {
		return ValgrindLaunchPlugin.getDefault().getCurrentLaunchConfiguration();
	}
	
	@Override
	public void setCurrentLaunchConfiguration(ILaunchConfiguration config) {
		ValgrindLaunchPlugin.getDefault().setCurrentLaunchConfiguration(config);
	}
	
	@Override
	public IPath getValgrindLocation() throws CoreException {
		return ValgrindLaunchPlugin.getDefault().getValgrindLocation();
	}
	
	@Override
	public void setValgrindLocation(IPath valgrindLocation) {
		ValgrindLaunchPlugin.getDefault().setValgrindLocation(valgrindLocation);
	}
	
	@Override
	public Version getValgrindVersion() throws CoreException {
		return ValgrindLaunchPlugin.getDefault().getValgrindVersion();
	}
	
	@Override
	public void setValgrindVersion(Version valgrindVersion) {
		ValgrindLaunchPlugin.getDefault().setValgrindVersion(valgrindVersion);
	}
	
	@Override
	public IValgrindToolPage getToolPage(String id) throws CoreException {
		return toolPage;
	}

	@Override
	protected ValgrindCommand getValgrindCommand() {
		if (!ValgrindTestsPlugin.RUN_VALGRIND) {
			return new ValgrindStubCommand();
		}
		else {
			return super.getValgrindCommand();
		}
	}
	
}
