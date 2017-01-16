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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.docker.ui.launch.LaunchConfigurationUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerConfigFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerHostConfigFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ButtonAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseShellRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
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

	private String configureRunImageLaunchConfiguration(final IDockerConnection connection, final String networkMode) {
		final IDockerImage image = MockDockerImageFactory.name("images").connection(connection).build();
		final DockerContainerConfig containerConfig = MockDockerContainerConfigFactory.cmd("cmd").build();
		final IDockerHostConfig hostConfig = MockDockerHostConfigFactory.publishAllPorts(true).networkMode(networkMode)
				.build();
		final ILaunchConfiguration runImageLaunchConfiguration = LaunchConfigurationUtils
				.createRunImageLaunchConfiguration(image, containerConfig, hostConfig, "some_container", false);
		return runImageLaunchConfiguration.getName();
	}

	private String configureRunImageLaunchConfiguration(final IDockerConnection connection) {
		final IDockerImage image = MockDockerImageFactory.name("images").connection(connection).build();
		final DockerContainerConfig containerConfig = MockDockerContainerConfigFactory.cmd("cmd").build();
		final IDockerHostConfig hostConfig = MockDockerHostConfigFactory.publishAllPorts(true).build();
		final ILaunchConfiguration runImageLaunchConfiguration = LaunchConfigurationUtils
				.createRunImageLaunchConfiguration(image, containerConfig, hostConfig, "some_container", false);
		return runImageLaunchConfiguration.getName();
	}

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);

	@Before
	public void lookupDockerExplorerView() {
		dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		dockerExplorerViewBot.show();
		dockerExplorerViewBot.setFocus();
	}

	@Test
	public void shouldReportErrorIfContainerWithSameNameExists() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo:latest").build())
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// expand the 'Images' node and select the 'foo' images
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo").select();

		// when opening the "Run Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.contextMenu("Run...").click();

		// when use an existing container name
		bot.textWithLabel(WizardMessages.getString("ImageRunSelectionPage.containerName")).setText("foo_bar");
		// then
		// wait for https://bugs.eclipse.org/bugs/show_bug.cgi?id=482889 to be
		// able to check the wizard validation message
		//assertThat(bot.text(WizardMessages.getString("ImageRunSelectionPage.containerWithSameName"))).isNotNull();
		ButtonAssertions.assertThat(bot.button("Finish")).isNotEnabled();
	}

	@Test
	public void testNetworkModeBridge() throws CoreException {
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo:latest").build())
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection connection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		final String runImageLaunchConfigurationName = configureRunImageLaunchConfiguration(connection, "bridge");
		final ILaunchConfiguration runDockerImageLaunchConfig = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, runImageLaunchConfigurationName);
		assertThat(runDockerImageLaunchConfig).isNotNull();
		assertThat(runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				"")).isEqualTo("Test");
		assertThat(
				runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE, ""))
						.isEqualTo("bridge");
	}

	@Test
	public void testNetworkModeHost() throws CoreException {
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo:latest").build())
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection connection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		final String runImageLaunchConfigurationName = configureRunImageLaunchConfiguration(connection, "host");
		final ILaunchConfiguration runDockerImageLaunchConfig = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, runImageLaunchConfigurationName);
		assertThat(runDockerImageLaunchConfig).isNotNull();
		assertThat(runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				"")).isEqualTo("Test");
		assertThat(
				runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE, ""))
						.isEqualTo("host");
	}

	@Test
	public void testNetworkModeDefault() throws CoreException {
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo:latest").build())
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection connection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		final String runImageLaunchConfigurationName = configureRunImageLaunchConfiguration(connection, "");
		final ILaunchConfiguration runDockerImageLaunchConfig = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, runImageLaunchConfigurationName);
		assertThat(runDockerImageLaunchConfig).isNotNull();
		assertThat(runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				"")).isEqualTo("Test");
		assertThat(
				runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE, ""))
						.isEqualTo("");
	}

	@Test
	public void testNetworkModeContainer() throws CoreException {
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo:latest").build())
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection connection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		final String runImageLaunchConfigurationName = configureRunImageLaunchConfiguration(connection,
				"container:angry_bird");
		final ILaunchConfiguration runDockerImageLaunchConfig = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, runImageLaunchConfigurationName);
		assertThat(runDockerImageLaunchConfig).isNotNull();
		assertThat(runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				"")).isEqualTo("Test");
		assertThat(
				runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE, ""))
						.isEqualTo("container:angry_bird");
	}

	@Test
	public void testNetworkModeOther() throws CoreException {
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo:latest").build())
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection connection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		final String runImageLaunchConfigurationName = configureRunImageLaunchConfiguration(connection, "jeffnet");
		final ILaunchConfiguration runDockerImageLaunchConfig = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, runImageLaunchConfigurationName);
		assertThat(runDockerImageLaunchConfig).isNotNull();
		assertThat(runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				"")).isEqualTo("Test");
		assertThat(
				runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE, ""))
						.isEqualTo("jeffnet");
	}

	@Test
	public void shouldNotReportErrorIfNoContainerWithSameNameExists() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("foo:latest").build())
				.container(MockContainerFactory.name("foo_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// expand the 'Images' node and select the 'foo' images
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo").select();
		// when opening the "Run Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.contextMenu("Run...").click();

		// when use an existing container name
		bot.textWithLabel(WizardMessages.getString("ImageRunSelectionPage.containerName")).setText("foo_bar_baz");
		// then
		assertThat(bot.button("Finish").isEnabled()).isEqualTo(true);
	}

	@Test
	public void shouldCreateLaunchConfigurationWhenRunningNamedContainer()
			throws InterruptedException, DockerException, CoreException {
		// images to use
		final String imageName = "foo/bar:latest";
		final Image image = MockImageFactory.id("1a2b3c4d5e6f7g").name(imageName).build();
		final ImageInfo imageInfo = MockImageInfoFactory.volume("/foo/bar")
				.command(Arrays.asList("the", "command")).entrypoint(Arrays.asList("the", "entrypoint")).build();
		// container to be created
		final String containerName = "foo_bar";
		final Container createdContainer = MockContainerFactory.id("MockContainer").name(containerName)
				.imageName("1a2b3c4d5e6f7g").status("Started 1 second ago").build();
		final ContainerInfo containerInfo = MockContainerInfoFactory.build();
		final DockerClient client = MockDockerClientFactory.image(image, imageInfo)
				//.container(createdContainer, containerInfo)
				.build();
		// expected response when creating the container
		final ContainerCreation containerCreation = Mockito.mock(ContainerCreation.class);
		Mockito.when(containerCreation.id()).thenReturn("MockContainer");
		Mockito.when(client.createContainer(Matchers.any(), Matchers.any())).thenReturn(containerCreation);
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		// configure the Connection Manager
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when select images and click on run to open the wizard
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo/bar").select();
		dockerExplorerViewBot.bot().tree().contextMenu("Run...").click();
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
		final ILaunchConfiguration launchConfiguration = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, "foo_bar_latest");
		assertThat(launchConfiguration).isNotNull();

	}
}
