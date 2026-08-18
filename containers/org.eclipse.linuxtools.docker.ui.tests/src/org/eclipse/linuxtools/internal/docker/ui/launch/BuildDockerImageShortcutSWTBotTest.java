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
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.ProgressHandler;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Testing the {@link BuildDockerImageShortcut}
 */
public class BuildDockerImageShortcutSWTBotTest {

	@RegisterExtension
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			"org.eclipse.linuxtools.docker.ui.perspective");

	@Order(4)
	@RegisterExtension
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Order(1)
	@RegisterExtension
	public ProjectInitializationRule projectInit = new ProjectInitializationRule();

	@Order(3)
	@RegisterExtension
	public ClearLaunchConfigurationsRule clearLaunchConfig = new ClearLaunchConfigurationsRule(
			IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID);

	@Order(2)
	@RegisterExtension
	public ProjectExplorerViewRule projectExplorerViewRule = new ProjectExplorerViewRule();

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();

	/**
	 * @return the {@link SWTBotMenu} for the "Run as > Docker Image Build"
	 *         shortcut
	 */
	private SWTBotMenu getRunAsdockerImageBuildContextMenu(final String projectName, final String dockerFileName) {
		final SWTBotView projectExplorerBotView = this.projectExplorerViewRule.getProjectExplorerBotView();
		// make sure the project explorer view is the active part: the "Run As"
		// contextual menu and the launch shortcut are computed from the selection
		// of the active part, and a previous build activates the console view
		// (from a job scheduled with a delay, hence the wait). Focusing the view
		// is not enough to activate the part, it has to be shown.
		SWTUtils.waitForJobsToComplete();
		projectExplorerBotView.show();
		projectExplorerBotView.setFocus();
		final SWTBotTreeItem fooProjectTreeItem = SWTUtils.getTreeItem(projectExplorerBotView, projectName);
		assertThat(fooProjectTreeItem).isNotNull();
		UIThreadRunnable.syncExec(() -> fooProjectTreeItem.expand());
		final SWTBotTreeItem dockerfileTreeItem = SWTUtils.getTreeItem(fooProjectTreeItem, dockerFileName);
		assertThat(dockerfileTreeItem).isNotNull();
		// select the item itself: SWTUtils.select(item, matchers...) with no matchers
		// filters the children against nothing and ends up selecting nothing at all,
		// which leaves the workbench selection empty and the "Run As" menu absent
		UIThreadRunnable.syncExec(() -> dockerfileTreeItem.select());
		final SWTBotMenu runAsDockerImageBuildMenu = SWTUtils.getContextMenu(projectExplorerBotView.bot().tree(),
				"Run As", "1 Docker Image Build");
		return runAsDockerImageBuildMenu;
	}

	/**
	 * Fills in the image name in the "Build a Docker Image" dialog and confirms
	 * it. The dialog has to be activated and the widgets looked up through its
	 * own bot: lookups on the global bot go to whichever shell is active, which
	 * without a window manager is still the workbench window, so the "OK" click
	 * would never reach this dialog.
	 */
	private void fillAndConfirmImageBuildDialog(final String imageName) {
		final SWTBotShell dialog = bot.shell(WizardMessages.getString("ImageBuildDialog.title")); //$NON-NLS-1$
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("ImageBuildDialog.title"))); //$NON-NLS-1$
		dialog.activate();
		dialog.bot().textWithLabel(WizardMessages.getString("ImageBuildName.label")).setText(imageName); //$NON-NLS-1$
		final SWTBotButton okButton = dialog.bot().button(IDialogConstants.OK_LABEL);
		bot.getDisplay().syncExec(() -> okButton.click());
	}

	@Test
	@RunWithProject("foo")
	public void shouldDisableCommandOnFirstCallWhenMissingConnection() {
		// given no connection
		ClearConnectionManagerRule.removeAllConnections(DockerConnectionManager.getInstance());
		// when
		final SWTBotMenu runAsMenu = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		bot.getDisplay().asyncExec(() -> runAsMenu.click());
		// then expect an error dialog because no Docker connection exists
		final SWTBotShell noConnectionShell = bot
				.shell(LaunchMessages.getString("BuildDockerImageShortcut.no.connections.msg"));
		assertThat(noConnectionShell).isNotNull();
		// closing the dialog
		noConnectionShell.bot().button(IDialogConstants.NO_LABEL).click();
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
		final SWTBotMenu runAsMenu = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		bot.getDisplay().asyncExec(() -> runAsMenu.click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		fillAndConfirmImageBuildDialog("foo/bar:latest");
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(String.class), ArgumentMatchers.any(ProgressHandler.class),
				ArgumentMatchers.any(DockerClient.BuildParam[].class));
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
		final SWTBotMenu runAsMenu = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		bot.getDisplay().asyncExec(() -> runAsMenu.click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		fillAndConfirmImageBuildDialog("foo/bar:latest");
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(String.class), ArgumentMatchers.any(ProgressHandler.class),
				ArgumentMatchers.any(DockerClient.BuildParam[].class));
		// when trying to call again, there should be no dialog
		final SWTBotMenu runAsMenu2 = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		bot.getDisplay().asyncExec(() -> runAsMenu2.click());
		// then a second call should have been done
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(2)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(String.class), ArgumentMatchers.any(ProgressHandler.class),
				ArgumentMatchers.any(DockerClient.BuildParam[].class));
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
		final SWTBotMenu runAsMenu = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		bot.getDisplay().asyncExec(() -> runAsMenu.click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		fillAndConfirmImageBuildDialog("foo/bar:latest");
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(30)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(String.class), ArgumentMatchers.any(ProgressHandler.class),
				ArgumentMatchers.any(DockerClient.BuildParam[].class));
		// when trying to call again after connection was removed, there should
		// be an error dialog
		DockerConnectionManager.getInstance().removeConnection(dockerConnection);
		final SWTBotMenu runAsMenu2 = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		SWTUtils.asyncExec(() -> runAsMenu2.click(), false);
		final SWTBotShell shell = bot.shell("Edit Configuration");
		assertThat(shell).isNotNull();
		assertThat(shell.bot().button("Run").isEnabled()).isFalse();
		// closing the dialog: the shortcut left the configuration dirty
		// (no connection), so the dialog asks whether to save it
		shell.bot().button(IDialogConstants.CLOSE_LABEL).click();
		final SWTBotShell saveChangesShell = bot.shell("Save Changes");
		saveChangesShell.bot().button("Don't Save").click();
		bot.waitUntil(Conditions.shellCloses(shell));
	}

	@RunWithProject("foo")
	public void shouldPromptForAnotherConnectionWhenBuildingDockerImageOnSecondCallAfterConnectionWasReplaced()
			throws InterruptedException, org.mandas.docker.client.exceptions.DockerException, IOException {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		final SWTBotMenu runAsMenu = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		bot.getDisplay().asyncExec(() -> runAsMenu.click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		fillAndConfirmImageBuildDialog("foo/bar:latest");
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(String.class), ArgumentMatchers.any(ProgressHandler.class),
				ArgumentMatchers.any(DockerClient.BuildParam[].class));
		// when trying to call again after connection was replaced, there should
		// be an error dialog
		final DockerConnection dockerConnection2 = MockDockerConnectionFactory.from("Test 2", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection2);
		final SWTBotMenu runAsMenu2 = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		bot.getDisplay().asyncExec(() -> runAsMenu2.click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		fillAndConfirmImageBuildDialog("foo/bar:latest");
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(3)).times(2)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(String.class), ArgumentMatchers.any(ProgressHandler.class),
				ArgumentMatchers.any(DockerClient.BuildParam[].class));
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
		final SWTBotMenu runAsMenu = getRunAsdockerImageBuildContextMenu("foo", "Dockerfile");
		bot.getDisplay().asyncExec(() -> runAsMenu.click());
		// then expect a dialog, fill the "repository" text field and click "Ok"
		fillAndConfirmImageBuildDialog("foo/bar:latest");
		// then the 'DockerConnection#buildImage(...) method should have been
		// called within the specified timeout
		Mockito.verify(client, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(30)).times(1)).build(
				ArgumentMatchers.any(Path.class), ArgumentMatchers.any(String.class),
				ArgumentMatchers.any(String.class), ArgumentMatchers.any(ProgressHandler.class),
				ArgumentMatchers.any(DockerClient.BuildParam[].class));
		// when trying to call again after file was removed, there should
		// be an error dialog
		projectInit.getProject().findMember("Dockerfile").delete(true, new NullProgressMonitor());
		// re-launch the configuration from the launch history (the label of the
		// history entries has a decoration appended, so match on its start)
		bot.menu("Run").menu("Run History")
				.menu(WidgetMatcherFactory.<MenuItem>withRegex("1 foo_bar \\[latest\\].*"), false, 0).click();
		final SWTBotShell shell = bot.shell(JobMessages.getString("BuildImageJob.title")); //$NON-NLS-1$
		assertThat(shell).isNotNull();
		// closing the dialog
		bot.getDisplay().syncExec(() -> {
			shell.bot().button(IDialogConstants.OK_LABEL).click();
		});
	}

}
