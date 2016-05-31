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
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
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
	public void lookupDockerExplorerView() {
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
	public void shouldRetrieveConnectionFromSelectedContainersCategory() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem containers = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers");
		// when selecting the container
		containers.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainer() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem container = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar");
		// when selecting the container
		container.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainerLinksCategory() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build(),
						MockContainerInfoFactory.link("/foo:/bar/foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem links = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Links");
		// when
		links.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedContainerLink() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build(),
						MockContainerInfoFactory.link("/foo:/bar/foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem link = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Links", "foo (foo)");
		// when selecting the Link
		link.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedContainerVolumesCategory() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build(),
						MockContainerInfoFactory.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem volumes = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Volumes");
		volumes.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainerVolume() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build(),
						MockContainerInfoFactory.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem volume = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Volumes", "/path/to/host");
		// when selecting the volume
		volume.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

	@Test
	public void shouldRetrieveConnectionFromSelectedContainerPortsCategory() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build(),
						MockContainerInfoFactory.port("8080/tcp", "0.0.0.0", "8080").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem ports = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Ports");
		// when selecting the port
		ports.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedContainerPort() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build(),
						MockContainerInfoFactory.port("8080/tcp", "0.0.0.0", "8080").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem port = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Ports", "0.0.0.0:8080 -> 8080 (tcp)");
		// when selecting the port
		port.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedImagesCategory() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem images = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Images");
		// when selecting the images category
		images.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}
	
	@Test
	public void shouldRetrieveConnectionFromSelectedImage() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem image = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Images", "foo");
		// when selecting the image
		image.select();
		// then current connection should be found
		Assertions.assertThat(CommandUtils.getCurrentConnection(dockerExplorerView)).isEqualTo(dockerConnection);
	}

}
