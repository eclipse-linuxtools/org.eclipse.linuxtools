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

package org.eclipse.linuxtools.docker.integration.tests.container;

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.StringUtils;
import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerTerminal;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.swt.api.CTabItem;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mandas.docker.client.DockerClient;

public class ContainerLogTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = "docker.io/" + IMAGE_HELLO_WORLD;
	private static final String CONTAINER_NAME = "test_run";
	private static final String CONSOLE_TEXT = "test_run Console output";

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_NAME);
		new WaitWhile(new JobIsRunning());
		DockerExplorerView explorer = new DockerExplorerView();
		explorer.open();
		getConnection().refresh();
		getConnection().getImage(IMAGE_NAME).run();
		ImageRunSelectionPage firstPage = new ImageRunSelectionPage(explorer);
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.setAllocatePseudoTTY();
		firstPage.setKeepSTDINOpen();
		firstPage.finish();
		if (mockitoIsUsed()) {
			runContainer();
			getConnection().refresh();
			new WaitUntil(new ContainerIsDeployedCondition(CONTAINER_NAME, getConnection()));
		}
		new WaitWhile(new JobIsRunning(), TimePeriod.DEFAULT);
	}

	@Test
	public void testDislayLog() {
		getConnection().getContainer(CONTAINER_NAME).select();
		String consoleText = getContainerLog();
		assertTrue("Log for container:" + CONTAINER_NAME + " is empty!", StringUtils.isNotEmpty(consoleText));
	}

	@Override
	@After
	public void after() {
		deleteImageContainerAfter(CONTAINER_NAME);
		deleteImageContainerAfter(IMAGE_NAME);
		cleanDockerTerminal();
	}

	private void runContainer() {
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name(CONTAINER_NAME).status("Stopped").build(),
						MockContainerInfoFactory.link(IMAGE_NAME + ":" + IMAGE_TAG_LATEST).id("TestTestTestTestTest")
								.ipAddress("127.0.0.1").build())
				.image(MockImageFactory.id("987654321abcde").name(IMAGE_UHTTPD + ":" + IMAGE_TAG_LATEST).build())
				.build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(DEFAULT_CONNECTION_NAME, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

	private String getContainerLog() {
		new ContextMenu().getItem("Display Log").select();
		String consoleText;
		if (mockitoIsUsed()) {
			ConsoleView consoleView = new ConsoleView();
			consoleView.open();
			consoleView = MockUtils.getConsoleViewText(CONSOLE_TEXT);
			consoleText = consoleView.getConsoleText();
		} else {
			DockerTerminal dockerTerminal = new DockerTerminal();
			dockerTerminal.activate();
			consoleText = dockerTerminal.getTextFromPage(CONTAINER_NAME);
		}
		return consoleText;
	}

	private void cleanDockerTerminal () {
		if (!mockitoIsUsed()) {
			DockerTerminal dockerTerminal = new DockerTerminal();
			dockerTerminal.activate();
			CTabItem tabItem = dockerTerminal.getPage(CONTAINER_NAME);
			tabItem.close();
			dockerTerminal.close();
		}
	}

}