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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunResourceVolumesVariablesPage;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.jboss.reddeer.eclipse.ui.browser.BrowserView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
/**
 * 
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 */
public class VolumeMountTest extends AbstractImageBotTest {

	private static final String CONTAINER_NAME = "test_mount_volumes";
	private static final String VOLUME_PATH = "resources/test-volumes";
	private static final String CONTAINER_PATH = "/www";
	private static final String INDEX_PAGE = "index.html";
	private static final String INDEX_PAGE_PATH = VOLUME_PATH + "/" + INDEX_PAGE;
	private static final String HOST_PORT = "80";

	@Before
	public void before() throws DockerException, InterruptedException {
		clearConsole();
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_UHTTPD, IMAGE_TAG_LATEST);
	}

	@Test
	public void testVolumeMount() throws IOException {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(IMAGE_UHTTPD + ":" + IMAGE_TAG_LATEST);

		ImageRunSelectionPage firstPage = new ImageRunSelectionPage();
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.setPublishAllExposedPorts(true);
		firstPage.next();

		ImageRunResourceVolumesVariablesPage secondPage = new ImageRunResourceVolumesVariablesPage();
		String volumePath = (new File(VOLUME_PATH)).getCanonicalPath();
		secondPage.addDataVolumeToHost(CONTAINER_PATH, volumePath);
		secondPage.finish();
		new WaitWhile(new JobIsRunning());

		if (!mockitoIsUsed()) {
			new WaitWhile(new ConsoleHasNoChange());
		} else {
			runServer();
			new WaitUntil(new ContainerIsDeployedCondition(CONTAINER_NAME, getConnection()));
		}

		String indexPage = getIndexPageContent();
		String indexPageResource = getResourceAsString(INDEX_PAGE_PATH);
		assertEquals(INDEX_PAGE_PATH + " wasnt mounted/displayed properly.", indexPage, indexPageResource);
	}

	private String getIndexPageContent() throws IOException {
		String containerIP = getContainerIP(CONTAINER_NAME);
		String url = "http://" + containerIP + ":" + HOST_PORT + "/" + INDEX_PAGE;
		BrowserView browserView = new BrowserView();
		browserView.open();
		if (mockitoIsUsed()) {
			browserView = MockUtils.getBrowserView(INDEX_PAGE_PATH, getResourceAsString(INDEX_PAGE_PATH));
		}
		browserView.openPageURL(url);
		return browserView.getText();
	}

	private void runServer() {
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name(CONTAINER_NAME).status("Stopped").build(),
						MockContainerInfoFactory.link(IMAGE_ALPINE).volume(VOLUME_PATH).id("TestTestTestTestTest")
								.ipAddress("127.0.0.1").build())
				.image(MockImageFactory.id("987654321abcde").name(IMAGE_UHTTPD + ":" + IMAGE_TAG_LATEST).build())
				.build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(DEFAULT_CONNECTION_NAME, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME);
	}

}