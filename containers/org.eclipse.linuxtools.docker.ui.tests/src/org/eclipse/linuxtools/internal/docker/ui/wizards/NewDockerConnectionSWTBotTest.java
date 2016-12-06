/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.SystemUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ButtonAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CheckBoxAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseShellRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.RadioAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTBotTreeItemAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TextAssertions;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerContainersView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerImagesView;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link NewDockerConnection} {@link Wizard}
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class NewDockerConnectionSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotToolbarButton addConnectionButton;
	private SWTBotView dockerExplorerViewBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);

	@Before
	public void lookupDockerExplorerView() throws Exception {
		SWTUtils.asyncExec(() -> {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(DockerExplorerView.VIEW_ID);
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
			}
		});
		dockerExplorerViewBot = bot.viewById(DockerExplorerView.VIEW_ID);
		dockerExplorerViewBot.show();
		bot.views().stream().filter(v -> v.getReference().getId().equals(DockerContainersView.VIEW_ID)
				|| v.getReference().getId().equals(DockerImagesView.VIEW_ID)).forEach(v -> v.close());
		dockerExplorerViewBot.setFocus();
		this.addConnectionButton = dockerExplorerViewBot.toolbarButton("&Add Connection");
	}

	@Before
	public void clearClipboards() {
		// Clear all clipboards
		Display.getDefault().syncExec(() -> {
			Clipboard clip = new Clipboard(Display.getCurrent());
			clip.clearContents(DND.CLIPBOARD);
			clip.clearContents(DND.SELECTION_CLIPBOARD);
		});
	}

	private IDockerConnection configureUnixSocketConnection(final String connectionName, final String pathToSocket) {
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(connectionName, client)
				.withUnixSocketConnectionSettings(pathToSocket);
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		return dockerConnection;
	}

	private IDockerConnection configureTCPConnection(final String connectionName, final String host) {
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(connectionName, client)
				.withTCPConnectionSettings(host, null);
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		return dockerConnection;
	}

	@Test
	public void shouldShowCustomUnixSocketSettingsWhenNoConnectionAvailable() {
		// given
		MockDockerConnectionSettingsFinder.noDockerConnectionAvailable();
		// when
		addConnectionButton.click();
		// then
		// Empty Connection name
		TextAssertions.assertThat(bot.text(0)).isEnabled().isEmpty();
		// "Use custom connection settings" should be enabled and checked
		CheckBoxAssertions.assertThat(bot.checkBox(0)).isEnabled().isChecked();
		// "Unix socket" radio should be enabled and selected
		RadioAssertion.assertThat(bot.radio(0)).isEnabled().isSelected();
		// "Unix socket path" text should be enabled and empty
		TextAssertions.assertThat(bot.text(1)).isEnabled().isEmpty();
		// "TCP Connection" radio should be enabled but unselected
		RadioAssertion.assertThat(bot.radio(1)).isEnabled().isNotSelected();
		// "URI" should be disabled but empty
		TextAssertions.assertThat(bot.text(2)).isNotEnabled().isEmpty();
		// "Enable Auth" checkbox should be unselected and disabled
		CheckBoxAssertions.assertThat(bot.checkBox(1)).isNotEnabled().isNotChecked();
		// "Path" for certs should be disabled and empty
		TextAssertions.assertThat(bot.text(3)).isNotEnabled().isEmpty();
	}

	@Test
	public void shouldShowDefaultUnixSocketConnectionSettingsWithValidConnectionAvailable() {
		// given
		MockDockerConnectionSettingsFinder.validUnixSocketConnectionAvailable();
		// when
		addConnectionButton.click();
		// then
		// Connection name
		TextAssertions.assertThat(bot.text(0)).isEnabled().textEquals("mock");
		// "Use custom connection settings" should be enabled but unchecked
		CheckBoxAssertions.assertThat(bot.checkBox(0)).isEnabled().isNotChecked();
		// "Unix socket" radio should be disabled and selected
		RadioAssertion.assertThat(bot.radio(0)).isNotEnabled().isSelected();
		// "Unix socket path" text should be disabled and not empty
		TextAssertions.assertThat(bot.text(1)).isNotEnabled().textEquals("unix:///var/run/docker.sock");
		// "TCP Connection" radio should be unselected and disabled
		RadioAssertion.assertThat(bot.radio(1)).isNotEnabled().isNotSelected();
		// "URI" should be disabled and empty
		TextAssertions.assertThat(bot.text(2)).isNotEnabled().isEmpty();
		// "Enable Auth" checkbox should be unselected and disabled
		CheckBoxAssertions.assertThat(bot.checkBox(1)).isNotEnabled().isNotChecked();
		// "Path" for certs should be disabled but not empty
		TextAssertions.assertThat(bot.text(3)).isNotEnabled().isEmpty();
	}

	@Test
	public void shouldShowDefaultTCPSettingsWithValidConnectionAvailable() {
		// given
		MockDockerConnectionSettingsFinder.validTCPConnectionAvailable();
		// when
		addConnectionButton.click();
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("NewDockerConnection.title"))); //$NON-NLS-1$
		// then
		// Connection name
		TextAssertions.assertThat(bot.text(0)).isEnabled().textEquals("mock");
		// "Use custom connection settings" should be enabled but unchecked
		CheckBoxAssertions.assertThat(bot.checkBox(0)).isEnabled().isNotChecked();
		// "Unix socket" radio should be disabled and unselected
		RadioAssertion.assertThat(bot.radio(0)).isNotEnabled().isNotSelected();
		// "Unix socket path" text should be disabled and not empty
		TextAssertions.assertThat(bot.text(1)).isNotEnabled().isEmpty();
		// "TCP Connection" radio should be selected but diabled
		RadioAssertion.assertThat(bot.radio(1)).isNotEnabled().isSelected();
		// "URI" should be disabled but not empty
		TextAssertions.assertThat(bot.text(2)).isNotEnabled().textEquals("https://1.2.3.4:1234");
		// "Enable Auth" checkbox should be selected but disabled
		CheckBoxAssertions.assertThat(bot.checkBox(1)).isNotEnabled().isChecked();
		// "Path" for certs should be disabled but not empty
		TextAssertions.assertThat(bot.text(3)).isNotEnabled().textEquals("/path/to/certs");
	}

	@Test
	public void shouldAddConnectionToDockerExplorerView() throws IOException {
		// given
		final File dockerSocketTmpFile = File.createTempFile("docker", ".sock");
		MockDockerConnectionSettingsFinder.validUnixSocketConnectionAvailable("Mock",
				"unix://" + dockerSocketTmpFile.getAbsolutePath());
		// when open wizard
		addConnectionButton.click();
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("NewDockerConnection.title"))); //$NON-NLS-1$
		// when click on "OK"
		bot.button("Finish").click();
		// then the Docker Explorer view should have a connection named "Mock"
		SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(dockerExplorerViewBot.bot().tree(), "Mock"))
				.isNotNull();
	}

	@Test
	public void shouldNotAllowNewConnectionWithDifferentNameAndSameUnixSocketSettings() throws IOException {
		// given
		final String dockerSocketTmpPath = File.createTempFile("docker", ".sock").getAbsolutePath();
		MockDockerConnectionSettingsFinder.validUnixSocketConnectionAvailable("Mock", "unix://" + dockerSocketTmpPath);
		// add an existing connection based on the settings above
		configureUnixSocketConnection("Mock", dockerSocketTmpPath);
		// when open wizard
		addConnectionButton.click();
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("NewDockerConnection.title"))); //$NON-NLS-1$
		// when changing connection name
		bot.text(0).setText("foo");
		// then the wizard should not allow for completion because a connection
		// with the connection settings already exists.
		ButtonAssertions.assertThat(bot.button("Finish")).isNotEnabled();
	}

	@Test
	public void shouldNotAllowNewConnectionWithDifferentNameAndSameTCPSettings() {
		// given
		MockDockerConnectionSettingsFinder.validTCPConnectionAvailable("Mock", "https://foo:1234", null);
		// add an existing connection based on the settings above
		configureTCPConnection("Mock", "https://foo:1234");
		// when open wizard
		addConnectionButton.click();
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("NewDockerConnection.title"))); //$NON-NLS-1$
		// when changing connection name
		bot.text(0).setText("foo");
		// then the wizard should not allow for completion because a connection
		// with the connection settings already exists.
		ButtonAssertions.assertThat(bot.button("Finish")).isNotEnabled();
	}

	@Test
	public void shouldAllowNewConnectionWithDifferentNameAndUnixSettings() throws IOException {
		// given
		final String dockerSocketTmpPath = File.createTempFile("docker", ".sock").getAbsolutePath();
		configureUnixSocketConnection("Bar", dockerSocketTmpPath);
		MockDockerConnectionSettingsFinder.validUnixSocketConnectionAvailable("Mock", "unix://" + dockerSocketTmpPath);
		final String otherDockerSocketTmpPath = File.createTempFile("docker", ".sock").getAbsolutePath();
		// when open wizard
		addConnectionButton.click();
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("NewDockerConnection.title"))); //$NON-NLS-1$
		// when changing connection name
		bot.text(0).setText("foo");
		bot.checkBox(0).select();
		bot.text(1).setText(otherDockerSocketTmpPath);
		// then the wizard should not allow for completion because a connection
		// with the connection settings already exists.
		ButtonAssertions.assertThat(bot.button("Finish")).isEnabled();
	}

	@Test
	public void shouldAllowNewConnectionWithDifferentNameAndTCPSettings() {
		// given
		MockDockerConnectionSettingsFinder.validTCPConnectionAvailable("Mock", "https://foo:1234", null);
		// add an existing connection based on the settings above
		configureTCPConnection("Mock", "https://foo");
		// when open wizard
		addConnectionButton.click();
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("NewDockerConnection.title"))); //$NON-NLS-1$
		// when changing connection name
		bot.text(0).setText("foo");
		bot.checkBox(0).select();
		bot.text(2).setText("https://bar:1234");
		// then the wizard should not allow for completion because a connection
		// with the connection settings already exists.
		ButtonAssertions.assertThat(bot.button("Finish")).isEnabled();
	}

	@Test
	public void shouldPopulateConnectionWithClipboard() {
		verifyPopulateConnectionWithClipboard(DND.CLIPBOARD);

	}

	@Test
	public void shouldPopulateConnectionWithSelectionClipboard() {
		// SELECTION_CLIPBOARD does not seem to be supported on platforms other
		// than Linux (GTK/Motif)
		Assume.assumeTrue("This test only runs on Linux", SystemUtils.isLinux());
		verifyPopulateConnectionWithClipboard(DND.SELECTION_CLIPBOARD);
	}

	private void verifyPopulateConnectionWithClipboard(final int clipboardType) {
		// given
		final String[] connectionData = new String[] {
				"DOCKER_HOST=https://1.2.3.4:1234 DOCKER_CERT_PATH=/path/to/certs DOCKER_TLS_VERIFY=1" };
		Display.getDefault().syncExec(() -> {
			Clipboard clip = new Clipboard(Display.getCurrent());
			clip.setContents(connectionData, new Transfer[] { TextTransfer.getInstance() }, clipboardType);
		});
		// when
		addConnectionButton.click();
		// then
		// Connection name
		TextAssertions.assertThat(bot.text(0)).isEnabled().isEmpty();
		// "Use custom connection settings" should be enabled and checked
		CheckBoxAssertions.assertThat(bot.checkBox(0)).isEnabled().isChecked();
		// "Unix socket" radio should be enabled and unselected
		RadioAssertion.assertThat(bot.radio(0)).isEnabled().isNotSelected();
		// "Unix socket path" text should be disabled and empty
		TextAssertions.assertThat(bot.text(1)).isNotEnabled().isEmpty();
		// "TCP Connection" radio should be enabled and selected
		RadioAssertion.assertThat(bot.radio(1)).isEnabled().isSelected();
		// "URI" should be enabled and not empty
		TextAssertions.assertThat(bot.text(2)).isEnabled().textEquals("https://1.2.3.4:1234");
		// "Enable Auth" checkbox should be enabled and selected
		CheckBoxAssertions.assertThat(bot.checkBox(1)).isEnabled().isChecked();
		// "Path" for certs should be enabled and not empty
		TextAssertions.assertThat(bot.text(3)).isEnabled().textEquals("/path/to/certs");

		// Close wizard
		bot.button("Cancel").click();
	}

}
