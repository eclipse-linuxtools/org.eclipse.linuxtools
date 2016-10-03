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

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.internal.docker.core.DockerCompose;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.ProcessLauncher;
import org.eclipse.linuxtools.internal.docker.ui.consoles.ConsoleMessages;
import org.eclipse.linuxtools.internal.docker.ui.testutils.CustomMatchers;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.ProjectInitializationRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.RunWithProject;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ButtonAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearLaunchConfigurationsRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseShellRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ComboAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ConsoleViewRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ProjectExplorerViewRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TextAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ToolbarButtonAssertions;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link DockerCompose} utility class using SWTBot.
 */
public class DockerComposeSWTBotTest {

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule
	public ProjectInitializationRule projectInit = new ProjectInitializationRule();

	@Rule
	public ClearLaunchConfigurationsRule clearLaunchConfig = new ClearLaunchConfigurationsRule(
			IDockerComposeLaunchConfigurationConstants.CONFIG_TYPE_ID);

	@Rule
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CLOSE_LABEL);

	@Rule
	public ConsoleViewRule consoleViewRule = new ConsoleViewRule();

	@Rule
	public ProjectExplorerViewRule projectExplorerViewRule = new ProjectExplorerViewRule();

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();

	private CountDownLatch latch;

	@Before
	public void setupMockedProcessLauncher() throws DockerException, InterruptedException {
		// configure the 'docker-compose up' mocks with a CountDownLatch to
		// simulate a long-running process
		final ProcessLauncher mockProcessLauncher = Mockito.mock(ProcessLauncher.class, Mockito.RETURNS_DEEP_STUBS);
		DockerCompose.getInstance().setProcessLauncher(mockProcessLauncher);
		setupDockerComposeUpMockProcess(mockProcessLauncher);
		// configure the 'docker-compose stop' mocks which release the
		// CountDownLatch to halt the long-running process
		setupDockerComposeStopMockProcess(mockProcessLauncher);

	}

	private void setupDockerComposeUpMockProcess(final ProcessLauncher mockProcessLauncher)
			throws DockerException, InterruptedException {
		final Process mockDockerComposeUpProcess = Mockito.mock(Process.class);
		Mockito.when(mockDockerComposeUpProcess.getInputStream())
				.thenReturn(new ByteArrayInputStream("up!\n".getBytes()));
		Mockito.when(mockDockerComposeUpProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
		Mockito.when(mockDockerComposeUpProcess.getOutputStream()).thenReturn(new ByteArrayOutputStream());
		Mockito.when(mockProcessLauncher.processBuilder(Matchers.anyString(),
				Matchers.eq(DockerCompose.getDockerComposeCommandName()), CustomMatchers.arrayContains("up"))
				.workingDir(Matchers.anyString()).start()).thenReturn(mockDockerComposeUpProcess);
		latch = new CountDownLatch(1);
		Mockito.when(mockDockerComposeUpProcess.waitFor()).then(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				latch.await(5, TimeUnit.SECONDS);
				return 0;
			}
		});
	}

	private void setupDockerComposeStopMockProcess(final ProcessLauncher mockProcessLauncher)
			throws DockerException, InterruptedException {
		final Process mockDockerComposeStopProcess = Mockito.mock(Process.class);
		Mockito.when(mockDockerComposeStopProcess.getInputStream())
				.thenReturn(new ByteArrayInputStream("stop\n".getBytes()));
		Mockito.when(mockDockerComposeStopProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
		Mockito.when(mockDockerComposeStopProcess.getOutputStream()).thenReturn(new ByteArrayOutputStream());

		Mockito.when(mockProcessLauncher.processBuilder(Matchers.anyString(),
				Matchers.eq(DockerCompose.getDockerComposeCommandName()), CustomMatchers.arrayContains("stop"))
				.workingDir(Matchers.anyString()).start()).thenReturn(mockDockerComposeStopProcess);
		Mockito.when(mockDockerComposeStopProcess.waitFor()).then(invocation -> {
			latch.countDown();
			return 0;
		});
	}

	/**
	 * @return the {@link SWTBotMenu} for the "Run as > Docker Image Build"
	 *         shortcut
	 */
	private SWTBotMenu getRunAsDockerComposeContextMenu(final String projectName, final String dockerComposeFileName) {
		final SWTBotView projectExplorerBotView = this.projectExplorerViewRule.getProjectExplorerBotView();
		// make sure the project explorer view is visible, in case it was hidden
		// by another view.
		projectExplorerBotView.setFocus();
		final SWTBotTreeItem fooProjectTreeItem = SWTUtils.getTreeItem(projectExplorerBotView, projectName);
		assertThat(fooProjectTreeItem).isNotNull();
		SWTUtils.syncExec(() -> fooProjectTreeItem.expand());
		final SWTBotTreeItem dockerfileTreeItem = SWTUtils.getTreeItem(fooProjectTreeItem, dockerComposeFileName);
		assertThat(dockerfileTreeItem).isNotNull();
		SWTUtils.select(dockerfileTreeItem);
		final SWTBotMenu runAsDockerComposeMenu = SWTUtils.getContextMenu(
				projectExplorerBotView.bot().tree(),
				"Run As", "1 Docker Compose");
		return runAsDockerComposeMenu;
	}

	@Test
	@RunWithProject("foo")
	public void shouldDisableCommandOnFirstCallWhenMissingConnection() {
		// given no connection
		ClearConnectionManagerRule.removeAllConnections(DockerConnectionManager.getInstance());
		// when
		SWTUtils.asyncExec(() -> getRunAsDockerComposeContextMenu("foo", "docker-compose.yml").click());
		// then expect an error dialog because no Docker connection exists
		assertThat(bot.shell(LaunchMessages.getString("DockerComposeUpShortcut.no.connections.msg"))).isNotNull();
		// closing the wizard
		SWTUtils.syncExec(() -> {
			bot.button("No").click();
		});
	}

	@Test
	@RunWithProject("foo")
	public void shouldStartDockerComposeFromScratch() throws CoreException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		SWTUtils.asyncExec(() -> getRunAsDockerComposeContextMenu("foo", "docker-compose.yml").click());
		// then confirm the connection
		bot.button("OK").click();
		// wait for the job to run
		SWTUtils.waitForJobsToComplete();
		// then expect the console to be displayed
		assertThat(SWTUtils.isConsoleViewVisible(this.bot)).isTrue();
		// expect the 'stop' button to be enabled
		final SWTBotToolbarButton consoleToolbarStopButton = SWTUtils.getConsoleToolbarButtonWithTooltipText(bot,
				ConsoleMessages.getString("DockerComposeStopAction.tooltip"));
		ToolbarButtonAssertions.assertThat(consoleToolbarStopButton).isEnabled();
		// verify that the launch configuration was saved
		final ILaunchConfiguration launchConfiguration = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IDockerComposeLaunchConfigurationConstants.CONFIG_TYPE_ID, "Docker Compose [foo]");
		assertThat(launchConfiguration).isNotNull();
		// verify the latch
		assertThat(latch.getCount()).isEqualTo(1);
	}

	@Test
	@RunWithProject("foo")
	public void shouldStartDockerComposeWithExistingLaunchConfiguration() throws CoreException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final IFile dockerComposeScript = projectInit.getProject().getFile("docker-compose.yml");
		LaunchConfigurationUtils.createDockerComposeUpLaunchConfiguration(dockerConnection, dockerComposeScript);
		// when
		SWTUtils.asyncExec(() -> getRunAsDockerComposeContextMenu("foo", "docker-compose.yml").click());
		// then confirm the connection
		final SWTBotToolbarButton consoleToolbarStopButton = SWTUtils.getConsoleToolbarButtonWithTooltipText(bot,
				ConsoleMessages.getString("DockerComposeStopAction.tooltip"));
		ToolbarButtonAssertions.assertThat(consoleToolbarStopButton).isEnabled();
	}

	@Test
	@RunWithProject("foo")
	public void shouldStopDockerCompose() throws CoreException {
		// given
		shouldStartDockerComposeFromScratch();
		// when
		final SWTBotToolbarButton consoleToolbarStopButton = SWTUtils.getConsoleToolbarButtonWithTooltipText(bot,
				ConsoleMessages.getString("DockerComposeStopAction.tooltip"));
		ToolbarButtonAssertions.assertThat(consoleToolbarStopButton).isEnabled();
		consoleToolbarStopButton.click();
		// then
		// verify the latch is down
		assertThat(latch.getCount()).isEqualTo(0);
		// verify the stop button is disabled
		ToolbarButtonAssertions.assertThat(consoleToolbarStopButton).isNotEnabled();
	}

	@Test
	@RunWithProject("foo")
	public void shouldRestartDockerCompose() throws InterruptedException, DockerException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when starting without launch config
		SWTUtils.asyncExec(() -> getRunAsDockerComposeContextMenu("foo", "docker-compose.yml").click());
		bot.button("OK").click();
		// wait for the job to run
		SWTUtils.waitForJobsToComplete();
		// when stopping
		final SWTBotToolbarButton consoleToolbarStopButton = SWTUtils.getConsoleToolbarButtonWithTooltipText(bot,
				ConsoleMessages.getString("DockerComposeStopAction.tooltip"));
		ToolbarButtonAssertions.assertThat(consoleToolbarStopButton).isEnabled();
		consoleToolbarStopButton.click();
		// redo the setup to get a new mock process
		setupMockedProcessLauncher();
		// when restarting
		SWTUtils.asyncExec(() -> getRunAsDockerComposeContextMenu("foo", "docker-compose.yml").click());
		// wait for the job to run
		SWTUtils.waitForJobsToComplete();
		// then
		ToolbarButtonAssertions.assertThat(consoleToolbarStopButton).isEnabled();
	}

	@Test
	@RunWithProject("foo")
	@Ignore // ignored for now because the "Run" menu from the toolbar remains
			// visible (on macOS) and this
	// has side-effects on the other tests that fail because the widgets are not
	// found.
	public void shouldValidateLaunchConfiguration() throws CoreException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final IFile dockerComposeScript = projectInit.getProject().getFile("docker-compose.yml");
		LaunchConfigurationUtils.createDockerComposeUpLaunchConfiguration(dockerConnection, dockerComposeScript);
		// when
		bot.toolbarDropDownButtonWithTooltip("Run").menuItem("Run Configurations...").click();
		final SWTBotTreeItem dockerComposeTreeItem = SWTUtils.expand(bot.tree(), "Docker Compose");
		SWTUtils.select(dockerComposeTreeItem, "Docker Compose [foo]");
		// verify that the config is set and the form can be closed with the
		// "OK" button
		ComboAssertions.assertThat(bot.comboBox(0)).isEnabled().itemSelected("Test");
		TextAssertions.assertThat(bot.text(2)).isEnabled().textEquals("/foo");
		ButtonAssertions.assertThat(bot.button("Run")).isEnabled();

	}
}
