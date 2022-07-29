/*******************************************************************************
 * Copyright (c) 2017,2022 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests.image;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.StringUtils;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PushImageTest extends AbstractImageBotTest {

	private static final String DOCKERFILE_FOLDER = "resources/test-variables";
	private static final String DOCKER_HUB_PASSWORD = "dockerHubPassword";
	private static final String DOCKER_HUB_EMAIL = "dockerHubEmail";
	private static final String DOCKER_HUB_USERNAME = "dockerHubUsername";

	private static final String IMAGE_NAME = "test_push";
	private static final String REGISTRY_ACCOUNT = System.getProperty(DOCKER_HUB_USERNAME) + "@https://index.docker.io";
	private static String IMAGE_TAG = System.getProperty(DOCKER_HUB_USERNAME) + "/variables";

	private String imageNewTag = "";

	private String dockerHubUsername = "";
	private String dockerHubEmail = "";
	private String dockerHubPassword = "";

	@Before
	public void before() {
		deleteAllConnections();
		createConnection();
		checkCredentials();
		if (mockitoIsUsed()) {
			//Set up for Mockito
			MockUtils.pullImage(DEFAULT_CONNECTION_NAME, IMAGE_NAME, imageNewTag);
			IMAGE_TAG = "test_push:/variables";
			getConnection().refresh();
		}
	}

	@Test
	public void pushImage() {
		DockerImagesTab imagesTab = openDockerImagesTab();
		buildImage(IMAGE_NAME, DOCKERFILE_FOLDER, imagesTab);
		assertConsoleSuccess();

		setUpRegister(REGISTRY_URL, dockerHubEmail, dockerHubUsername, dockerHubPassword);
		setSecureStorage("password");
		String seconds = String.valueOf(new java.util.Date().getTime());
		this.imageNewTag = IMAGE_TAG + ":" + seconds;
		addTagToImage(IMAGE_NAME, imageNewTag);

		//new WaitUntil(new JobIsRunning(), TimePeriod.VERY_LONG);
		if (!mockitoIsUsed()) {
			getConnection().getImage(IMAGE_TAG, seconds).pushImage(REGISTRY_ACCOUNT, false, false);
		}
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		deleteImage(IMAGE_TAG, seconds);
		pullImage(IMAGE_TAG, seconds, REGISTRY_URL);
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		assertTrue("Image has not been pushed/pulled!", imageIsDeployed(IMAGE_TAG));
	}

	private void checkCredentials(){
		dockerHubUsername = System.getProperty(DOCKER_HUB_USERNAME);
		dockerHubEmail = System.getProperty(DOCKER_HUB_EMAIL);
		dockerHubPassword = System.getProperty(DOCKER_HUB_PASSWORD);

		if (!mockitoIsUsed()){
			assertFalse("At least one of credentials is null or empty! " + "dockerHubUsername:" + dockerHubUsername
					+ " dockerHubEmail:" + dockerHubEmail + " dockerHubPassword:" + dockerHubPassword + " Aborting test...",
					StringUtils.isBlank(dockerHubUsername) || StringUtils.isBlank(dockerHubEmail)
					|| StringUtils.isBlank(dockerHubPassword));
		} else {
			dockerHubUsername = "user";
			dockerHubPassword = "password";
			dockerHubEmail = "some@email.com";
		}
	}

	@Override
	@After
	public void after() {
		deleteRegister(REGISTRY_URL);
		deleteImageContainerAfter(imageNewTag);
		cleanUpWorkspace();
	}
}