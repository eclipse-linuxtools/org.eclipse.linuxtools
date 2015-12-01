/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import org.assertj.core.api.Assertions;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * 
 */
public class CommandUtilsSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerExplorerViewBot;
	private DockerExplorerView dockerExplorerView;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(); 
	
	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();
	
	@Before
	public void lookupDockerExplorerView() throws InterruptedException {
		SWTUtils.asyncExec(() -> {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
			}
		});
		dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		bot.views().stream()
				.filter(v -> v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerContainersView")
						|| v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerImagesView"))
				.forEach(v -> v.close());
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainersCategory() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when selecting the container
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Containers").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainer() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when selecting the container
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Containers", "foo_bar").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainerLinksCategory() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.link("/foo:/bar/foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		// when selecting the port
		SWTUtils.getTreeItem(containerTreeItem, "Links").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedContainerLink() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.link("/foo:/bar/foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Links").expand());
		// when selecting the port
		SWTUtils.getTreeItem(containerTreeItem, "Links", "foo (foo)").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedContainerVolumesCategory() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Volumes").expand());
		// when selecting the port
		SWTUtils.getTreeItem(containerTreeItem, "Volumes").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainerVolume() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Volumes").expand());
		// when selecting the volume
		SWTUtils.getTreeItem(containerTreeItem, "Volumes", "/path/to/host").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainerPortsCategory() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.port("8080/tcp", "0.0.0.0", "8080").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		// when selecting the port
		SWTUtils.getTreeItem(containerTreeItem, "Ports").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedContainerPort() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.port("8080/tcp", "0.0.0.0", "8080").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Ports").expand());
		// when selecting the port
		SWTUtils.getTreeItem(containerTreeItem, "Ports", "0.0.0.0:8080 -> 8080 (tcp)").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedImagesCategory() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockDockerImageFactory.name("foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when selecting the images category
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedImage() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockDockerImageFactory.name("foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when selecting the image
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo").select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

}
