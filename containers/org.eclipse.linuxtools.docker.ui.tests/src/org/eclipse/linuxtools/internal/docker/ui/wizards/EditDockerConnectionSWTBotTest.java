/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
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

import java.io.File;
import java.io.IOException;
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
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ButtonAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CheckBoxAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseShellRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.RadioAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTBotViewRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TextAssertion;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link EditDockerConnection} {@link Wizard}
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class EditDockerConnectionSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();

	private static String PATH_TO_CERTS = System.getProperty("java.io.tmpdir");

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public SWTBotViewRule dockerExplorer = new SWTBotViewRule(DockerExplorerView.VIEW_ID);

	@Rule
	public SWTBotViewRule dockerImages = new SWTBotViewRule(DockerImagesView.VIEW_ID);

	@Rule
	public SWTBotViewRule dockerContainers = new SWTBotViewRule(DockerContainersView.VIEW_ID);

	@Before
	public void setFocusOnDockerExplorerView() {
		this.dockerExplorer.bot().setFocus();
	}

	private IDockerConnection configureUnixSocketConnection() {
		return configureUnixSocketConnection("Test");
	}

	private IDockerConnection configureUnixSocketConnection(final String connectionName) {
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(connectionName, client)
				.withUnixSocketConnectionSettings("/var/run/docker.sock");
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		return dockerConnection;
	}

	private IDockerConnection configureTCPConnection() {
		return configureTCPConnection("Test");
	}

	private IDockerConnection configureTCPConnection(final String connectionName) {
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(connectionName, client)
				.withTCPConnectionSettings("https://foo:1234", PATH_TO_CERTS, EnumDockerConnectionState.ESTABLISHED);
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		return dockerConnection;
	}

	private void openConnectionEditionWizard(final String elementName) {
		final SWTBotTreeItem connectionItem = SWTUtils.getTreeItem(dockerExplorer.bot(), elementName); // $NON-NLS-1$
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorer.bot().bot().tree();
		dockerExplorerViewTreeBot.select(connectionItem);
		dockerExplorerViewTreeBot.contextMenu(WizardMessages.getString("ImageRunSelectionPage.editButton")).click(); //$NON-NLS-1$
	}

	private SWTBotButton getFinishButton() {
		return bot.button("Finish"); //$NON-NLS-1$
	}

	private String configureRunImageLaunchConfiguration(final IDockerConnection connection) {
		final IDockerImage image = MockDockerImageFactory.name("image").connection(connection).build();
		final DockerContainerConfig containerConfig = MockDockerContainerConfigFactory.cmd("cmd").build();
		final IDockerHostConfig hostConfig = MockDockerHostConfigFactory.publishAllPorts(true).build();
		final ILaunchConfiguration runImageLaunchConfiguration = LaunchConfigurationUtils
				.createRunImageLaunchConfiguration(image, containerConfig, hostConfig, "some_container", false);
		return runImageLaunchConfiguration.getName();
	}

	private String configureBuildImageLaunchConfiguration(final IDockerConnection connection) throws CoreException {
		final IResource mockDockerFile = Mockito.mock(IResource.class, Mockito.RETURNS_DEEP_STUBS);
		final IPath mockDockerFilePath = Mockito.mock(IPath.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(mockDockerFile.getFullPath()).thenReturn(mockDockerFilePath);
		Mockito.when(mockDockerFilePath.removeLastSegments(Matchers.anyInt())).thenReturn(mockDockerFilePath);
		Mockito.when(mockDockerFilePath.toString()).thenReturn("/path/to/dockerfile");
		final ILaunchConfiguration buildImageLaunchConfiguration = LaunchConfigurationUtils
				.createBuildImageLaunchConfiguration(connection, "foo/bar:latest", mockDockerFile);
		return buildImageLaunchConfiguration.getName();
	}

	@Test
	public void shouldShowUnixSocketConnectionSettingsWithValidConnectionAvailable() {
		// given
		configureUnixSocketConnection();
		openConnectionEditionWizard("Test");

		// then
		// Connection name
		TextAssertion.assertThat(bot.text(0)).isEnabled().textEquals("Test");
		// "Unix socket" radio should be enabled and selected
		RadioAssertion.assertThat(bot.radio(0)).isEnabled().isSelected();
		// "Unix socket path" text should be disabled and not empty
		TextAssertion.assertThat(bot.text(1)).isEnabled().textEquals("unix:///var/run/docker.sock");
		// "TCP Connection" radio should be unselected and disabled
		RadioAssertion.assertThat(bot.radio(1)).isEnabled().isNotSelected();
		// "URI" should be disabled and empty
		TextAssertion.assertThat(bot.text(2)).isNotEnabled().isEmpty();
		// "Enable Auth" checkbox should be unselected and disabled
		CheckBoxAssertion.assertThat(bot.checkBox(0)).isNotEnabled().isNotChecked();
		// "Path" for certs should be disabled but not empty
		TextAssertion.assertThat(bot.text(3)).isNotEnabled().isEmpty();
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
	public void shouldUpdateTCPConnectionSettingsWithValidConnectionAvailable() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");

		// when: switch to TCP connection settings and save
		bot.text(0).setText("foo");
		bot.radio(0).click();
		bot.text(1).setText("/var/run/docker.sock");
		getFinishButton().click();

		// then
		final IDockerConnection fooConnection = DockerConnectionManager.getInstance().findConnection("foo");
		assertThat(fooConnection).isNotNull();
		assertThat(fooConnection.getName()).isEqualTo("foo");
		assertThat(fooConnection.getSettings()).isInstanceOf(UnixSocketConnectionSettings.class);
		final UnixSocketConnectionSettings connectionSettings = (UnixSocketConnectionSettings) fooConnection
				.getSettings();
		assertThat(connectionSettings.getPath()).isEqualTo("unix:///var/run/docker.sock");
	}

	@Test
	public void shouldReportProblemWhenMissingName() {
		// given
		configureUnixSocketConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(0).setText("");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
	}

	@Test
	public void shouldReportProblemWhenMissingUnixSocketPath() {
		// given
		configureUnixSocketConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(1).setText("");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
	}

	@Test
	public void shouldReportProblemWhenInvalidUnixSocketPath() {
		// given
		configureUnixSocketConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(1).setText("/invalid/path");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
	}

	@Test
	public void shouldReportProblemWhenMissingHost() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(2).setText("");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
	}

	@Test
	public void shouldReportProblemWhenInvalidHostMissingScheme() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(2).setText("foo");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
	}

	@Test
	public void shouldReportProblemWhenInvalidHostInvalidScheme() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(2).setText("ftp://foo");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
	}

	@Test
	public void shouldReportProblemWhenInvalidHostMissingPort() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(2).setText("http://foo");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
	}

	@Test
	public void shouldReportProblemWhenMissingPathToCerts() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(3).setText("");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
	}

	@Test
	public void shouldReportProblemWhenInvalidPathToCerts() {
		// given
		configureTCPConnection();
		openConnectionEditionWizard("Test");
		ButtonAssertion.assertThat(getFinishButton()).isEnabled();
		// when
		bot.text(3).setText("/invalid/path");
		// then
		ButtonAssertion.assertThat(getFinishButton()).isNotEnabled();
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
		final String runImageLaunchConfigurationName = configureRunImageLaunchConfiguration(connection);
		final ILaunchConfiguration runDockerImageLaunchConfig = LaunchConfigurationUtils.getLaunchConfigurationByName(
				IRunDockerImageLaunchConfigurationConstants.CONFIG_TYPE_ID, runImageLaunchConfigurationName);
		assertThat(runDockerImageLaunchConfig).isNotNull();
		assertThat(runDockerImageLaunchConfig.getAttribute(IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				"")).isEqualTo("Test");

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
	}

	@Test
	public void shouldRefreshDockerExplorerViewWhenConnectionNameChanged() {
		// given
		final IDockerConnection connection = configureTCPConnection("Test");
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
		final IDockerConnection connection = configureTCPConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// when
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// then
		final DockerImagesView dockerImagesView = dockerImages.view();
		final String formTitle = SWTUtils.syncExec(() -> dockerImagesView.getFormTitle());
		assertThat(formTitle).contains("foo");
	}

	@Test
	public void shouldRefreshDockerContainersViewWhenConnectionNameChanges() {
		// given
		final IDockerConnection connection = configureTCPConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// when
		openConnectionEditionWizard("Test");
		bot.text(0).setText("foo");
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// then
		final DockerContainersView dockerContainersView = dockerContainers.view();
		final String formTitle = SWTUtils.syncExec(() -> dockerContainersView.getFormTitle());
		assertThat(formTitle).contains("foo");
	}

	@Test
	public void shouldRefreshDockerExplorerViewWhenNameChangedTwice() {
		// given
		final IDockerConnection connection = configureTCPConnection("Test");
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
		final File tmpDockerSocketFile = folder.newFile("docker.sock");
		// when
		openConnectionEditionWizard("Test");
		bot.text(1).setText(tmpDockerSocketFile.getAbsolutePath());
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// then
		final SWTBotTreeItem updatedConnectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(updatedConnectionTreeItem).isNotNull();
		assertThat(updatedConnectionTreeItem.getText()).contains(tmpDockerSocketFile.getAbsolutePath());
		// list of containers and images should have been refreshed
		Mockito.verify(connection, Mockito.times(0)).getContainers(true);
		Mockito.verify(connection, Mockito.times(0)).getImages(true);
	}

	@Test
	public void shouldRefreshDockerExplorerViewWhenTCPConnectionSettingsChanged() {
		// given
		dockerContainers.close();
		dockerImages.close();
		final IDockerConnection connection = configureTCPConnection("Test");
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		// when
		openConnectionEditionWizard("Test");
		bot.text(2).setText("https://foo.bar:1234");
		getFinishButton().click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		// then
		final SWTBotTreeItem updatedConnectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(updatedConnectionTreeItem).isNotNull();
		assertThat(updatedConnectionTreeItem.getText()).contains("https://foo.bar:1234");
		// list of containers and images should have been refreshed
		Mockito.verify(connection, Mockito.times(0)).getContainers(true);
		Mockito.verify(connection, Mockito.times(0)).getImages(true);
	}

	@SuppressWarnings("unchecked")
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
		Mockito.verify(connectionStorageManager).saveConnections(Matchers.anyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSaveConnectionWhenUnixSocketConnectionSettingsChanged() throws IOException {
		// given
		final IDockerConnection connection = configureUnixSocketConnection("Test");
		final IDockerConnectionStorageManager connectionStorageManager = MockDockerConnectionStorageManagerFactory
				.providing(connection);
		DockerConnectionManagerUtils.configureConnectionManager(connectionStorageManager);
		final SWTBotTreeItem connectionTreeItem = SWTUtils.getTreeItem(dockerExplorer.bot(), "Test");
		assertThat(connectionTreeItem).isNotNull();
		final File tmpDockerSocketFile = folder.newFile("docker.sock");
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
		Mockito.verify(connectionStorageManager).saveConnections(Matchers.anyList());
	}

	@SuppressWarnings("unchecked")
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
		Mockito.verify(connectionStorageManager).saveConnections(Matchers.anyList());
	}

	@SuppressWarnings("unchecked")
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
		Mockito.verify(connectionStorageManager).saveConnections(Matchers.anyList());
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
		final File tmpDockerSocketFile = folder.newFile("docker.sock");
		// when
		openConnectionEditionWizard("Test");
		bot.text(1).setText(tmpDockerSocketFile.getAbsolutePath());
		getFinishButton().click();
		// then
		final IDockerConnection foundConnection = DockerConnectionManager.getInstance().findConnection("Test");
		assertThat(foundConnection).isNotNull();
		assertThat(foundConnection.getSettings()).isNotNull()
				.isEqualTo(new UnixSocketConnectionSettings(tmpDockerSocketFile.getAbsolutePath()));
		assertThat(foundConnection.getState()).isEqualTo(EnumDockerConnectionState.UNKNOWN);
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
		// when
		openConnectionEditionWizard("Test");
		bot.text(2).setText("https://foo.bar:1234");
		getFinishButton().click();
		// then
		final IDockerConnection foundConnection = DockerConnectionManager.getInstance().findConnection("Test");
		assertThat(foundConnection).isNotNull();
		assertThat(foundConnection.getSettings()).isNotNull()
				.isEqualTo(new TCPConnectionSettings("https://foo.bar:1234", PATH_TO_CERTS));
		assertThat(foundConnection.getState()).isEqualTo(EnumDockerConnectionState.UNKNOWN);
	}

}
