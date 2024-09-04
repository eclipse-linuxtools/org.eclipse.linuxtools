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

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.TimeUnit;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionState;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerProgressHandler;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountInfo;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockRegistryAccountManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseShellRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.MenuAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.exceptions.DockerException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Testing the {@link ImagePull} wizard
 */
public class ImagePullSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerExplorerViewBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);
	private RegistryAccountStorageManager defaultRegistryAccountStorageManager;
	private DockerClient client;

	@Before
	public void lookupDockerExplorerView() {
		this.dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		this.dockerExplorerViewBot.show();
		this.dockerExplorerViewBot.setFocus();
		this.defaultRegistryAccountStorageManager = RegistryAccountManager.getInstance().getStorageManager();
	}

	@Before
	public void setupDockerClient() {
		this.client = MockDockerClientFactory.image(MockImageFactory.name("bar:latest", "foo/bar:latest").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
	}

	@After
	public void restoreRegistryAccountStorageManager() {
		RegistryAccountManager.getInstance().setStorageManager(this.defaultRegistryAccountStorageManager);
	}

	private void openPullWizard() {
		// when opening the "Push Image..." wizard
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images").select();
		dockerExplorerViewBot.bot().tree().contextMenu("Pull...").click();
	}

	@Ignore
	@Test
	public void shoulDisableSearchButtonWhenNoRegistrySelected() {
		// given
		openPullWizard();
		// when
		bot.text(0).setText("jboss/wildfly:latest");
		// then
		assertFalse(bot.button("Finish").isEnabled());
	}

	@Test
	public void shouldNotAllowFinishWhenImageNameIsMissing() {
		// given
		openPullWizard();
		// when no data is input for the images name
		// then
		assertFalse(bot.button("Finish").isEnabled());
	}

	@Test
	public void shouldPullImageFromDockerHub() throws DockerException, InterruptedException {
		// given
		openPullWizard();
		// when
		bot.text(0).setText("jboss/wildfly:latest");
		bot.button("Finish").click();
		// then
		Mockito.verify(client, Mockito.times(1)).pull(ArgumentMatchers.eq("jboss/wildfly:latest"),
				ArgumentMatchers.any(DockerProgressHandler.class));
	}

	@Test
	public void shouldPullLatestImageFromDockerHub() throws DockerException, InterruptedException {
		// given
		openPullWizard();
		// when
		bot.text(0).setText("jboss/wildfly");
		bot.button("Finish").click();
		// then
		Mockito.verify(client, Mockito.times(1)).pull(ArgumentMatchers.eq("jboss/wildfly:latest"),
				ArgumentMatchers.any(DockerProgressHandler.class));
	}

	@Test
	public void shouldPullImageFromOtherRegistry() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory
				.registryAccount(
						new RegistryAccountInfo("http://foo.com", "foo", null, "secret".toCharArray(), false))
				.build();
		openPullWizard();
		// when selecting other registry
		bot.comboBox(0).setSelection("foo@http://foo.com");
		// when
		bot.text(0).setText("jboss/wildfly:latest");
		bot.button("Finish").click();
		// then
		Mockito.verify(client, Mockito.times(1)).pull(ArgumentMatchers.eq("foo.com/jboss/wildfly:latest"),
				ArgumentMatchers.any(DockerProgressHandler.class));
	}

	@Test
	public void shouldDisablePullCommandWhenConnectionStateIsUnknown() {
		// given
		this.client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withState(EnumDockerConnectionState.UNKNOWN);
		assertThat(dockerConnection.getState()).isEqualTo(EnumDockerConnectionState.UNKNOWN);
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		// when opening the "Push Image..." wizard
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test").select();
		// then
		MenuAssertion.assertThat(dockerExplorerViewBot.bot().tree().contextMenu("Pull...")).isNotEnabled();
	}

	@Test
	public void shouldDisablePullCommandWhenConnectionIsClosed() {
		// given
		this.client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withState(EnumDockerConnectionState.CLOSED);
		assertThat(dockerConnection.getState()).isEqualTo(EnumDockerConnectionState.CLOSED);
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		// when opening the "Push Image..." wizard
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test").select();
		// then
		MenuAssertion.assertThat(dockerExplorerViewBot.bot().tree().contextMenu("Pull...")).isNotEnabled();
	}

	@Test
	public void shouldEnablePullCommandWhenConnectionIsEstablished() {
		// given
		this.client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		// when opening the "Push Image..." wizard
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test").select();
		// then
		SWTUtils.wait(1, TimeUnit.SECONDS);
		assertThat(dockerConnection.getState()).isEqualTo(EnumDockerConnectionState.ESTABLISHED);
		MenuAssertion.assertThat(dockerExplorerViewBot.bot().tree().contextMenu("Pull...")).isEnabled();
	}

	@Test
	public void shouldEnablePullCommandWhenConnectionIsEstablishedAndExpanded() {
		// given
		this.client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		// when opening the "Push Image..." wizard
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images").select();
		// then
		assertThat(dockerConnection.getState()).isEqualTo(EnumDockerConnectionState.ESTABLISHED);
		MenuAssertion.assertThat(dockerExplorerViewBot.bot().tree().contextMenu("Pull...")).isEnabled();
	}
}
