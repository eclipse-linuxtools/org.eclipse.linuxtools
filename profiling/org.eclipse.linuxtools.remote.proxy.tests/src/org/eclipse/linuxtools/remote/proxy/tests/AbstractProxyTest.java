/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wainer dos Santos Moschetta (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.remote.proxy.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.profiling.tests.AbstractRemoteTest;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public abstract class AbstractProxyTest extends AbstractRemoteTest {
	protected static RemoteProxyManager proxyManager;
	protected static final String CONNECTION_NAME = "test_connection";
	protected static IRemoteConnection connection = null;
	protected ICProject localProject = null;
	protected ICProject syncProject = null;
	protected final String PLUGIN = "org.eclipse.linuxtools.remote.proxy.tests";

	@Before
	public void setUp() throws RemoteConnectionException {
		proxyManager = RemoteProxyManager.getInstance();
		assertNotNull("RemoteProxyManager object should not be null", proxyManager);
		if(connection == null) {
			connection = createJSchConnection(CONNECTION_NAME, CONNECTION_TYPE_JSCH);
		}
		createTestProjects();
	}

	@After
	public void tearDown() throws CoreException {
		if(localProject !=  null) {
			deleteProject(localProject);
			localProject = null;
		}
		if(syncProject !=  null) {
			deleteProject(syncProject);
			syncProject = null;
		}
		if(connection != null) {
			deleteConnection(connection);
			connection = null;
		}
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		// This testsuite does not care about LaunchConfig
		return null;
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		// Do nothing. This testsuite does not care about LaunchConfig
	}

	protected void createTestProjects() {
		if (localProject == null) {
			try {
				localProject = createProject(Platform.getBundle(PLUGIN), "localTestProject");
			} catch (Exception e) {
				fail("Failed to create local project for the tests: " + e.getMessage());
			}
			assertNotNull(localProject);
		}

		if (syncProject == null) {
			ICProject project = null;
			try {
				project = createProject(Platform.getBundle(PLUGIN), "syncTestProject");
				convertToSyncProject(project.getProject(), connection, "/tmp/" + PLUGIN);
			} catch (Exception e) {
				fail("Failed to create synchronized project for the tests: " + e.getMessage());
			}
			syncProject = project;
			assertNotNull(syncProject);
		}
	}
}
