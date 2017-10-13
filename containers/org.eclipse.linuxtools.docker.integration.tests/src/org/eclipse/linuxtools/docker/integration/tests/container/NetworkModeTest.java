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
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunNetworkPage;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunResourceVolumesVariablesPage;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

/**
 *
 * @author jkopriva@redhat.com
 *
 */
public class NetworkModeTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = "docker.io/" + IMAGE_BUSYBOX;
	private static final String IMAGE_TAG = IMAGE_TAG_LATEST;
	private static final String CONTAINER_NAME = "test_run_busybox";
	private static final String NETWORK_MODE_DEFAULT = "default";
	private static final String NETWORK_MODE_BRIDGE = "bridge";
	private static final String NETWORK_MODE_HOST = "host";
	private static final String NETWORK_MODE_NONE = "none";

	ImageRunSelectionPage firstPage;

	@Before
	public void before() throws DockerException, InterruptedException {
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_NAME, IMAGE_TAG);
		new WaitWhile(new JobIsRunning());
		DockerExplorerView explorer = new DockerExplorerView();
		getConnection().getImage(IMAGE_NAME).run();
		firstPage = new ImageRunSelectionPage(explorer);
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.setAllocatePseudoTTY();
		firstPage.setKeepSTDINOpen();
		firstPage.next();
		ImageRunResourceVolumesVariablesPage variablesPage = new ImageRunResourceVolumesVariablesPage(firstPage);
		variablesPage.next();
	}

	@Test
	public void testDefaultMode() {
		ImageRunNetworkPage networkPage = new ImageRunNetworkPage(firstPage);
		networkPage.setDefaultNetworkMode();
		networkPage.finish();
		checkNetworkMode(NETWORK_MODE_DEFAULT);
	}

	@Test
	public void testBridgeMode() {
		ImageRunNetworkPage networkPage = new ImageRunNetworkPage(firstPage);
		networkPage.setBridgeNetworkMode();
		networkPage.finish();
		checkNetworkMode(NETWORK_MODE_BRIDGE);
	}

	@Test
	public void testHostMode() {
		ImageRunNetworkPage networkPage = new ImageRunNetworkPage(firstPage);
		networkPage.setHostNetworkMode();
		networkPage.finish();
		checkNetworkMode(NETWORK_MODE_HOST);
	}

	@Test
	public void testNoneMode() {
		ImageRunNetworkPage networkPage = new ImageRunNetworkPage(firstPage);
		networkPage.setNoneNetworkMode();
		networkPage.finish();
		checkNetworkMode(NETWORK_MODE_NONE);
	}

	@Override
	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME);
	}

	private void runContainer(String networkMode) {
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name(CONTAINER_NAME).status("Stopped").build(),
						MockContainerInfoFactory.link(IMAGE_NAME + ":" + IMAGE_TAG_LATEST).networkMode(networkMode)
								.id("TestTestTestTestTest").ipAddress("127.0.0.1").build())
				.image(MockImageFactory.id("987654321abcde").name(IMAGE_UHTTPD + ":" + IMAGE_TAG_LATEST).build())
				.build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(DEFAULT_CONNECTION_NAME, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

	private void checkNetworkMode(String networkMode) {
		if (mockitoIsUsed()) {
			runContainer(networkMode);
			getConnection().refresh();
			new WaitUntil(new ContainerIsDeployedCondition(CONTAINER_NAME, getConnection()));
		}
		new WaitWhile(new JobIsRunning());
		PropertySheet propertiesView = openPropertiesTabForContainer("Inspect", CONTAINER_NAME);
		String networkProp = propertiesView.getProperty("HostConfig", "NetworkMode").getPropertyValue();
		assertTrue("Container is not running in " + networkMode + " network mode!", networkProp.equals(networkMode));
	}
}