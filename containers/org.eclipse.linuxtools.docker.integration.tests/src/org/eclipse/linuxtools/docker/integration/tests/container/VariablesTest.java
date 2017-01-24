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
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageInfo;

/**
 * 
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 */

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
		ImageRunSelectionPage firstPage = new ImageRunSelectionPage();
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.next();
		ImageRunResourceVolumesVariablesPage secondPage = new ImageRunResourceVolumesVariablesPage();
		secondPage.addEnviromentVariable("FOO", "barbarbar");
		if (mockitoIsUsed()) {
			MockDockerClientFactory.addContainer(this.client, this.createdContainer, this.containerInfo);
		}
		secondPage.finish();
		new WaitWhile(new JobIsRunning());
		assertConsoleContains("FOO is barbarbar");
	}

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