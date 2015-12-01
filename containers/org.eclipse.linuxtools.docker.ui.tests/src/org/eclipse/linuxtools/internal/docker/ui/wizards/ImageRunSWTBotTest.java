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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.docker.ui.launch.LaunchConfigurationUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWizardRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageInfo;

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
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
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
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
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

	@Test
	public void shouldCreateLaunchConfigurationWhenRunningNamedContainer()
			throws InterruptedException, DockerException, CoreException {
		// image to use
		final String imageName = "foo/bar:latest";
		final Image image = MockDockerImageFactory.id("1a2b3c4d5e6f7g").name(imageName).build();
		final ImageInfo imageInfo = MockDockerImageInfoFactory.volume("/foo/bar")
				.command(Arrays.asList("the", "command")).entrypoint(Arrays.asList("the", "entrypoint")).build();
		// container to be created
		final String containerName = "foo_bar";
		final Container createdContainer = MockDockerContainerFactory.id("MockContainer").name(containerName)
				.imageId("1a2b3c4d5e6f7g").status("Started 1 second ago").build();
		final ContainerInfo containerInfo = MockDockerContainerInfoFactory.build();
		final DockerClient client = MockDockerClientFactory.image(image, imageInfo)
				//.container(createdContainer, containerInfo)
				.build();
		// expected response when creating the container
		final ContainerCreation containerCreation = Mockito.mock(ContainerCreation.class);
		Mockito.when(containerCreation.id()).thenReturn("MockContainer");
		Mockito.when(client.createContainer(Matchers.any(), Matchers.any())).thenReturn(containerCreation);
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		// configure the Connection Manager
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().getTree().getItems());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images").expand();
		// when select image and click on run to open the wizard
		SWTUtils.getTreeItem(imagesTreeItem, "foo/bar").select();
		dockerExplorerViewBot.bot().tree().contextMenu("Run Image...").click();
		bot.waitUntil(Conditions.shellIsActive("Run a Docker Image"), TimeUnit.SECONDS.toMillis(1)); //$NON-NLS-1$
		// configure container
		bot.text(0).setText(containerName);
		// bot.button("Next >").click();
		// update the client to make sure the container exists once the call to "Finish" is done
		MockDockerClientFactory.addContainer(client, createdContainer, containerInfo);
		bot.button("Finish").click();
		// wait for background job to complete
		SWTUtils.waitForJobsToComplete();

		// then
		// check that the client was called
		Mockito.verify(client).createContainer(Matchers.any(), Matchers.eq(containerName));
		// check that a launch configuration was created
		final ILaunchConfigurationType launchConfigType = LaunchConfigurationUtils
				.getLaunchConfigType(IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);
		final ILaunchConfiguration launchConfiguration = LaunchConfigurationUtils
				.getLaunchConfigurationByName(launchConfigType, containerName);
		assertThat(launchConfiguration).isNotNull();

	}
}
