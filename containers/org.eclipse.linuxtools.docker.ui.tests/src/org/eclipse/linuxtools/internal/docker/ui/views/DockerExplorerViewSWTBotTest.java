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

import org.assertj.core.api.Assertions;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
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
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
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
	private SWTBotTree dockerExplorerViewTreeBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(); 
	
	@Rule
	public TestLoggerRule watcher = new TestLoggerRule();
	
	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();
	
	@Before
	public void setup() throws InterruptedException {
		this.bot = new SWTWorkbenchBot();
		SWTUtils.asyncExec(() -> {try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
		}});
		this.dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		this.dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		this.bot.views().stream()
				.filter(v -> v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerContainersView")
						|| v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerImagesView"))
				.forEach(v -> v.close());
	}

	@Test
	public void shouldDisplayExplanationPane() throws InterruptedException {
		// given
		DockerConnectionManagerUtils.configureConnectionManager();
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isEmpty();
	}

	@Test
	public void shouldDisplayConnectionsPane() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isNotEmpty();
	}

	@Test
	public void shouldRefreshImagesAndShowChanges() throws InterruptedException {
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
		dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imagesTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Refresh").click();
		Thread.sleep(2000);
		imagesTreeItem.expand();
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		// then check that there are images now
		Assertions.assertThat(imagesTreeItem.isExpanded()).isTrue();
		Assertions.assertThat(imagesTreeItem.getItems().length).isEqualTo(1);
	}

	@Test
	public void shouldRefreshContainersAndShowChanges() throws InterruptedException {
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
		dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(containersTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Refresh").click();
		SWTUtils.asyncExec(() -> containersTreeItem.expand());

		// then check that there are images now
		Assertions.assertThat(containersTreeItem.isExpanded()).isTrue();
		Assertions.assertThat(containersTreeItem.getItems().length).isEqualTo(1);
	}

	@Test
	public void shouldShowContainerPortMapping() throws InterruptedException {
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
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test (null)",
				"Containers", "foo_bar (null)");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		final SWTBotTreeItem containerPortsTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test (null)",
				"Containers", "foo_bar (null)", "Ports");
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
	public void shouldShowContainerLinks() throws InterruptedException {
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
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test (null)",
				"Containers", "foo_bar (null)");
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
	public void shouldShowContainerVolumes() throws InterruptedException {
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
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test (null)",
				"Containers", "foo_bar (null)");
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
	public void shouldRemainExpandedAfterRefresh() throws InterruptedException {
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
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test (null)",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar (null)");
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
		dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(containersTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Refresh").click();
		SWTUtils.asyncExec(() -> containersTreeItem.expand());
		// then all items should remain expanded (after they were reloaded)
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Links")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Ports")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Volumes")).isExpanded();
		});
	}

}
