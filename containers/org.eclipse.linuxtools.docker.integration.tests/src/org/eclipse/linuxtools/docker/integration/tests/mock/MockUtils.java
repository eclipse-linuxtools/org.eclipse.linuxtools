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
package org.eclipse.linuxtools.docker.integration.tests.mock;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.reddeer.eclipse.ui.browser.WebBrowserView;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerInfo;

public class MockUtils {

	public static void createDockerMockConnection(String connectionName) {
		DockerExplorerView de = new DockerExplorerView();
		de.open();
		File dockerSocketTmpFile = null;
		try {
			dockerSocketTmpFile = File.createTempFile("dockerMock", ".sock");
		} catch (IOException e) {
			new Exception("Cannot create mocked Docker connection!");
			e.printStackTrace();
		}
		MockDockerConnectionSettingsFinder.validUnixSocketConnectionAvailable(connectionName,
				"unix://" + dockerSocketTmpFile.getAbsolutePath());
		de.createDockerConnectionUnix(
				configureUnixSocketConnection(connectionName, "unix://" + dockerSocketTmpFile.getAbsolutePath()));
		assertTrue("Docker connection does not exist! ", de.connectionExistForName(connectionName));

	}

	private static IDockerConnection configureUnixSocketConnection(final String connectionName,
			final String pathToSocket) {
		DockerClient client = MockDockerClientFactory.build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(connectionName, client).withUnixSocketConnectionSettings(pathToSocket);
		DockerExplorerView de = new DockerExplorerView();
		de.open();
		de.createDockerConnectionUnix(dockerConnection);
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
		return dockerConnection;
	}

	public static void pullImage(String connectionName, String imageName, String imageTag) {
		final DockerClient client = MockDockerClientFactory
				.image(MockImageFactory.id("987654321abcde").name(imageName + ":" + imageTag).build()).build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(connectionName, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

	public static void removeImage(String connectionName, String imageName, String imageTag) {
		final DockerClient client = MockDockerClientFactory.build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(connectionName, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

	public static void runContainer(String connectionName, String imageName, String imageTag, String containerName) {
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name(containerName).status("Stopped").build())
				.image(MockImageFactory.id("987654321abcde").name(imageName + ":" + imageTag).build()).build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(connectionName, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

	public static void runContainer(String connectionName, String imageName, String imageTag, String containerName, ContainerInfo containerInfo) {
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name(containerName).status("Stopped").build(), MockContainerInfoFactory.link("alpine").volume("resources/test-volumes/index.html").id("TestTestTestTestTest")
						.ipAddress("127.0.0.1").build())
				.image(MockImageFactory.id("987654321abcde").name(imageName + ":" + imageTag).build()).build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(connectionName, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

	public static ConsoleView getConsoleViewText(String msg) {
		final ConsoleView cv = MockConsoleView.msg(msg).build();
		return cv;
	}

	public static WebBrowserView getBrowserView(String url, String text) {
		final WebBrowserView browser = MockBrowserView.openPageURL(url).setText(text).build();
		return browser;
	}

}
