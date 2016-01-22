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

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerExplorerViewAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTBotTreeItemAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TestLoggerRule;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link DockerExplorerView} {@link Viewer}
 */
@RunWith(SWTBotJunit4ClassRunner.class) 
public class DockerExplorerViewSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerExplorerViewBot;
	private DockerExplorerView dockerExplorerView;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(); 
	
	@Rule
	public TestLoggerRule watcher = new TestLoggerRule();
	
	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();
	
	@Before
	public void setup() {
		this.bot = new SWTWorkbenchBot();
		SWTUtils.asyncExec(() -> {try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(DockerExplorerView.VIEW_ID);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
		}});
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
		} catch(WidgetNotFoundException e) {
			// ignore if widget is not found, that's probably because there's no tree in the 
			// Docker Explorer view for the test that just ran.
		}
	}
	
	private void selectConnectionInTreeView(final String connectionName) {
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, connectionName);
		connectionTreeItem.select();
	}

	private void selectContainersInTreeView(final String connectionName, final String... containerNames) {
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// When a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, connectionName, "Containers");
		SWTUtils.asyncExec(() -> containersTreeItem.expand());
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
		// select both containers
		SWTUtils.select(imagesTreeItem, imageNames);
	}

	@Test 
	public void shouldDisplayExplanationPane() {
		// given
		DockerConnectionManagerUtils.configureConnectionManager();
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isEmpty();
	}

	@Test 
	public void shouldDisplayConnectionsPane() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isNotEmpty();
	}

	@Test 
	public void shouldRefreshImagesAndShowChanges() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem[] allItems = dockerExplorerViewBot.bot().tree().getAllItems();
		Assertions.assertThat(allItems).hasSize(1);
		Assertions.assertThat(allItems[0].getItems()).hasSize(2);
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		imagesTreeItem.expand();
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		Assertions.assertThat(imagesTreeItem.getItems().length).isEqualTo(0);
		
		// update the client 
		final DockerClient updatedClient = MockDockerClientFactory.image(MockDockerImageFactory.name("foo/bar").build())
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
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		final SWTBotTreeItem[] allItems = dockerExplorerViewBot.bot().tree().getAllItems();
		// one connection
		Assertions.assertThat(allItems).hasSize(1);
		// "containers" and "images" items
		Assertions.assertThat(allItems[0].getItems()).hasSize(2);
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Containers");
		containersTreeItem.expand();
		Assertions.assertThat(containersTreeItem.getItems().length).isEqualTo(0);
		
		// update the client 
		final DockerClient updatedClient = MockDockerClientFactory.container(MockDockerContainerFactory.name("foo_bar").build()).build();
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
				.container(MockDockerContainerFactory.name("foo_bar").build(), MockDockerContainerInfoFactory
						.port("8080/tcp", "0.0.0.0", "8080").port("8787/tcp", "0.0.0.0", "8787").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers", "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		final SWTBotTreeItem containerPortsTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers", "foo_bar", "Ports");
		SWTUtils.asyncExec(() -> containerPortsTreeItem.expand());
		// then
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(containerPortsTreeItem).isExpanded().hasChildItems(2);
			SWTBotTreeItemAssertions.assertThat(containerPortsTreeItem.getNode(0))
					.hasText("0.0.0.0:8080 -> 8080 (tcp)");
			SWTBotTreeItemAssertions.assertThat(containerPortsTreeItem.getNode(1))
					.hasText("0.0.0.0:8787 -> 8787 (tcp)");
		});
	}

	@Test 
	public void shouldShowContainerLinks() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(), MockDockerContainerInfoFactory
						.link("/postgres-demo:/foo_bar/postgres1").link("/postgres-demo:/foo_bar/postgres2").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers", "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		final SWTBotTreeItem containerLinksTreeItem = SWTUtils.getTreeItem(containerTreeItem, "Links");
		SWTUtils.asyncExec(() -> containerLinksTreeItem.expand());
		// then
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(containerLinksTreeItem).isExpanded().hasChildItems(2);
			SWTBotTreeItemAssertions.assertThat(containerLinksTreeItem.getNode(0)).hasText("postgres-demo (postgres1)");
			SWTBotTreeItemAssertions.assertThat(containerLinksTreeItem.getNode(1)).hasText("postgres-demo (postgres2)");
		});
	}

	@Test 
	public void shouldShowContainerVolumes() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.volume("/path/to/container")
								.volume("/path/to/host:/path/to/container")
								.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers", "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		final SWTBotTreeItem containerVolumesItem = SWTUtils.getTreeItem(containerTreeItem, "Volumes");
		SWTUtils.asyncExec(() -> containerVolumesItem.expand());
		// then
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(containerVolumesItem).isExpanded().hasChildItems(3);
			SWTBotTreeItemAssertions.assertThat(containerVolumesItem.getNode(0)).hasText("/path/to/container");
			SWTBotTreeItemAssertions.assertThat(containerVolumesItem.getNode(1))
					.hasText("/path/to/host -> /path/to/container");
			SWTBotTreeItemAssertions.assertThat(containerVolumesItem.getNode(2))
					.hasText("/path/to/host -> /path/to/container (Z,ro)");
		});
	}

	@Test 
	public void shouldRemainExpandedAfterRefresh() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.volume("/path/to/container")
								.port("8080/tcp", "0.0.0.0", "8080")
								.link("/foo:/bar/foo")
								.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Links").expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Ports").expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Volumes").expand());
		// ensure items are actually expanded before calling the 'refresh' command
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Links")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Ports")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Volumes")).isExpanded();
		});
		// when refreshing the container
		dockerExplorerViewBot.bot().tree().select(containersTreeItem);
		dockerExplorerViewBot.bot().tree().contextMenu("Refresh").click();
		SWTUtils.asyncExec(() -> containersTreeItem.expand());
		// then all items should remain expanded (after they were reloaded)
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Links")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Ports")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Volumes")).isExpanded();
		});
	}

	@Test 
	public void shouldProvideEnabledStartCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Start");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test 
	public void shouldProvideDisabledStartCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Start");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test 
	public void shouldProvideEnabledStopCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Stop");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test 
	public void shouldProvideDisabledStopCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Stop");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test 
	public void shouldProvideEnabledPauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Pause");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test 
	public void shouldProvideDisabledPauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Pause");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test 
	public void shouldProvideEnabledUnpauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Up (Paused)").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Unpause");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test 
	public void shouldProvideDisabledUnpauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Unpause");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test 
	public void shouldProvideEnabledKillCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Kill");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test 
	public void shouldProvideDisabledKillCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Kill");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test 
	public void shouldProvideEnabledRemoveCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Remove");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test 
	public void shouldProvideRemoveCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectContainersInTreeView("Test", "gentle_foo", "angry_bar");
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Remove");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test
	public void shouldShowSelectedConnectionInPropertiesView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one the container
		selectConnectionInTreeView("Test");
		// show container info in Properties view
		SWTUtils.getContextMenu(dockerExplorerViewBot.bot().tree(), "Show In", "Properties").click();
		// the properties view should be visible
		assertThat(this.bot.viewById("org.eclipse.ui.views.PropertySheet").isActive()).isEqualTo(true);
	}

	@Test
	public void shouldShowSelectedContainerInPropertiesView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one the container
		selectContainersInTreeView("Test", "angry_bar");
		// show container info in Properties view
		SWTUtils.getContextMenu(dockerExplorerViewBot.bot().tree(), "Show In", "Properties").click();
		// the properties view should be visible
		assertThat(this.bot.viewById("org.eclipse.ui.views.PropertySheet").isActive()).isEqualTo(true);
	}

	@Test
	public void shouldShowSelectedImageInPropertiesView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.image(MockDockerImageFactory.name("angry_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one the container
		selectImagesInTreeView("Test", "angry_bar");
		// show container info in Properties view
		SWTUtils.getContextMenu(dockerExplorerViewBot.bot().tree(), "Show In", "Properties").click();
		// the properties view should be visible
		assertThat(this.bot.viewById("org.eclipse.ui.views.PropertySheet").isActive()).isEqualTo(true);
	}
	
	@Test
	public void shouldRemoveListenersWhenClosingView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.image(MockDockerImageFactory.name("angry_bar").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// remove the DockerContainerRefreshManager
		dockerConnection.removeContainerListener(DockerContainerRefreshManager
								.getInstance());
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
		final DockerClient client = MockDockerClientFactory.image(MockDockerImageFactory.id("1a2b3c4d5e6f7g")
				.name("foo:1.0", "foo:latest", "bar:1.0", "bar:latest").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test (null)",
				"Images");
		// when
		SWTUtils.asyncExec(() -> imagesTreeItem.expand());
		// then 2 images should be displayed
		SWTUtils.syncAssert(() -> {
			final SWTBotTreeItem[] images = imagesTreeItem.getItems();
			assertThat(images).hasSize(2);
			assertThat(images[0].getText()).startsWith("bar: 1.0, latest");
			assertThat(images[1].getText()).startsWith("foo: 1.0, latest");
		});
	}

}
