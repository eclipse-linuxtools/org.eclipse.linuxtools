/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
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

import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TestLoggerRule;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link DockerImagesView} using {@link SWTBot}.
 */
public class DockerImagesViewSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerImagesBotView;
	private DockerImagesView dockerImagesView;
	private SWTBotView dockerExplorerBotView;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule();

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
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DockerImagesView.VIEW_ID);
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Failed to open Docker Images view: " + e.getMessage());
			}
		});
		this.dockerImagesBotView = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerImagesView");
		this.dockerImagesView = (DockerImagesView) (dockerImagesBotView.getViewReference().getView(true));
		this.dockerExplorerBotView = bot.viewById(DockerExplorerView.VIEW_ID);
	}

	@Test
	public void shouldShowAllImageVariants() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockDockerImageFactory.id("1a2b3c4d5e6f7g")
				.name("foo:1.0", "foo:latest", "bar:1.0", "bar:latest").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// then 1 image with all repo/tags should be displayed
		SWTUtils.syncAssert(() -> {
			final TableItem[] images = dockerImagesView.getViewer().getTable().getItems();
			assertThat(images).hasSize(1);
			assertThat(images[0].getText(1)).isEqualTo("bar:1.0\nbar:latest\nfoo:1.0\nfoo:latest");
		});
	}

	@Test
	public void shouldRemoveListenersWhenClosingView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// remove the DockerContainerRefreshManager
		dockerConnection.removeContainerListener(DockerContainerRefreshManager.getInstance());
		// DockerExplorerView inner classes
		assertThat(dockerConnection.getImageListeners()).hasSize(2);
		// close the Docker Images View
		dockerImagesBotView.close();
		// there should be one listener left: DockerExplorerView
		assertThat(dockerConnection.getImageListeners()).hasSize(1);
	}

	@Test
	public void shouldNotRemoveListenersWhenNoSelectedConnectionBeforeClosingView() {
		// given
		dockerExplorerBotView.close();
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// remove the DockerContainerRefreshManager
		dockerConnection.removeContainerListener(DockerContainerRefreshManager.getInstance());
		assertThat(dockerConnection.getImageListeners()).hasSize(0);
		// close the Docker Images View
		dockerImagesBotView.close();
		// there should be one listener left: DockerExplorerView
		assertThat(dockerConnection.getImageListeners()).hasSize(0);
	}
}
