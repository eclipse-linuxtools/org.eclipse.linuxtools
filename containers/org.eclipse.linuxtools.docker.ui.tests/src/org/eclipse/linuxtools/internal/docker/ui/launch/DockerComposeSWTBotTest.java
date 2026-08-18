/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.mandas.docker.client.DockerClient;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Testing the {@link DockerCompose} utility class using SWTBot.
 */
public class DockerComposeSWTBotTest {

	@RegisterExtension
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Order(6)
	@RegisterExtension
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Order(1)
	@RegisterExtension
	public ProjectInitializationRule projectInit = new ProjectInitializationRule();

	@Order(5)
	@RegisterExtension
	public ClearLaunchConfigurationsRule clearLaunchConfig = new ClearLaunchConfigurationsRule(
			IDockerComposeLaunchConfigurationConstants.CONFIG_TYPE_ID);

	@Order(4)
	@RegisterExtension
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CLOSE_LABEL);

	@Order(3)
	@RegisterExtension
	public ConsoleViewRule consoleViewRule = new ConsoleViewRule();

	@Order(2)
	@RegisterExtension
	public ProjectExplorerViewRule projectExplorerViewRule = new ProjectExplorerViewRule();

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();

	private CountDownLatch latch;

	@BeforeEach
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
		Mockito.when(
				mockProcessLauncher
						.processBuilder(ArgumentMatchers.anyString(),
								ArgumentMatchers.eq(DockerCompose.getDockerComposeCommandName()),
								CustomMatchers.arrayContains("up"))
						.workingDir(ArgumentMatchers.anyString()).start())
				.thenReturn(mockDockerComposeUpProcess);
		final CountDownLatch processLatch = new CountDownLatch(1);
		this.latch = processLatch;
		Mockito.when(mockDockerComposeUpProcess.waitFor()).then(invocation -> {
			processLatch.await(5, TimeUnit.SECONDS);
			return 0;
		});
		// behave like a running process until the latch is released: the debug
		// framework relies on 'exitValue()' throwing to tell that a process is
		// still alive
		Mockito.when(mockDockerComposeUpProcess.isAlive()).then(invocation -> processLatch.getCount() > 0);
		Mockito.when(mockDockerComposeUpProcess.exitValue()).then(invocation -> {
			if (processLatch.getCount() > 0) {
				throw new IllegalThreadStateException("process has not exited");
			}
			return 0;
		});
	}

	private void setupDockerComposeStopMockProcess(final ProcessLauncher mockProcessLauncher)
			throws DockerException, InterruptedException {
		final Process mockDockerComposeStopProcess = Mockito.mock(Process.class);
		Mockito.when(mockDockerComposeStopProcess.getInputStream())
				.thenReturn(new ByteArrayInputStream("stop\n".getBytes()));
		Mockito.when(mockDockerComposeStopProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
		Mockito.when(mockDockerComposeStopProcess.getOutputStream()).thenReturn(new ByteArrayOutputStream());

		Mockito.when(
				mockProcessLauncher
						.processBuilder(ArgumentMatchers.anyString(),
								ArgumentMatchers.eq(DockerCompose.getDockerComposeCommandName()),
								CustomMatchers.arrayContains("stop"))
						.workingDir(ArgumentMatchers.anyString()).start())
				.thenReturn(mockDockerComposeStopProcess);
		final CountDownLatch processLatch = this.latch;
		Mockito.when(mockDockerComposeStopProcess.waitFor()).then(invocation -> {
			processLatch.countDown();
			return 0;
		});
	}

	/**
	 * @return the {@link SWTBotMenu} for the "Run as > Docker Image Build"
	 *         shortcut
	 */
	private SWTBotMenu getRunAsDockerComposeContextMenu(final String projectName, final String dockerComposeFileName) {
		final SWTBotView projectExplorerBotView = this.projectExplorerViewRule.getProjectExplorerBotView();
		// make sure the project explorer view is the active part: the "Run As"
		// contextual menu and the launch shortcut are computed from the selection
		// of the active part, and a previous launch activates the console view
		// (from a job scheduled with a delay, hence the wait).
		SWTUtils.waitForJobsToComplete();
		projectExplorerBotView.show();
		projectExplorerBotView.setFocus();
		final SWTBotTreeItem fooProjectTreeItem = SWTUtils.getTreeItem(projectExplorerBotView, projectName);
		assertThat(fooProjectTreeItem).isNotNull();
		UIThreadRunnable.syncExec(() -> fooProjectTreeItem.expand());
		final SWTBotTreeItem dockerfileTreeItem = SWTUtils.getTreeItem(fooProjectTreeItem, dockerComposeFileName);
		assertThat(dockerfileTreeItem).isNotNull();
		// select the item itself: SWTUtils.select(item, matchers...) with no matchers
		// filters the children against nothing and ends up selecting nothing at all,
		// which leaves the workbench selection empty and the "Run As" menu absent
		UIThreadRunnable.syncExec(() -> dockerfileTreeItem.select());
		final SWTBotMenu runAsDockerComposeMenu = projectExplorerBotView.bot().tree().contextMenu()
				.menu("Run As", "1 Docker Compose");
		return runAsDockerComposeMenu;
	}

	/**
	 * Waits for the "Stop services" button of the Docker Compose console to be
	 * in the given enablement state and returns it. The button is looked up
	 * again on each poll, since the console page toolbar may be rebuilt.
	 */
	private SWTBotToolbarButton waitForStopButton(final boolean enabled) {
		final String tooltip = ConsoleMessages.getString("DockerComposeStopAction.tooltip"); //$NON-NLS-1$
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				return SWTUtils.getConsoleToolbarButtonWithTooltipText(DockerComposeSWTBotTest.this.bot, tooltip)
						.isEnabled() == enabled;
			}

			@Override
			public String getFailureMessage() {
				return "The '" + tooltip + "' console button was not " + (enabled ? "enabled" : "disabled");
			}
		});
		return SWTUtils.getConsoleToolbarButtonWithTooltipText(bot, tooltip);
	}

	/**
	 * Confirms the "Docker Compose Up" dialog that prompts for the connection to
	 * use. The dialog has to be activated and its "OK" button looked up through
	 * its own bot: lookups on the global bot go to whichever shell is active,
	 * which without a window manager is still the workbench window.
	 */
	private void confirmDockerComposeUpDialog() {
		final String title = WizardMessages.getString("DockerComposeUpDialog.title"); //$NON-NLS-1$
		final SWTBotShell dialog = bot.shell(title);
		bot.waitUntil(Conditions.shellIsActive(title));
		dialog.activate();
		final SWTBotButton okButton = dialog.bot().button(IDialogConstants.OK_LABEL);
		bot.waitUntil(Conditions.widgetIsEnabled(okButton));
		bot.getDisplay().syncExec(() -> okButton.click());
	}

	@Test
	@RunWithProject("foo")
	public void shouldDisableCommandOnFirstCallWhenMissingConnection() {
		// given no connection
		ClearConnectionManagerRule.removeAllConnections(DockerConnectionManager.getInstance());
		// when
		final SWTBotMenu composeMenu = getRunAsDockerComposeContextMenu("foo", "docker-compose.yml");
		bot.getDisplay().asyncExec(() -> composeMenu.click());
		// then expect an error dialog because no Docker connection exists
		final SWTBotShell noConnectionShell = bot
				.shell(LaunchMessages.getString("DockerComposeUpShortcut.no.connections.msg"));
		assertThat(noConnectionShell).isNotNull();
		// closing the dialog
		noConnectionShell.bot().button(IDialogConstants.NO_LABEL).click();
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
		final SWTBotMenu composeMenu = getRunAsDockerComposeContextMenu("foo", "docker-compose.yml");
		bot.getDisplay().asyncExec(() -> composeMenu.click());
		// then confirm the connection
		confirmDockerComposeUpDialog();
		// wait for the job to run
		SWTUtils.waitForJobsToComplete();
		// then expect the console to be displayed (it is revealed by the console
		// manager's own job, so wait for it)
		SWTUtils.waitForConsoleView(this.bot);
		// expect the 'stop' button to be enabled (the 'docker-compose up' command
		// runs on its own thread once the launch job is done)
		final SWTBotToolbarButton consoleToolbarStopButton = waitForStopButton(true);
		assertTrue(consoleToolbarStopButton.isEnabled());
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
		final SWTBotMenu composeMenu = getRunAsDockerComposeContextMenu("foo", "docker-compose.yml");
		bot.getDisplay().asyncExec(() -> composeMenu.click());
		// then there is no dialog to confirm the connection: wait for the job to
		// run and for the console to show up with its 'stop' button enabled
		SWTUtils.waitForJobsToComplete();
		final SWTBotToolbarButton consoleToolbarStopButton = waitForStopButton(true);
		assertTrue(consoleToolbarStopButton.isEnabled());
	}

	@Test
	@RunWithProject("foo")
	public void shouldStopDockerCompose() throws CoreException {
		// given
		shouldStartDockerComposeFromScratch();
		// when
		final SWTBotToolbarButton consoleToolbarStopButton = SWTUtils.getConsoleToolbarButtonWithTooltipText(bot,
				ConsoleMessages.getString("DockerComposeStopAction.tooltip"));
		assertTrue(consoleToolbarStopButton.isEnabled());
		consoleToolbarStopButton.click();
		// then wait for the 'docker-compose stop' job to run
		SWTUtils.waitForJobsToComplete();
		// verify the latch is down
		assertThat(latch.getCount()).isEqualTo(0);
		// verify the stop button is disabled
		assertFalse(waitForStopButton(false).isEnabled());
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
		final SWTBotMenu composeMenu = getRunAsDockerComposeContextMenu("foo", "docker-compose.yml");
		bot.getDisplay().asyncExec(() -> composeMenu.click());
		confirmDockerComposeUpDialog();
		// wait for the job to run
		SWTUtils.waitForJobsToComplete();
		// when stopping
		final SWTBotToolbarButton consoleToolbarStopButton = SWTUtils.getConsoleToolbarButtonWithTooltipText(bot,
				ConsoleMessages.getString("DockerComposeStopAction.tooltip"));
		assertTrue(consoleToolbarStopButton.isEnabled());
		consoleToolbarStopButton.click();
		// wait for the 'docker-compose stop' job to run
		SWTUtils.waitForJobsToComplete();
		// redo the setup to get a new mock process
		setupMockedProcessLauncher();
		// when restarting
		final SWTBotMenu composeMenu2 = getRunAsDockerComposeContextMenu("foo", "docker-compose.yml");
		bot.getDisplay().asyncExec(() -> composeMenu2.click());
		// then the 'docker-compose up' command runs on its own thread once the
		// launch job is done, so wait for the stop button to be enabled again
		assertTrue(waitForStopButton(true).isEnabled());
	}

	@Test
	@RunWithProject("foo")
	@Disabled // ignored for now because the "Run" menu from the toolbar remains
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
		assertTrue(bot.button("Run").isEnabled());

	}
}
