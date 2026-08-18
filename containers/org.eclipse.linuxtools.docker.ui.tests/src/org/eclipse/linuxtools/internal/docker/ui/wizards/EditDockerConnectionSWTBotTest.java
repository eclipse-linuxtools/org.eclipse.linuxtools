/*******************************************************************************
 * Copyright (c) 2015, 2023 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionState;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionStorageManager;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.linuxtools.internal.docker.core.UnixSocketConnectionSettings;
import org.eclipse.linuxtools.internal.docker.ui.launch.IBuildDockerImageLaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.docker.ui.launch.LaunchConfigurationUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerConfigFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerHostConfigFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CheckBoxAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseShellRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.RadioAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTBotViewRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TextAssertions;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit5.SWTBotJunit5Extension;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mandas.docker.client.DockerClient;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Testing the {@link EditDockerConnection} {@link Wizard}
 */
@ExtendWith(SWTBotJunit5Extension.class)
public class EditDockerConnectionSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();

	private static String PATH_TO_CERTS = System.getProperty("java.io.tmpdir");

	@RegisterExtension
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Order(4)
	@RegisterExtension
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);

	@TempDir
	public Path folder;

	@Order(2)
	@RegisterExtension
	public SWTBotViewRule dockerExplorer = new SWTBotViewRule(DockerExplorerView.VIEW_ID);

	@Order(1)
	@RegisterExtension
	public SWTBotViewRule dockerImages = new SWTBotViewRule(DockerImagesView.VIEW_ID);

	@Order(3)
	@RegisterExtension
	public SWTBotViewRule dockerContainers = new SWTBotViewRule(DockerContainersView.VIEW_ID);

	@BeforeEach
	public void setFocusOnDockerExplorerView() {
		this.dockerExplorer.bot().setFocus();
	}

	/**
	 * The wizard only accepts a Unix socket path that exists and is
	 * readable/writable, and {@link DockerConnection#ping()} refuses to open a
	 * connection whose socket does not exist. The CI machines have no Docker
	 * daemon (hence no <code>/var/run/docker.sock</code>), so the initial Unix
	 * socket connection points to a temporary file instead.
	 */
	private File initialDockerSocketFile;

	private String initialDockerSocketPath() throws IOException {
		if (initialDockerSocketFile == null) {
			initialDockerSocketFile = Files.createFile(folder.resolve("initial-docker.sock")).toFile();
		}
		return initialDockerSocketFile.getAbsolutePath();
	}

	private IDockerConnection configureUnixSocketConnection() throws IOException {
		return configureUnixSocketConnection("Test");
	}

	private IDockerConnection configureUnixSocketConnection(final String connectionName) throws IOException {
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(connectionName, client)
				.withUnixSocketConnectionSettings(initialDockerSocketPath());
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		return dockerConnection;
	}

	private IDockerConnection configureTCPConnection() {
		return configureTCPConnection("Test");
	}

	private IDockerConnection configureTCPConnection(final String connectionName) {
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(connectionName, client)
				.withTCPConnectionSettings("https://foo:1234", PATH_TO_CERTS);
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		return dockerConnection;
	}

	private void openConnectionEditionWizard(final String elementName) {
		final SWTBotTreeItem connectionItem = SWTUtils.getTreeItem(dockerExplorer.bot(), elementName); // $NON-NLS-1$
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorer.bot().bot().tree();
		dockerExplorerViewTreeBot.select(connectionItem);
		dockerExplorerViewTreeBot.contextMenu(WizardMessages.getString("ImageRunSelectionPage.editButton")).click(); //$NON-NLS-1$
		// Wait for the wizard and keep it activated. Without this the lookups
		// below race the dialog, and because bot.text(int) resolves against
		// whichever shell is currently active they silently return widgets of
		// the workbench window instead of the ones in this dialog.
		final SWTBotShell wizardShell = bot.shell(WizardMessages.getString("EditDockerConnection.title")); //$NON-NLS-1$
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("EditDockerConnection.title"))); //$NON-NLS-1$
		wizardShell.activate();
	}

	private SWTBotButton getFinishButton() {
		return bot.button("Finish"); //$NON-NLS-1$
	}

	/**
	 * Waits until the given connection is established. The Docker Explorer view
	 * (re)opens any connection it shows that is not established yet in a
	 * background job, and the mocked client makes that succeed, so a connection
	 * that was just configured or whose settings were just changed becomes
	 * established shortly after.
	 */
	private void waitUntilConnectionIsOpen(final IDockerConnection connection) {
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				return connection.isOpen();
			}

			@Override
			public String getFailureMessage() {
				return "Connection '" + connection.getName() + "' was not established, state is "
						+ connection.getState();
			}
		}, TimeUnit.SECONDS.toMillis(10));
	}

	/**
	 * Waits until the given (spied) connection was opened again after its
	 * invocations were cleared and is established.
	 */
	private void waitUntilConnectionIsReopened(final IDockerConnection connection) {
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				return connection.isOpen() && Mockito.mockingDetails(connection).getInvocations().stream()
						.anyMatch(invocation -> invocation.getMethod().getName().equals("open")
								&& Boolean.TRUE.equals(invocation.getArgument(0)));
			}

			@Override
			public String getFailureMessage() {
				return "Connection '" + connection.getName() + "' was not opened again, state is "
						+ connection.getState();
			}
		}, TimeUnit.SECONDS.toMillis(10));
	}

	/**
	 * Waits until the Docker Explorer view shows a connection whose label
	 * starts with the given name and contains the given text.
	 */
	private SWTBotTreeItem waitForConnectionTreeItem(final String connectionName, final String expectedText) {
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				final SWTBotTreeItem item = SWTUtils.getTreeItem(dockerExplorer.bot().bot().tree(), connectionName);
				return item != null && item.getText().contains(expectedText);
			}

			@Override
			public String getFailureMessage() {
				return "Docker Explorer view did not show connection '" + connectionName + "' with '" + expectedText
						+ "'";
			}
		}, TimeUnit.SECONDS.toMillis(10));
		return SWTUtils.getTreeItem(dockerExplorer.bot(), connectionName);
	}

	private String configureRunImageLaunchConfiguration(final IDockerConnection connection, final String networkMode) {
		final IDockerImage image = MockDockerImageFactory.name("images").connection(connection).build();
		final DockerContainerConfig containerConfig = MockDockerContainerConfigFactory.cmd("cmd").build();
		final IDockerHostConfig hostConfig = MockDockerHostConfigFactory.publishAllPorts(true).networkMode(networkMode)
				.build();
		final ILaunchConfiguration runImageLaunchConfiguration = LaunchConfigurationUtils
				.createRunImageLaunchConfiguration(image, containerConfig, hostConfig, new ArrayList<>(),
						"some_container", false);
		return runImageLaunchConfiguration.getName();
	}

	private String configureBuildImageLaunchConfiguration(final IDockerConnection connection) throws CoreException {
		final IResource mockDockerFile = Mockito.mock(IResource.class, Mockito.RETURNS_DEEP_STUBS);
		final IPath mockDockerFilePath = Mockito.mock(IPath.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(mockDockerFile.getFullPath()).thenReturn(mockDockerFilePath);
		Mockito.when(mockDockerFilePath.removeLastSegments(ArgumentMatchers.anyInt())).thenReturn(mockDockerFilePath);
		Mockito.when(mockDockerFilePath.toString()).thenReturn("/path/to/dockerfile");
		final ILaunchConfiguration buildImageLaunchConfiguration = LaunchConfigurationUtils
				.createBuildImageLaunchConfiguration(connection, "foo/bar:latest", mockDockerFile);
		return buildImageLaunchConfiguration.getName();
	}

	@Test
	public void shouldShowUnixSocketConnectionSettingsWithValidConnectionAvailable() throws IOException {
		// given
		configureUnixSocketConnection();
		openConnectionEditionWizard("Test");

		// then
		// Connection name
		TextAssertions.assertThat(bot.text(0)).isEnabled().textEquals("Test");
		// "Unix socket" radio should be enabled and selected
		RadioAssertion.assertThat(bot.radio(0)).isEnabled().isSelected();
		// "Unix socket path" text should be disabled and not empty
		TextAssertions.assertThat(bot.text(1)).isEnabled().textEquals("unix://" + initialDockerSocketPath());
		// "TCP Connection" radio should be unselected and disabled
		RadioAssertion.assertThat(bot.radio(1)).isEnabled().isNotSelected();
		// "URI" should be disabled and empty
		TextAssertions.assertThat(bot.text(2)).isNotEnabled().isEmpty();
		// "Enable Auth" checkbox should be unselected and disabled
		CheckBoxAssertions.assertThat(bot.checkBox(0)).isNotEnabled().isNotChecked();
		// "Path" for certs should be disabled but not empty
		TextAssertions.assertThat(bot.text(3)).isNotEnabled().isEmpty();
	}

	@Test
	public void shouldUpdateUnixSocketConnectionSettingsWithValidConnectionAvailable() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");

		// when: switch to TCP connection settings and save
		bot.text(0).setText("foo");
		bot.radio(1).click();
		bot.text(2).setText("http://foo:1234");
		bot.checkBox(0).select();
		bot.text(3).setText(PATH_TO_CERTS);
		getFinishButton().click();

		// then
		final IDockerConnection fooConnection = DockerConnectionManager.getInstance().findConnection("foo");
		assertThat(fooConnection).isNotNull();
		assertThat(fooConnection.getName()).isEqualTo("foo");
		assertThat(fooConnection.getSettings()).isInstanceOf(TCPConnectionSettings.class);
		final TCPConnectionSettings connectionSettings = (TCPConnectionSettings) fooConnection.getSettings();
		assertThat(connectionSettings.getHost()).isEqualTo("https://foo:1234");
		assertThat(connectionSettings.isTlsVerify()).isEqualTo(true);
		assertThat(connectionSettings.getPathToCertificates()).isEqualTo(PATH_TO_CERTS);
	}

	@Test
	public void shouldUpdateTCPConnectionSettingsWithValidConnectionAvailable() throws IOException {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");

		// when: switch to TCP connection settings and save
		bot.text(0).setText("foo");
		bot.radio(0).click();
		bot.text(1).setText(initialDockerSocketPath());
		getFinishButton().click();

		// then
		final IDockerConnection fooConnection = DockerConnectionManager.getInstance().findConnection("foo");
		assertThat(fooConnection).isNotNull();
		assertThat(fooConnection.getName()).isEqualTo("foo");
		assertThat(fooConnection.getSettings()).isInstanceOf(UnixSocketConnectionSettings.class);
		final UnixSocketConnectionSettings connectionSettings = (UnixSocketConnectionSettings) fooConnection
				.getSettings();
		assertThat(connectionSettings.getPath()).isEqualTo("unix://" + initialDockerSocketPath());
	}

	@Test
	public void shouldReportProblemWhenMissingName() throws IOException {
		// given
		configureUnixSocketConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(0).setText("");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldReportProblemWhenMissingUnixSocketPath() throws IOException {
		// given
		configureUnixSocketConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(1).setText("");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldReportProblemWhenInvalidUnixSocketPath() throws IOException {
		// given
		configureUnixSocketConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(1).setText("/invalid/path");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldReportProblemWhenMissingHost() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(2).setText("");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldReportProblemWhenInvalidHostMissingScheme() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(2).setText("foo");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldReportProblemWhenInvalidHostInvalidScheme() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(2).setText("ftp://foo");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldReportProblemWhenInvalidHostMissingPort() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(2).setText("http://foo");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldReportProblemWhenMissingPathToCerts() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(3).setText("");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldReportProblemWhenInvalidPathToCerts() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		assertTrue(getFinishButton().isEnabled());
		// when
		bot.text(3).setText("/invalid/path");
		// then
		assertFalse(getFinishButton().isEnabled());
	}

	@Test
	public void shouldUpdateLaunchConfigurationWhenConnectionNameChanged() throws CoreException {
		// given
		final IDockerConnection connection = configureTCPConnection("Test");
		final String buildImageLaunchConfigurationName = configureBuildImageLaunchConfiguration(connection);
		final ILaunchConfiguration buildDockerImageLaunchConfig = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, buildImageLaunchConfigurationName);
		assertThat(buildDockerImageLaunchConfig).isNotNull();
		assertThat(buildDockerImageLaunchConfig.getAttribute(IDockerImageBuildOptions.DOCKER_CONNECTION, ""))
				.isEqualTo("Test");
		final String runImageLaunchConfigurationName = configureRunImageLaunchConfiguration(connection, "bridge");
		final ILaunchConfiguration runDockerImageLaunchConfig = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, runImageLaunchConfigurationName);
		assertThat(runDockerImageLaunchConfig).isNotNull();
		assertThat(runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				"")).isEqualTo("Test");
		assertThat(
				runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE, ""))
						.isEqualTo("bridge");

		// when
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		getFinishButton().click();

		// then
		final ILaunchConfiguration updatedBuildDockerImageLaunchConfig = LaunchConfigurationUtils
				.getLaunchConfigurationByName(IBuildDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID,
						buildImageLaunchConfigurationName);
		assertThat(updatedBuildDockerImageLaunchConfig).isNotNull();
		assertThat(updatedBuildDockerImageLaunchConfig.getAttribute(IDockerImageBuildOptions.DOCKER_CONNECTION, ""))
				.isEqualTo("foo");
		final ILaunchConfiguration updatedRunDockerImageLaunchConfig = LaunchConfigurationUtils
				.getLaunchConfigurationByName(IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID,
						runImageLaunchConfigurationName);
		assertThat(updatedRunDockerImageLaunchConfig).isNotNull();
		assertThat(updatedRunDockerImageLaunchConfig
				.getAttribute(IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME, "")).isEqualTo("foo");
		assertThat(updatedRunDockerImageLaunchConfig
				.getAttribute(IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE, "")).isEqualTo("bridge");
	}

	@Test
	public void shouldRefreshDockerExplorerViewWhenConnectionNameChanged() {
		// given
		configureTCPConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// when
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// then
		final SWTBotTreeItem updatedConnectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "foo");
		assertThat(updatedConnectionTreeItem).isNotNull();
	}

	@Test
	public void shouldRefreshDockerImagesViewWhenConnectionNameChanges() {
		// given
		configureTCPConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// when
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// then
		final DockerImagesView dockerImagesView = dockerImages.view();
		final String formTitle = UIThreadRunnable.syncExec(() -> dockerImagesView.getFormTitle());
		assertThat(formTitle).contains("foo");
	}

	@Test
	public void shouldRefreshDockerContainersViewWhenConnectionNameChanges() {
		// given
		configureTCPConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// when
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// then
		final DockerContainersView dockerContainersView = dockerContainers.view();
		final String formTitle = UIThreadRunnable.syncExec(() -> dockerContainersView.getFormTitle());
		assertThat(formTitle).contains("foo");
	}

	@Test
	public void shouldRefreshDockerExplorerViewWhenNameChangedTwice() {
		// given
		configureTCPConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// name change #1
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// name change #2
		openConnectionEditionWizard("foo");
		bot.text(0).setText("bar");
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// then
		final SWTBotTreeItem updatedConnectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "bar");
		assertThat(updatedConnectionTreeItem).isNotNull();
	}

	@Test
	public void shouldRefreshDockerExplorerViewWhenUnixSockerConnectionSettingsChanged() throws IOException {
		// given
		dockerContainers.close();
		dockerImages.close();
		final IDockerConnection connection = configureUnixSocketConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		waitUntilConnectionIsOpen(connection);
		Mockito.clearInvocations(connection);
		final File tmpDockerSocketFile = Files.createFile(folder.resolve("docker.sock")).toFile();
		// when
		openConnectionEditionWizard("Test");
		bot.text(1).setText(tmpDockerSocketFile.getAbsolutePath());
		getFinishButton().click();
		// then
		final SWTBotTreeItem updatedConnectionTreeItem = waitForConnectionTreeItem("Test",
				tmpDockerSocketFile.getAbsolutePath());
		assertThat(updatedConnectionTreeItem).isNotNull();
		assertThat(updatedConnectionTreeItem.getText()).contains(tmpDockerSocketFile.getAbsolutePath());
		// the connection is re-established with the new settings, which
		// refreshes the list of containers and images
		waitUntilConnectionIsReopened(connection);
		Mockito.verify(connection, Mockito.atLeastOnce()).getContainers(true);
		Mockito.verify(connection, Mockito.atLeastOnce()).getImages(true);
	}

	@Test
	public void shouldRefreshDockerExplorerViewWhenTCPConnectionSettingsChanged() {
		// given
		dockerContainers.close();
		dockerImages.close();
		final IDockerConnection connection = configureTCPConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		waitUntilConnectionIsOpen(connection);
		Mockito.clearInvocations(connection);
		// when
		openConnectionEditionWizard("Test");
		bot.text(2).setText("https://foo.bar:1234");
		getFinishButton().click();
		// then
		final SWTBotTreeItem updatedConnectionTreeItem = waitForConnectionTreeItem("Test", "https://foo.bar:1234");
		assertThat(updatedConnectionTreeItem).isNotNull();
		assertThat(updatedConnectionTreeItem.getText()).contains("https://foo.bar:1234");
		// the connection is re-established with the new settings, which
		// refreshes the list of containers and images
		waitUntilConnectionIsReopened(connection);
		Mockito.verify(connection, Mockito.atLeastOnce()).getContainers(true);
		Mockito.verify(connection, Mockito.atLeastOnce()).getImages(true);
	}

	@Test
	public void shouldSaveConnectionWhenNameChanged() {
		// given
		final IDockerConnection connection = configureTCPConnection("Test");
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connection);
		DockerConnectionManagerUtils.configureConnectionManager(connectionStorageManager);
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// let's ignore the connection savings that may have occurred when
		// adding elements from the extension points
		Mockito.reset(connectionStorageManager);
		// when
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		getFinishButton().click();
		// then
		final IDockerConnection foundConnection = DockerConnectionManager.getInstance().findConnection("foo");
		assertThat(foundConnection).isNotNull();
		Mockito.verify(connectionStorageManager).saveConnections(ArgumentMatchers.anyList());
	}

	@Test
	public void shouldSaveConnectionWhenUnixSocketConnectionSettingsChanged() throws IOException {
		// given
		final IDockerConnection connection = configureUnixSocketConnection("Test");
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connection);
		DockerConnectionManagerUtils.configureConnectionManager(connectionStorageManager);
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		final File tmpDockerSocketFile = Files.createFile(folder.resolve("docker.sock")).toFile();
		// let's ignore the connection savings that may have occurred when
		// adding elements from the extension points
		Mockito.reset(connectionStorageManager);
		// when
		openConnectionEditionWizard("Test");
		bot.text(1).setText(tmpDockerSocketFile.getAbsolutePath());
		getFinishButton().click();
		// then
		final IDockerConnection foundConnection = DockerConnectionManager.getInstance().findConnection("Test");
		assertThat(foundConnection).isNotNull();
		assertThat(foundConnection.getSettings()).isNotNull()
				.isEqualTo(new UnixSocketConnectionSettings(tmpDockerSocketFile.getAbsolutePath()));
		Mockito.verify(connectionStorageManager).saveConnections(ArgumentMatchers.anyList());
	}

	@Test
	public void shouldSaveConnectionWhenTCPConnectionSettingsChanged() {
		// given
		final IDockerConnection connection = configureTCPConnection("Test");
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connection);
		DockerConnectionManagerUtils.configureConnectionManager(connectionStorageManager);
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// let's ignore the connection savings that may have occurred when
		// adding elements from the extension points
		Mockito.reset(connectionStorageManager);
		// when
		openConnectionEditionWizard("Test");
		bot.text(2).setText("https://foo.bar:1234");
		getFinishButton().click();
		// then
		final IDockerConnection foundConnection = DockerConnectionManager.getInstance().findConnection("Test");
		assertThat(foundConnection).isNotNull();
		assertThat(foundConnection.getSettings()).isNotNull()
				.isEqualTo(new TCPConnectionSettings("https://foo.bar:1234", PATH_TO_CERTS));
		Mockito.verify(connectionStorageManager).saveConnections(ArgumentMatchers.anyList());
	}

	@Test
	public void shouldSaveConnectionWhenNameAndTCPConnectionSettingsChanged() {
		// given
		final IDockerConnection connection = configureTCPConnection("Test");
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connection);
		DockerConnectionManagerUtils.configureConnectionManager(connectionStorageManager);
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// let's ignore the connection savings that may have occurred when
		// adding elements from the extension points
		Mockito.reset(connectionStorageManager);
		// when
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		bot.text(2).setText("https://foo.bar:1234");
		getFinishButton().click();
		// then
		final IDockerConnection foundConnection = DockerConnectionManager.getInstance().findConnection("foo");
		assertThat(foundConnection).isNotNull();
		assertThat(foundConnection.getSettings()).isNotNull()
				.isEqualTo(new TCPConnectionSettings("https://foo.bar:1234", PATH_TO_CERTS));
		Mockito.verify(connectionStorageManager).saveConnections(ArgumentMatchers.anyList());
	}

	@Test
	public void shouldResetConnectionStateWhenUnixConnectionSettingsChanged() throws IOException {
		// given
		dockerContainers.close();
		dockerImages.close();
		final IDockerConnection connection = configureUnixSocketConnection("Test");
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connection);
		DockerConnectionManagerUtils.configureConnectionManager(connectionStorageManager);
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		final File tmpDockerSocketFile = Files.createFile(folder.resolve("docker.sock")).toFile();
		waitUntilConnectionIsOpen(connection);
		Mockito.clearInvocations(connection);
		// when
		openConnectionEditionWizard("Test");
		bot.text(1).setText(tmpDockerSocketFile.getAbsolutePath());
		getFinishButton().click();
		// then
		final IDockerConnection foundConnection = DockerConnectionManager.getInstance().findConnection("Test");
		assertThat(foundConnection).isNotNull();
		assertThat(foundConnection.getSettings()).isNotNull()
				.isEqualTo(new UnixSocketConnectionSettings(tmpDockerSocketFile.getAbsolutePath()));
		// the connection state was reset, so the Docker Explorer view opens the
		// connection again, this time with the new settings
		waitUntilConnectionIsReopened(connection);
		assertThat(foundConnection.getState()).isEqualTo(EnumDockerConnectionState.ESTABLISHED);
	}

	@Test
	public void shouldResetConnectionStateWhenTCPConnectionSettingsChanged() {
		// given
		dockerContainers.close();
		dockerImages.close();
		final IDockerConnection connection = configureTCPConnection("Test");
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connection);
		DockerConnectionManagerUtils.configureConnectionManager(connectionStorageManager);
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		waitUntilConnectionIsOpen(connection);
		Mockito.clearInvocations(connection);
		// when
		openConnectionEditionWizard("Test");
		bot.text(2).setText("https://foo.bar:1234");
		getFinishButton().click();
		// then
		final IDockerConnection foundConnection = DockerConnectionManager.getInstance().findConnection("Test");
		assertThat(foundConnection).isNotNull();
		assertThat(foundConnection.getSettings()).isNotNull()
				.isEqualTo(new TCPConnectionSettings("https://foo.bar:1234", PATH_TO_CERTS));
		// the connection state was reset, so the Docker Explorer view opens the
		// connection again, this time with the new settings
		waitUntilConnectionIsReopened(connection);
		assertThat(foundConnection.getState()).isEqualTo(EnumDockerConnectionState.ESTABLISHED);
	}

}
