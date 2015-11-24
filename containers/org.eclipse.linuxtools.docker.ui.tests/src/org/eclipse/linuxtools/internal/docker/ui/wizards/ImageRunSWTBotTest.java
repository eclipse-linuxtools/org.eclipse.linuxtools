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

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWizardRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link ImageRun} wizard
 */
public class ImageRunSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private DockerExplorerView dockerExplorerView;
	private SWTBotView dockerExplorerViewBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule();

	@Rule 
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();
	
	@Rule 
	public CloseWizardRule closeWizard = new CloseWizardRule();
	
	@Before
	public void lookupDockerExplorerView() {
		dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		dockerExplorerViewBot.show();
		dockerExplorerViewBot.setFocus();
	}
	
	@Test
	public void shouldReportErrorIfContainerWithSameNameExists() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockDockerImageFactory.name("foo:latest").build())
				.container(MockDockerContainerFactory.name("foo_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerExplorerView, dockerConnection);
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		final SWTBotTreeItem imageTreeItem = SWTUtils.getTreeItem(imagesTreeItem, "foo");
		
		// when opening the "Run Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imageTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Run Image...").click();

		// when use an existing container name
		bot.textWithLabel(WizardMessages.getString("ImageRunSelectionPage.containerName")).setText("foo_bar");
		// then
		// wait for https://bugs.eclipse.org/bugs/show_bug.cgi?id=482889 to be able to check the wiazard validation message
		//assertThat(bot.text(WizardMessages.getString("ImageRunSelectionPage.containerWithSameName"))).isNotNull();
		assertThat(bot.button("Finish").isEnabled()).isEqualTo(false);
	}

	@Test
	public void shouldNotReportErrorIfNoContainerWithSameNameExists() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockDockerImageFactory.name("foo:latest").build())
				.container(MockDockerContainerFactory.name("foo_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerExplorerView, dockerConnection);
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		final SWTBotTreeItem imageTreeItem = SWTUtils.getTreeItem(imagesTreeItem, "foo");
		
		// when opening the "Run Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imageTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Run Image...").click();
		
		// when use an existing container name
		bot.textWithLabel(WizardMessages.getString("ImageRunSelectionPage.containerName")).setText("foo_bar_baz");
		// then
		assertThat(bot.button("Finish").isEnabled()).isEqualTo(true);
	}

}
