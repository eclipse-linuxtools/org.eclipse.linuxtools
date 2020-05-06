/*******************************************************************************
 * Copyright (c) 2017, 2020 Red Hat, Inc.
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

package org.eclipse.linuxtools.docker.integration.tests.container;

import java.util.Arrays;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunResourceVolumesVariablesPage;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageInfoFactory;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.exceptions.DockerException;
import org.mandas.docker.client.messages.Container;
import org.mandas.docker.client.messages.ContainerInfo;
import org.mandas.docker.client.messages.Image;
import org.mandas.docker.client.messages.ImageInfo;

public class VariablesTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = "test_variables";
	private static final String CONTAINER_NAME = "run_" + IMAGE_NAME;

	private DockerClient client;
	private Container createdContainer;
	private ContainerInfo containerInfo;

	@Before
	public void before() throws DockerException, InterruptedException {
		if (mockitoIsUsed()) {
			setUpForMockito();
		}

	}

	@Test
	public void testVariables() {
		getConnection();
		DockerImagesTab imagesTab = openDockerImagesTab();
		buildImage(IMAGE_NAME, "resources/test-variables", imagesTab);
		if (mockitoIsUsed()) {
			MockUtils.pullImage(DEFAULT_CONNECTION_NAME, IMAGE_NAME, IMAGE_TAG_LATEST);
		}
		assertConsoleSuccess();

		imagesTab.activate();
		imagesTab.refresh();
		new WaitWhile(new JobIsRunning());

		imagesTab.runImage(IMAGE_NAME);
		ImageRunSelectionPage firstPage = new ImageRunSelectionPage(imagesTab);
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.next();
		ImageRunResourceVolumesVariablesPage secondPage = new ImageRunResourceVolumesVariablesPage(firstPage);
		secondPage.addEnviromentVariable("FOO", "barbarbar");
		if (mockitoIsUsed()) {
			MockDockerClientFactory.addContainer(this.client, this.createdContainer, this.containerInfo);
		}
		secondPage.finish();
		new WaitWhile(new JobIsRunning());
		assertConsoleContains("FOO is barbarbar");
	}

	@Override
	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME);
		deleteImageIfExists(IMAGE_NAME);
	}

	private void setUpForMockito() throws DockerException, InterruptedException {
		// images to use
		final Image image = MockImageFactory.id("1a2b3c4d5e6f7g").name(IMAGE_UHTTPD + ":" + IMAGE_TAG_LATEST).build();
		final ImageInfo imageInfo = MockImageInfoFactory.volume("/foo/bar").command(Arrays.asList("the", "command"))
				.entrypoint(Arrays.asList("the", "entrypoint")).env(Arrays.asList("FOO", "barbarbar")).build();
		// container to be created
		this.createdContainer = MockContainerFactory.id("1MockContainer").name(CONTAINER_NAME)
				.imageName("1a2b3c4d5e6f7g").status("Started 1 second ago").build();
		this.containerInfo = MockContainerInfoFactory.privilegedMode(true).id("TestTestTestTestTest")
				.ipAddress("127.0.0.1").build();
		this.client = MockDockerClientFactory.image(image, imageInfo).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(DEFAULT_CONNECTION_NAME, client)
				.withDefaultTCPConnectionSettings();
		// configure the Connection Manager
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

}