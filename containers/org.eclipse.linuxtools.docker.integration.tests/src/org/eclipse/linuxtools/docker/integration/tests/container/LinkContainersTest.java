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

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerTerminal;
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerTerminal;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.eclipse.reddeer.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 *
 * @author jkopriva@redhat.com
 *
 */

public class LinkContainersTest extends AbstractImageBotTest {

	private static final String IMAGE_ALPINE_CURL = "byrnedo/alpine-curl";
	private static final String CONTAINER_NAME_HTTP_SERVER = "test_run_httpd";
	private static final String CONTAINER_NAME_CLIENT_ALPINE = "test_connect_httpd";

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_ALPINE_CURL);
		pullImage(IMAGE_UHTTPD);
	}

	private ImageRunSelectionPage openImageRunSelectionPage(String containerName, boolean publishAllExposedPorts, DockerImagesTab imagesTab) {
		ImageRunSelectionPage page = new ImageRunSelectionPage(imagesTab);
		page.setContainerName(containerName);
		page.setPublishAllExposedPorts(publishAllExposedPorts);
		return page;
	}

	@Test
	public void testLinkContainers() {
		runUhttpServer(IMAGE_UHTTPD, CONTAINER_NAME_HTTP_SERVER);
		runAlpineLinux(IMAGE_ALPINE_CURL, CONTAINER_NAME_CLIENT_ALPINE);

	}

	public void runUhttpServer(String imageName, String containerName) {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(imageName);
		ImageRunSelectionPage firstPage = openImageRunSelectionPage(containerName, false, imagesTab);
		firstPage.setContainerName(containerName);
		firstPage.setPublishAllExposedPorts(false);
		firstPage.finish();
		if (mockitoIsUsed()) {
			mockServerContainer();
			new WaitUntil(new ContainerIsDeployedCondition(containerName, getConnection()));
		} else {
			new WaitWhile(new JobIsRunning());
			new WaitWhile(new ConsoleHasNoChange());
		}
	}

	public void runAlpineLinux(String imageName, String containerName) {
		String serverAddress = getHttpServerAddress(CONTAINER_NAME_HTTP_SERVER);
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(imageName);
		ImageRunSelectionPage firstPage = openImageRunSelectionPage(containerName, false, imagesTab);
		firstPage.setContainerName(containerName);
		firstPage.setCommand(serverAddress + ":80");
		firstPage.addLinkToContainer(CONTAINER_NAME_HTTP_SERVER, "http_server");
		firstPage.setPublishAllExposedPorts(false);
		firstPage.setAllocatePseudoTTY();
		firstPage.setKeepSTDINOpen();
		firstPage.finish();
		new WaitWhile(new JobIsRunning());
		DockerTerminal dt = new DockerTerminal();
		dt.open();
		if (mockitoIsUsed()) {
			mockServerContainer();
			dt = MockDockerTerminal.setText("text").build();
		}
		String terminalText = dt.getTextFromPage("/" + containerName);
		assertTrue("No output from terminal!", !terminalText.isEmpty());
		assertTrue("Containers are not linked!", !terminalText.contains("Connection refused"));
	}

	private String getHttpServerAddress(String containerName) {
		PropertySheet propertiesView = new PropertySheet();
		propertiesView.open();
		getConnection().getContainer(containerName).select();
		propertiesView.selectTab("Inspect");
		return propertiesView.getProperty("NetworkSettings", "IPAddress").getPropertyValue();
	}

	@Override
	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME_CLIENT_ALPINE);
		deleteContainerIfExists(CONTAINER_NAME_HTTP_SERVER);
		deleteImageIfExists(IMAGE_ALPINE_CURL);
	}

	private void mockServerContainer() {
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name(CONTAINER_NAME_HTTP_SERVER).status("Stopped").build(),
						MockContainerInfoFactory.link(IMAGE_ALPINE).privilegedMode(true).id("TestServerTestServer")
								.ipAddress("127.0.0.1").build())
				.image(MockImageFactory.id("1a2b3c4d5e6f7g").name(IMAGE_UHTTPD + ":" + IMAGE_TAG_LATEST).build())
				.image(MockImageFactory.id("1a2b3c4d5e6f7g").name(IMAGE_ALPINE_CURL + ":" + IMAGE_TAG_LATEST).build())
				.build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(DEFAULT_CONNECTION_NAME, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}
}