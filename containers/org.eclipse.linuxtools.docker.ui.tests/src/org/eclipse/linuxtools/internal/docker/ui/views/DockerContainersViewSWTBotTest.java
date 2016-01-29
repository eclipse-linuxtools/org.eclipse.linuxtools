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
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TestLoggerRule;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link DockerContainersView}
 */
public class DockerContainersViewSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerContainersViewBot;
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
		SWTUtils.asyncExec(() -> {try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(DockerContainersView.VIEW_ID);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
		}});
		this.dockerContainersViewBot = bot.viewById(DockerContainersView.VIEW_ID);
		this.dockerExplorerBotView = bot.viewById(DockerExplorerView.VIEW_ID);
	}
	
	@Test
	public void shouldRemoveListenersWhenClosingView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// remove the DockerContainerRefreshManager
		dockerConnection.removeContainerListener(DockerContainerRefreshManager
								.getInstance());
		// DockerExplorerView inner classes
		assertThat(dockerConnection.getContainerListeners()).hasSize(2);
		// close the Docker Explorer View
		dockerContainersViewBot.close();
		// there should be one listener left: DockerExplorerView
		assertThat(dockerConnection.getContainerListeners()).hasSize(1);
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
		dockerConnection.removeContainerListener(DockerContainerRefreshManager
								.getInstance());
		// DockerExplorerView inner classes
		assertThat(dockerConnection.getContainerListeners()).hasSize(0);
		// close the Docker Explorer View
		dockerContainersViewBot.close();
		// there should be one listener left: DockerExplorerView
		assertThat(dockerConnection.getContainerListeners()).hasSize(0);
	}

}
