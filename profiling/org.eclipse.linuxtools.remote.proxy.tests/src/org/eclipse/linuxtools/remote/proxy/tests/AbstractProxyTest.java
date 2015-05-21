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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.profiling.tests.AbstractRemoteTest;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public abstract class AbstractProxyTest extends AbstractRemoteTest {
	protected static RemoteProxyManager proxyManager;
	protected static final String CONNECTION_NAME = "test_connection";
	protected static IRemoteConnection connection = null;
	protected IProject localProject = null;
	protected IProject syncProject = null;
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

	@AfterClass
	public static void tearDown() throws RemoteConnectionException {
		if(connection != null) {
			deleteConnection(connection);
		}
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		// This testsuite does not care about LaunchConfig
		return null;
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) throws CoreException {
		// Do nothing. This testsuite does not care about LaunchConfig
	}

	protected void createTestProjects() {
		ICProject project = null;
		if (localProject == null) {
			try {
				project = createProject(Platform.getBundle(PLUGIN), "localTestProject");
			} catch (Exception e) {
				fail("Failed to create local project for the tests: " + e.getMessage());
			}
			localProject = project.getProject();
			assertNotNull(localProject);
		}

		if (syncProject == null) {
			try {
				project = createProject(Platform.getBundle(PLUGIN), "syncTestProject");
				convertToSyncProject(project.getProject(), connection, "/tmp/" + PLUGIN);
			} catch (Exception e) {
				fail("Failed to create synchronized project for the tests: " + e.getMessage());
			}
			syncProject = project.getProject();
			assertNotNull(syncProject);
		}
	}
}
