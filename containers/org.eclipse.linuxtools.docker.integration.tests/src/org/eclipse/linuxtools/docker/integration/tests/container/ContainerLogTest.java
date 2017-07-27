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

import org.apache.commons.lang.StringUtils;
import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerTerminal;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
import org.jboss.reddeer.swt.api.CTabItem;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

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
		getConnection().refresh();
		getConnection().getImage(IMAGE_NAME).run();
		ImageRunSelectionPage firstPage = new ImageRunSelectionPage();
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.setAllocatePseudoTTY();
		firstPage.setKeepSTDINOpen();
		firstPage.finish();
		if (mockitoIsUsed()) {
			runContainer();
			getConnection().refresh();
			new WaitUntil(new ContainerIsDeployedCondition(CONTAINER_NAME, getConnection()));
		}
		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);
	}

	@Test
	public void testDislayLog() {
		getConnection().getContainer(CONTAINER_NAME).select();
		String consoleText = getContainerLog();
		assertTrue("Log for container:" + CONTAINER_NAME + " is empty!", StringUtils.isNotEmpty(consoleText));
	}

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
		new ContextMenu("Display Log").select();
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