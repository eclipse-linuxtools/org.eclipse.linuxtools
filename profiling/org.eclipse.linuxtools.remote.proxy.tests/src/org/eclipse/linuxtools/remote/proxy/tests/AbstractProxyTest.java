/*******************************************************************************
 * Copyright (c) 2015, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wainer dos Santos Moschetta (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.remote.proxy.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for the proxy tests, providing a local C project.
 */
public abstract class AbstractProxyTest extends AbstractTest {
	protected static RemoteProxyManager proxyManager;
	protected ICProject localProject = null;
	protected final String PLUGIN = "org.eclipse.linuxtools.remote.proxy.tests";

	@BeforeEach
	public void setUp() throws Exception {
		proxyManager = RemoteProxyManager.getInstance();
		assertNotNull(proxyManager, "RemoteProxyManager object should not be null");
		localProject = createProject(Platform.getBundle(PLUGIN), "localTestProject");
		assertNotNull(localProject);
	}

	@AfterEach
	public void tearDown() throws CoreException {
		if (localProject != null) {
			deleteProject(localProject);
			localProject = null;
		}
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigType() {
		// This testsuite does not care about LaunchConfig
		return null;
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		// Do nothing. This testsuite does not care about LaunchConfig
	}
}
