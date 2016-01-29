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

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.ProjectInitializationRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.RunWithProject;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearLaunchConfigurationsRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;

/**
 * Testing the {@link BuildDockerImageShortcut}
 */
public class BuildDockerImageShortcutSWTBotTest {

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			"org.eclipse.ui.resourcePerspective");

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule
	public ProjectInitializationRule projectInit = new ProjectInitializationRule();

	@Rule
	public ClearLaunchConfigurationsRule clearLaunchConfig = new ClearLaunchConfigurationsRule(
			IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView projectExplorerBotView;

	@Before
	public void setup() {
		this.projectExplorerBotView = bot.viewById("org.eclipse.ui.navigator.ProjectExplorer");
		this.projectExplorerBotView.setFocus();
	}

	/**
	 * @return the {@link SWTBotMenu} for the "Run as > Docker Image Build"
	 *         shortcut
	 */
	private SWTBotMenu getRunAsdockerImageBuildContextMenu(final String projectName, final String dockerFileName) {
		final SWTBotTreeItem fooProjectTreeItem = SWTUtils.getTreeItem(this.projectExplorerBotView, projectName);
		assertThat(fooProjectTreeItem).isNotNull();
		SWTUtils.syncExec(() -> fooProjectTreeItem.expand());
		final SWTBotTreeItem dockerfileTreeItem = SWTUtils.getTreeItem(fooProjectTreeItem, dockerFileName);
		assertThat(dockerfileTreeItem).isNotNull();
		SWTUtils.select(dockerfileTreeItem);
		final SWTBotMenu runAsDockerImageBuildMenu = SWTUtils.getContextMenu(this.projectExplorerBotView.bot().tree(),
				"Run As", "1 Docker Image Build");
		return runAsDockerImageBuildMenu;
	}

	@Test
	@RunWithProject("foo")
	public void shouldDisableCommandOnFirstCallWhenMissingConnection() {
		// given no connection
		// when
		SWTUtils.asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect an error dialog
		assertThat(bot.shell(LaunchMessages.getString("ImageBuildShortcut.error.msg"))).isNotNull();
		// closing the wizard
		SWTUtils.syncExec(() -> {
			bot.button("OK").click();
		});
	}

	@Test
	@RunWithProject("foo")
	public void shouldPromptDialogThenBuildDockerImageOnFirstCall()
			throws InterruptedException, com.spotify.docker.client.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		SWTUtils.asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		SWTUtils.syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				Matchers.any(Path.class), Matchers.any(String.class), Matchers.any(ProgressHandler.class),
				Matchers.anyVararg());
	}

	@Test
	@RunWithProject("foo")
	public void shouldBuildDockerImageImmediatelyOnSecondCall()
			throws InterruptedException, com.spotify.docker.client.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		SWTUtils.asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		SWTUtils.syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				Matchers.any(Path.class), Matchers.any(String.class), Matchers.any(ProgressHandler.class),
				Matchers.anyVararg());
		// when trying to call again, there should be no dialog
		SWTUtils.asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then a second call should have been done
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(2)).build(
				Matchers.any(Path.class), Matchers.any(String.class), Matchers.any(ProgressHandler.class),
				Matchers.anyVararg());
	}

	@Test
	@RunWithProject("foo")
	public void shouldNotBuildDockerImageOnSecondCallWhenAllConnectionWereRemoved()
			throws InterruptedException, com.spotify.docker.client.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		SWTUtils.asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		SWTUtils.syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(30)).times(1)).build(
				Matchers.any(Path.class), Matchers.any(String.class), Matchers.any(ProgressHandler.class),
				Matchers.anyVararg());
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
		SWTUtils.syncExec(() -> {
			bot.button(IDialogConstants.NO_LABEL).click();
		});
	}

	@RunWithProject("foo")
	public void shouldPromptForAnotherConnectionWhenBuildingDockerImageOnSecondCallAfterConnectionWasReplaced()
			throws InterruptedException, com.spotify.docker.client.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		SWTUtils.asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		SWTUtils.syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				Matchers.any(Path.class), Matchers.any(String.class), Matchers.any(ProgressHandler.class),
				Matchers.anyVararg());
		// when trying to call again after connection was replaced, there should
		// be an error dialog
		final DockerConnection dockerConnection2 = MockDockerConnectionFactory.from("Test 2", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection2);
		SWTUtils.asyncExec(() -> getRunAsdockerImageBuildContextMenu("foo", "Dockerfile").click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		assertThat(bot.shell(WizardMessages.getString("ImageBuildDialog.title"))).isNotNull();
		bot.textWithLabel(WizardMessages.getString("ImageBuildDialog.repoNameLabel")).setText("foo/bar:latest");
		// when launching the build
		SWTUtils.syncExec(() -> {
			bot.button("OK").click();
		});
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(2)).build(
				Matchers.any(Path.class), Matchers.any(String.class), Matchers.any(ProgressHandler.class),
				Matchers.anyVararg());
	}
}
