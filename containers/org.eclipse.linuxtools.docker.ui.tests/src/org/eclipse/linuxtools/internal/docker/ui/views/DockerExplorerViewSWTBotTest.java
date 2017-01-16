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

package org.eclipse.linuxtools.internal.docker.ui.views;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerExplorerViewAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.MenuAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTBotTreeItemAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TabDescriptorAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TestLoggerRule;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

/**
 * Testing the {@link DockerExplorerView} {@link Viewer}
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class DockerExplorerViewSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerExplorerViewBot;
	private DockerExplorerView dockerExplorerView;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public TestLoggerRule watcher = new TestLoggerRule();

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Before
	public void setup() {
		this.bot = new SWTWorkbenchBot();
		SWTUtils.asyncExec(() -> {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(DockerExplorerView.VIEW_ID);
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
			}
		});
		this.dockerExplorerViewBot = bot.viewById(DockerExplorerView.VIEW_ID);
		this.dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		this.bot.views().stream()
				.filter(v -> v.getReference().getId().equals(DockerContainersView.VIEW_ID)
						|| v.getReference().getId().equals(DockerImagesView.VIEW_ID)
						|| v.getReference().getId().equals("org.eclipse.ui.views.PropertySheet"))
				.forEach(v -> v.close());
	}

	@After
	public void hideMenu() {
		try {
			SWTUtils.hideMenu(dockerExplorerViewBot.bot().tree());
		} catch (WidgetNotFoundException e) {
			// ignore if widget is not found, that's probably because there's no
			// tree in the
			// Docker Explorer view for the test that just ran.
		}
	}

	private SWTBotTreeItem selectConnectionInTreeView(final String connectionName) {
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, connectionName);
		return connectionTreeItem.select();
	}

	private void selectContainersInTreeView(final String connectionName, final String... containerNames) {
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// When a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containersTreeItem = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), connectionName,
				"Containers");
		// select both containers
		SWTUtils.select(containersTreeItem, containerNames);
	}

	private void selectImagesInTreeView(final String connectionName, final String... imageNames) {
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, connectionName, "Images");
		SWTUtils.asyncExec(() -> imagesTreeItem.expand());
		// select both images
		SWTUtils.select(imagesTreeItem, imageNames);
	}

	@Test
	public void shouldDisplayExplanationPane() {
		// when
		ClearConnectionManagerRule.removeAllConnections(DockerConnectionManager.getInstance());
		// then
		DockerExplorerViewAssertions.assertThat(dockerExplorerView).isEmpty();
	}

	@Test
	public void shouldDisplayConnectionsPane() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// then
		DockerExplorerViewAssertions.assertThat(dockerExplorerView).isNotEmpty();
	}

	@Test
	public void shouldRefreshImagesAndShowChanges() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// one connection
		final SWTBotTreeItem connection = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test");
		// "containers" and "images" items
		Assertions.assertThat(connection.getItems()).hasSize(2);
		final SWTBotTreeItem imagesTreeItem = SWTUtils.expand(connection, "Images");
		Assertions.assertThat(imagesTreeItem.getItems().length).isEqualTo(0);
		// update the client
		final DockerClient updatedClient = MockDockerClientFactory.image(MockImageFactory.name("foo/bar").build())
				.build();
		dockerConnection.setClient(updatedClient);
		// when locating the 'Images' node and hit refresh
		dockerExplorerViewBot.bot().tree().select(imagesTreeItem);
		dockerExplorerViewBot.bot().tree().contextMenu("Refresh").click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		imagesTreeItem.expand();
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		// then check that there are images now
		Assertions.assertThat(imagesTreeItem.isExpanded()).isTrue();
		Assertions.assertThat(imagesTreeItem.getItems().length).isEqualTo(1);
	}

	@Test
	public void shouldRefreshContainersAndShowChanges() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		// one connection
		final SWTBotTreeItem connection = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test");
		// "containers" and "images" items
		Assertions.assertThat(connection.getItems()).hasSize(2);
		final SWTBotTreeItem containersTreeItem = SWTUtils.expand(connection, "Containers");
		Assertions.assertThat(containersTreeItem.getItems().length).isEqualTo(0);

		// update the client
		final DockerClient updatedClient = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build()).build();
		dockerConnection.setClient(updatedClient);
		dockerExplorerViewBot.bot().tree().select(containersTreeItem);
		dockerExplorerViewBot.bot().tree().contextMenu("Refresh").click();
		SWTUtils.asyncExec(() -> containersTreeItem.expand());

		// then check that there are images now
		Assertions.assertThat(containersTreeItem.isExpanded()).isTrue();
		Assertions.assertThat(containersTreeItem.getItems().length).isEqualTo(1);
	}

	@Test
	public void shouldShowContainerPortMapping() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build(), MockContainerInfoFactory
						.port("8080/tcp", "0.0.0.0", "8080").port("8787/tcp", "0.0.0.0", "8787").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		final SWTBotTreeItem containerPorts = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Ports");
		// then
		SWTBotTreeItemAssertions.assertThat(containerPorts).isExpanded().hasChildItems(2);
		SWTBotTreeItemAssertions.assertThat(containerPorts.getNode(0)).hasText("0.0.0.0:8080 -> 8080 (tcp)");
		SWTBotTreeItemAssertions.assertThat(containerPorts.getNode(1)).hasText("0.0.0.0:8787 -> 8787 (tcp)");
	}

	@Test
	public void shouldShowContainerLinks() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("foo_bar").build(), MockContainerInfoFactory
						.link("/postgres-demo:/foo_bar/postgres1").link("/postgres-demo:/foo_bar/postgres2").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		final SWTBotTreeItem containerLinks = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Links");
		// then
		SWTBotTreeItemAssertions.assertThat(containerLinks).isExpanded().hasChildItems(2);
		SWTBotTreeItemAssertions.assertThat(containerLinks.getNode(0)).hasText("postgres-demo (postgres1)");
		SWTBotTreeItemAssertions.assertThat(containerLinks.getNode(1)).hasText("postgres-demo (postgres2)");
	}

	@Test
	public void shouldShowContainerVolumes() {
		// given
		final DockerClient client = MockDockerClientFactory.container(MockContainerFactory.name("foo_bar").build(),
				MockContainerInfoFactory.volume("/path/to/container").volume("/path/to/host:/path/to/container")
						.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when
		final SWTBotTreeItem volumesTreeItem = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers",
				"foo_bar", "Volumes");
		// then
		SWTBotTreeItemAssertions.assertThat(volumesTreeItem).isExpanded().hasChildItems(3);
		SWTBotTreeItemAssertions.assertThat(volumesTreeItem.getNode(0)).hasText("/path/to/container");
		SWTBotTreeItemAssertions.assertThat(volumesTreeItem.getNode(1)).hasText("/path/to/host -> /path/to/container");
		SWTBotTreeItemAssertions.assertThat(volumesTreeItem.getNode(2))
				.hasText("/path/to/host -> /path/to/container (Z,ro)");
	}

	@Test
	public void shouldRemainExpandedAfterRefreshOnContainersCategory() {
		// given
		final DockerClient client = MockDockerClientFactory.container(MockContainerFactory.name("foo_bar").build(),
				MockContainerInfoFactory.volume("/path/to/container").port("8080/tcp", "0.0.0.0", "8080")
						.link("/foo:/bar/foo").volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final SWTBotTreeItem containers = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test", "Containers");
		final SWTBotTreeItem containerLinks = SWTUtils.expand(containers, "foo_bar", "Links");
		final SWTBotTreeItem containerPorts = SWTUtils.expand(containers, "foo_bar", "Ports");
		final SWTBotTreeItem containerVolumes = SWTUtils.expand(containers, "foo_bar", "Volumes");
		// ensure items are actually expanded before calling the 'refresh'
		// command
		SWTBotTreeItemAssertions.assertThat(containerLinks).isExpanded();
		SWTBotTreeItemAssertions.assertThat(containerPorts).isExpanded();
		SWTBotTreeItemAssertions.assertThat(containerVolumes).isExpanded();
		// when refreshing the container
		dockerExplorerViewBot.bot().tree().select(containers);
		dockerExplorerViewBot.bot().tree().contextMenu("Refresh").click();
		SWTUtils.asyncExec(() -> containers.expand());
		// then all items should remain expanded (after they were reloaded)
		SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containers, "foo_bar", "Links")).isExpanded();
		SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containers, "foo_bar", "Ports")).isExpanded();
		SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containers, "foo_bar", "Volumes")).isExpanded();
	}

	@Test
	public void shouldProvideEnabledStartCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Start");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideDisabledStartCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Start");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isNotEnabled();
	}

	@Test
	public void shouldProvideEnabledRestartCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockContainerFactory.name("bold_eagle").status("Stopped").build())
				.container(MockContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "bold_eagle", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Restart");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideDisabledStopCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Stop");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isNotEnabled();
	}

	@Test
	public void shouldProvideEnabledPauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Pause");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideDisabledPauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = SWTUtils.getContextMenu(dockerExplorerViewBot.bot().tree(), "Pause");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isNotEnabled();
	}

	@Test
	public void shouldProvideEnabledUnpauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockContainerFactory.name("angry_bar").status("Up (Paused)").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Unpause");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideDisabledUnpauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Unpause");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isNotEnabled();
	}

	@Test
	public void shouldProvideEnabledKillCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Kill");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideDisabledKillCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Kill");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isNotEnabled();
	}

	@Test
	public void shouldProvideEnabledRemoveCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Remove");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideDisabledRemoveCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Remove");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isNotEnabled();
	}

	@Test
	public void shouldProvideEnabledRemoveCommandOnMultipleImagesAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("angry_bar").build())
				.image(MockImageFactory.name("gentle_foo").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectImagesInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Remove");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isEnabled();
	}

	@Test
	public void shouldShowSelectedConnectionInPropertiesView() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one the container
		selectConnectionInTreeView("Test");
		// show container info in Properties view
		SWTUtils.getContextMenu(dockerExplorerViewBot.bot().tree(), "Show In", "Properties").click();
		// the properties view should be visible
		assertThat(this.bot.viewById("org.eclipse.ui.views.PropertySheet").isActive()).isEqualTo(true);
		SWTBotView propertiesViewBot = bot.viewById("org.eclipse.ui.views.PropertySheet");
		SWTUtils.getContextMenu(propertiesViewBot.bot().tree().select(1), "Copy").click();
		this.dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
	}

	@Test
	public void shouldShowSelectedContainerInPropertiesView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one the container
		selectContainersInTreeView("Test", "angry_bar");
		// show container info in Properties view
		SWTUtils.getContextMenu(dockerExplorerViewBot.bot().tree(), "Show In", "Properties").click();
		// the properties view should be visible
		assertThat(this.bot.viewById("org.eclipse.ui.views.PropertySheet").isActive()).isEqualTo(true);
		// then the properties view should have a selected tab with container
		// info
		final PropertySheet propertySheet = SWTUtils
				.syncExec(() -> SWTUtils.getView(bot, "org.eclipse.ui.views.PropertySheet", true));
		assertThat(propertySheet.getCurrentPage()).isInstanceOf(TabbedPropertySheetPage.class);
		final TabbedPropertySheetPage currentPage = (TabbedPropertySheetPage) propertySheet.getCurrentPage();
		TabDescriptorAssertions.assertThat(currentPage.getSelectedTab()).isNotNull()
				.hasId("org.eclipse.linuxtools.docker.ui.properties.container.info");
	}

	@Test
	public void shouldShowSelectedImageInPropertiesView() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("angry_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one the container
		selectImagesInTreeView("Test", "angry_bar");
		// show images info in Properties view
		SWTUtils.getContextMenu(dockerExplorerViewBot.bot().tree(), "Show In", "Properties").click();
		// the properties view should be visible
		assertThat(this.bot.viewById("org.eclipse.ui.views.PropertySheet").isActive()).isEqualTo(true);
		// then the properties view should have a selected tab with container
		// info
		final PropertySheet propertySheet = SWTUtils
				.syncExec(() -> SWTUtils.getView(bot, "org.eclipse.ui.views.PropertySheet", true));
		assertThat(propertySheet.getCurrentPage()).isInstanceOf(TabbedPropertySheetPage.class);
		final TabbedPropertySheetPage currentPage = (TabbedPropertySheetPage) propertySheet.getCurrentPage();
		TabDescriptorAssertions.assertThat(currentPage.getSelectedTab()).isNotNull()
				.hasId("org.eclipse.linuxtools.docker.ui.properties.image.info");
	}

	@Test
	public void shouldRemoveListenersWhenClosingView() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("angry_bar").build())
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// remove the DockerContainerRefreshManager
		dockerConnection.removeContainerListener(DockerContainerRefreshManager.getInstance());
		// DockerExplorerView inner classes
		assertThat(dockerConnection.getContainerListeners()).hasSize(1);
		assertThat(dockerConnection.getImageListeners()).hasSize(1);
		// close the Docker Explorer View
		dockerExplorerViewBot.close();
		// there should be no listener left
		assertThat(dockerConnection.getContainerListeners()).hasSize(0);
		assertThat(dockerConnection.getImageListeners()).hasSize(0);
	}

	@Test
	public void shouldShowAllImageVariants() {
		// given
		final DockerClient client = MockDockerClientFactory.image(
				MockImageFactory.id("1a2b3c4d5e6f7g").name("foo:1.0", "foo:latest", "bar:1.0", "bar:latest").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		// when
		SWTUtils.asyncExec(() -> imagesTreeItem.expand());
		// then 2 images should be displayed
		final SWTBotTreeItem[] images = imagesTreeItem.getItems();
		assertThat(images).hasSize(2);
		assertThat(images[0].getText()).startsWith("bar:1.0, latest");
		assertThat(images[1].getText()).startsWith("foo:1.0, latest");
	}

	@Test
	public void shouldDisableConnectionWhenUnreachable() throws DockerException, InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		Mockito.when(client.ping()).thenThrow(new DockerException("failed by mock"));
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		final SWTBotTreeItem connectionTreeItem = SWTUtils.expand(dockerExplorerViewBot.bot().tree(), "Test");
		// then
		assertThat(connectionTreeItem.getItems()).isEmpty();
	}

	@Test
	public void shouldRemoveAllConnectionsSimultaneously() throws DockerException, InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection1 = MockDockerConnectionFactory.from("Test1", client)
				.withDefaultTCPConnectionSettings();
		final DockerConnection dockerConnection2 = MockDockerConnectionFactory.from("Test2", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection1, dockerConnection2);
		final List<String> initialConnections = Stream.of(dockerExplorerViewBot.bot().tree().getAllItems())
				.map(item -> item.getText()).collect(Collectors.toList());
		assertThat(initialConnections).contains("Test1", "Test2");
		// when
		SWTUtils.select(dockerExplorerViewBot.bot().tree(), "Test1", "Test2");
		SWTUtils.getContextMenu(dockerExplorerViewBot.bot().tree(), "Remove").click();
		// then
		final List<String> remainingConnections = Stream.of(dockerExplorerViewBot.bot().tree().getAllItems())
				.map(item -> item.getText()).collect(Collectors.toList());
		assertThat(remainingConnections).doesNotContain("Test1", "Test2");
	}

}
