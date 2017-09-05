/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests.ui;

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.ui.RunDockerImageLaunchConfiguration;
import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 */

public class LaunchDockerImageTest extends AbstractImageBotTest {

	private static final String CONTAINER_NAME = "test_variables";
	private static final String CONFIGURATION_NAME = "test_configuration";

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_HELLO_WORLD);
	}

	@Test
	public void testLaunchConfiguration() {
		String imageName = getCompleteImageName(IMAGE_HELLO_WORLD);

		RunDockerImageLaunchConfiguration runImageConf = new RunDockerImageLaunchConfiguration();
		try {
			runDockerImageLaunchConfiguration(imageName + NAME_TAG_SEPARATOR + IMAGE_TAG_LATEST, CONTAINER_NAME,
					CONFIGURATION_NAME, runImageConf);
			if (mockitoIsUsed()) {
				MockUtils.runContainer(DEFAULT_CONNECTION_NAME, imageName, IMAGE_TAG_LATEST, CONTAINER_NAME);
				getConnection().refresh();
				new WaitUntil(new ContainerIsDeployedCondition(CONTAINER_NAME, getConnection()));
			}
		} catch (WaitTimeoutExpiredException ex) {
				throw ex;
		}
		assertTrue("Container is not deployed!", containerIsDeployed(CONTAINER_NAME));
	}

	private void runDockerImageLaunchConfiguration(String imageName, String containerName, String configurationName,
			RunDockerImageLaunchConfiguration runImageConf) {
		runImageConf.open();
		runImageConf.createNewConfiguration(configurationName);
		runImageConf.setContainerName(containerName);
		runImageConf.selectImage(imageName);
		runImageConf.setPrivilegedMode(true);
		runImageConf.apply();
		runImageConf.runConfiguration(configurationName);
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
	}

	private void deleteIfExists(String configurationName) {
		RunDockerImageLaunchConfiguration runImageConf = new RunDockerImageLaunchConfiguration();
		try {
			runImageConf.open();
			runImageConf.deleteRunConfiguration(configurationName);
			runImageConf.close();
		} catch (RedDeerException e) {
			// catched intentionally
		}
	}

	@After
	public void after() {
		WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
		deleteIfExists(CONFIGURATION_NAME);
		deleteImageContainerAfter(CONTAINER_NAME);
	}

}