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
import static org.junit.jupiter.api.Assertions.assertFalse;

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
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
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

	@RegisterExtension
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Order(2)
	@RegisterExtension
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Order(1)
	@RegisterExtension
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);
	private RegistryAccountStorageManager defaultRegistryAccountStorageManager;
	private DockerClient client;
	/** bot scoped to the "Pull Image" wizard shell, see {@link #openPullWizard()}. */
	private SWTBot wizardBot;

	@BeforeEach
	public void lookupDockerExplorerView() {
		this.dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		this.dockerExplorerViewBot.show();
		this.dockerExplorerViewBot.setFocus();
		this.defaultRegistryAccountStorageManager = RegistryAccountManager.getInstance().getStorageManager();
	}

	@BeforeEach
	public void setupDockerClient() {
		this.client = MockDockerClientFactory.images(MockImageFactory.of("", "", "bar:latest", "foo/bar:latest"))
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
	}

	@AfterEach
	public void restoreRegistryAccountStorageManager() {
		RegistryAccountManager.getInstance().setStorageManager(this.defaultRegistryAccountStorageManager);
	}

	/**
	 * Opens the "Pull Image..." wizard from the Docker Explorer view and scopes
	 * {@link #wizardBot} to its shell, so that widget lookups do not depend on
	 * which shell happens to be active.
	 */
	private void openPullWizard() {
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images").select();
		dockerExplorerViewBot.bot().tree().contextMenu("Pull...").click();
		this.wizardBot = SWTUtils.waitForShell(bot, WizardMessages.getString("ImagePull.title")); //$NON-NLS-1$
	}

	private SWTBotText imageNameText() {
		return wizardBot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")); //$NON-NLS-1$
	}

	/**
	 * Verifies that the given image was pulled through the mocked client. The
	 * pull runs in a background job once the wizard finishes, so this waits
	 * for it.
	 */
	private void verifyImagePulled(final String imageName) throws DockerException, InterruptedException {
		Mockito.verify(client, Mockito.timeout(TimeUnit.SECONDS.toMillis(10)).times(1)).pull(
				ArgumentMatchers.eq(imageName), ArgumentMatchers.any(DockerProgressHandler.class));
	}

	@Disabled
	@Test
	public void shoulDisableSearchButtonWhenNoRegistrySelected() {
		// given
		openPullWizard();
		// when
		imageNameText().setText("jboss/wildfly:latest");
		// then
		assertFalse(wizardBot.button("Finish").isEnabled());
	}

	@Test
	public void shouldNotAllowFinishWhenImageNameIsMissing() {
		// given
		openPullWizard();
		// when no data is input for the images name
		// then
		assertFalse(wizardBot.button("Finish").isEnabled());
	}

	@Test
	public void shouldPullImageFromDockerHub() throws DockerException, InterruptedException {
		// given
		openPullWizard();
		// when
		imageNameText().setText("jboss/wildfly:latest");
		wizardBot.button("Finish").click();
		// then
		verifyImagePulled("jboss/wildfly:latest");
	}

	@Test
	public void shouldPullLatestImageFromDockerHub() throws DockerException, InterruptedException {
		// given
		openPullWizard();
		// when
		imageNameText().setText("jboss/wildfly");
		wizardBot.button("Finish").click();
		// then
		verifyImagePulled("jboss/wildfly:latest");
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
		wizardBot.comboBoxWithLabel(WizardMessages.getString("ImagePullPushPage.registry.account.label")) //$NON-NLS-1$
				.setSelection("foo@http://foo.com");
		// when
		imageNameText().setText("jboss/wildfly:latest");
		wizardBot.button("Finish").click();
		// then
		verifyImagePulled("foo.com/jboss/wildfly:latest");
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
