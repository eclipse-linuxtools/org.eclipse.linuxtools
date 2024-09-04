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

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.jobs.JobMessages;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.ProjectInitializationRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.RunWithProject;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearLaunchConfigurationsRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ProjectExplorerViewRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.ProgressHandler;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Testing the {@link BuildDockerImageShortcut}
 */
public class BuildDockerImageShortcutSWTBotTest {

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			"org.eclipse.linuxtools.docker.ui.perspective");

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule
	public ProjectInitializationRule projectInit = new ProjectInitializationRule();

	@Rule
	public ClearLaunchConfigurationsRule clearLaunchConfig = new ClearLaunchConfigurationsRule(
			IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);

	@Rule
	public ProjectExplorerViewRule projectExplorerViewRule = new ProjectExplorerViewRule();

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();

	/**
	 * @return the {@link SWTBotMenu} for the "Run as > Docker Image Build"
	 *         shortcut
	 */
	private SWTBotMenu getRunAsdockerImageBuildContextMenu(final String projectName, final String dockerFileName) {
		final SWTBotTreeItem fooProjectTreeItem = SWTUtils
				.getTreeItem(this.projectExplorerViewRule.getProjectExplorerBotView(), projectName);
		assertThat(fooProjectTreeItem).isNotNull();
		SWTUtils.syncExec(() -> fooProjectTreeItem.expand());
		final SWTBotTreeItem dockerfileTreeItem = SWTUtils.getTreeItem(fooProjectTreeItem, dockerFileName);
		assertThat(dockerfileTreeItem).isNotNull();
		SWTUtils.select(dockerfileTreeItem);
		final SWTBotMenu runAsDockerImageBuildMenu = SWTUtils.getContextMenu(
				this.projectExplorerViewRule.getProjectExplorerBotView().bot().tree(),
				"Run As", "1 Docker Image Build");
		return runAsDockerImageBuildMenu;
	}

	@Test
	@RunWithProject("foo")
	public void shouldDisableCommandOnFirstCallWhenMissingConnection() {
		// given no connection
		ClearConnectionManagerRule.removeAllConnections(DockerConnectionManager.getInstance());
		// when
		bot.getDisplay().asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect an error dialog because no Docker connection exists
		assertThat(bot.shell(LaunchMessages.getString("BuildDockerImageShortcut.no.connections.msg")))
				.isNotNull();
		// closing the wizard
		bot.getDisplay().syncExec(() -> {
			bot.button("No").click();
		});
	}

	@Test
	@RunWithProject("foo")
	public void shouldPromptDialogThenBuildDockerImageOnFirstCall()
			throws InterruptedException, org.mandas.docker.client.exceptions.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		bot.getDisplay().asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		bot.getDisplay().syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(ProgressHandler.class), ArgumentMatchers.any());
	}

	@Test
	@RunWithProject("foo")
	public void shouldBuildDockerImageImmediatelyOnSecondCall()
			throws InterruptedException, org.mandas.docker.client.exceptions.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		bot.getDisplay().asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		bot.getDisplay().syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(ProgressHandler.class), ArgumentMatchers.any());
		// when trying to call again, there should be no dialog
		bot.getDisplay().asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then a second call should have been done
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(2)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(ProgressHandler.class), ArgumentMatchers.any());
	}

	@Test
	@RunWithProject("foo")
	public void shouldNotBuildDockerImageOnSecondCallWhenAllConnectionWereRemoved()
			throws InterruptedException, org.mandas.docker.client.exceptions.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		bot.getDisplay().asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		bot.getDisplay().syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(30)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(ProgressHandler.class), ArgumentMatchers.any());
		// when trying to call again after connection was removed, there should
		// be an error dialog
		DockerConnectionManager.getInstance().removeConnection(dockerConnection);
		SWTUtils.asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click(), false);
		final SWTBotShell shell = bot.shell("Edit Configuration");
		assertThat(shell).isNotNull();
		assertThat(shell.bot().button("Run").isEnabled()).isFalse();
		// closing the wizard
		SWTUtils.asyncExec(() -> {
			shell.bot().button(IDialogConstants.CLOSE_LABEL).click();
		}, false);
		// do not save the config while closing
		bot.getDisplay().syncExec(() -> {
			bot.button(IDialogConstants.NO_LABEL).click();
		});
	}

	@RunWithProject("foo")
	public void shouldPromptForAnotherConnectionWhenBuildingDockerImageOnSecondCallAfterConnectionWasReplaced()
			throws InterruptedException, org.mandas.docker.client.exceptions.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		bot.getDisplay().asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		bot.getDisplay().syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(ProgressHandler.class), ArgumentMatchers.any());
		// when trying to call again after connection was replaced, there should
		// be an error dialog
		final DockerConnection dockerConnection2 = MockDockerConnectionFactory.from("Test 2", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection2);
		bot.getDisplay().asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		bot.getDisplay().syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(2)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(ProgressHandler.class), ArgumentMatchers.any());
	}

	@Test
	@RunWithProject("foo")
	public void shouldNotBuildDockerImageOnSecondCallWhenDockerfileWasRemoved()
			throws InterruptedException, org.mandas.docker.client.exceptions.DockerException, IOException, CoreException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		bot.getDisplay().asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		bot.getDisplay().syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(30)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(ProgressHandler.class), ArgumentMatchers.any());
		// when trying to call again after file was removed, there should
		// be an error dialog
		projectInit.getProject().findMember("Dockerfile").delete(true, new NullProgressMonitor());
		bot.toolbarDropDownButtonWithTooltip("Run").menuItem("1 foo_bar [latest]").click();
		final SWTBotShell shell = bot.shell(JobMessages.getString("BuildImageJob.title")); //$NON-NLS-1$
		assertThat(shell).isNotNull();
		// closing the dialog
		bot.getDisplay().syncExec(() -> {
			shell.bot().button(IDialogConstants.OK_LABEL).click();
		});
	}

}
