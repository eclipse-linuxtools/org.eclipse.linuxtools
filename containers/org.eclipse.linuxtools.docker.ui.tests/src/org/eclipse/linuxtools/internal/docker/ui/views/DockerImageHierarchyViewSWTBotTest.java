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

import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHiearchyNode;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TestLoggerRule;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;

/**
 * Testing the {@link DockerImageHierarchyView} call from the {@link DockerExplorerView}.
 */
public class DockerImageHierarchyViewSWTBotTest {

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
		// make sure that the Docker Image Hierarchy view is closed
		this.bot.views().stream().filter(v -> v.getReference().getId().equals(DockerImageHierarchyView.VIEW_ID))
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

	@Test
	public void shouldDisplayImageHierarchyView() {
		// given
		final Image rootImage = MockImageFactory.id("sha256:root").name("root").build();
		final Image fooImage = MockImageFactory.id("sha256:foo").name("foo").parentId("sha256:root").build();
		final Image barImage = MockImageFactory.id("sha256:bar").name("bar").parentId("sha256:foo").build();
		final DockerClient client = MockDockerClientFactory
				.image(rootImage).image(fooImage).image(barImage).build();
		final DockerConnection connection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(connection);
		connection.getImages(true);
		final IDockerImage image = connection.getImages().stream().filter(i -> i.id().equals("sha256:foo")).findFirst().get();
		// when
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo").select();
		dockerExplorerViewBot.bot().tree().contextMenu("Open Image Hierarchy").click(); //$NON-NLS-1$
		// then
		final SWTBotView dockerImageHierarchyView = bot.viewById(DockerImageHierarchyView.VIEW_ID);

	}

	@Test
	public void shouldRetrieveImageHierarchy() {
		// given
		final Image rootImage = MockImageFactory.id("sha256:root").name("root").build();
		final Image fooImage = MockImageFactory.id("sha256:foo").name("foo").parentId("sha256:root").build();
		final Image barImage = MockImageFactory.id("sha256:bar").name("bar").parentId("sha256:foo").build();
		final DockerClient client = MockDockerClientFactory
				.image(rootImage).image(fooImage).image(barImage).build();
		final DockerConnection connection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		connection.getImages(true);
		final IDockerImage image = connection.getImages().stream().filter(i -> i.id().equals("sha256:foo")).findFirst().get();
		// when
		final IDockerImageHiearchyNode imageHierarchy = connection.resolveImageHierarchy(image);
		// then
		assertThat(imageHierarchy).isNotNull();
		assertThat(imageHierarchy.getImage().id()).isEqualTo("sha256:foo");
		assertThat(imageHierarchy.getParent().getImage().id()).isEqualTo("sha256:root");
		assertThat(imageHierarchy.getParent().getChildren()).containsExactly(imageHierarchy);
	}

}
