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

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerExplorerViewAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
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
	public TestWatcher watcher = new TestWatcher() {
		@Override
		protected void starting(final Description description) {
			System.out.println("Starting " + description.getClassName() + "." + description.getMethodName());
		}
	};
	
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

	@After
	public void clearConnectionManager() {
		SWTUtils.syncExec(() -> { Stream.of(DockerConnectionManager.getInstance().getConnections())
				.forEach(c -> DockerConnectionManager.getInstance().removeConnection(c));
				dockerExplorerView.getCommonViewer().refresh(true); 
				dockerExplorerView.showConnectionsOrExplanations();});
	}
	
	@AfterClass
	public static void restoreDefaultConfig() {
		DockerConnectionManager.getInstance().setConnectionStorageManager(new DefaultDockerConnectionStorageManager());
	}

	private void configureConnectionManager(final IDockerConnection... connections) throws InterruptedException {
		DockerConnectionManager.getInstance()
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.providing(connections));
		SWTUtils.syncExec(() -> {
			DockerConnectionManager.getInstance().reloadConnections();
			dockerExplorerView.getCommonViewer().refresh();
			dockerExplorerView.showConnectionsOrExplanations();
		});
		Thread.sleep(TimeUnit.SECONDS.toMillis(1));
	}

	@Test
	public void shouldDisplayExplanationPane() throws InterruptedException {
		// given
		configureConnectionManager();
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isEmpty();
	}

	@Test
	public void shouldDisplayConnectionsPane() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.noImages().noContainers();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		configureConnectionManager(dockerConnection);
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isNotEmpty();
	}

	@Test
	public void shouldRefreshImagesAndShowChanges() throws Exception {
		// given
		final DockerClient client = MockDockerClientFactory.noImages().noContainers();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem[] allItems = dockerExplorerViewBot.bot().tree().getAllItems();
		Assertions.assertThat(allItems).hasSize(1);
		Assertions.assertThat(allItems[0].getItems()).hasSize(2);
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		imagesTreeItem.expand();
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		Assertions.assertThat(imagesTreeItem.getItems().length).isEqualTo(0);
		
		// update the client 
		final DockerClient updatedClient = MockDockerClientFactory.images(MockDockerImageFactory.name("foo/bar").build()).noContainers();
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
	public void shouldRefreshContainersAndShowChanges() throws Exception {
		// given
		final DockerClient client = MockDockerClientFactory.noImages().noContainers();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		configureConnectionManager(dockerConnection);
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
		final DockerClient updatedClient = MockDockerClientFactory.noImages().containers(MockDockerContainerFactory.name("foo_bar").build());
		dockerConnection.setClient(updatedClient);
		dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(containersTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Refresh").click();
		SWTUtils.asyncExec(() -> containersTreeItem.expand());

		// then check that there are images now
		Assertions.assertThat(containersTreeItem.isExpanded()).isTrue();
		Assertions.assertThat(containersTreeItem.getItems().length).isEqualTo(1);
	}

}
