/*******************************************************************************
 * Copyright (c) 2015, 2020 Red Hat Inc.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.eclipse.swtbot.swt.finder.junit5.SWTBotJunit5Extension;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mandas.docker.client.DockerClient;

/**
 * Testing the {@link NewDockerConnection} {@link Wizard}
 */
@ExtendWith(SWTBotJunit5Extension.class)
public class NewDockerConnectionSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotToolbarButton addConnectionButton;
	private SWTBotView dockerExplorerViewBot;

	@RegisterExtension
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@RegisterExtension
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);

	@BeforeEach
	public void lookupDockerExplorerView() throws Exception {
		bot.getDisplay().asyncExec(() -> {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(DockerExplorerView.VIEW_ID);
			} catch (Exception e) {
				e.printStackTrace();
				Assertions.fail("Failed to open Docker Explorer view: " + e.getMessage());
			}
		});
		dockerExplorerViewBot = bot.viewById(DockerExplorerView.VIEW_ID);
		dockerExplorerViewBot.show();
		bot.views().stream().filter(v -> v.getReference().getId().equals(DockerContainersView.VIEW_ID)
				|| v.getReference().getId().equals(DockerImagesView.VIEW_ID)).forEach(SWTBotView::close);
		dockerExplorerViewBot.setFocus();
		this.addConnectionButton = dockerExplorerViewBot.toolbarButton("&Add Connection");
	}

	@BeforeEach
	public void clearClipboards() {
		// Clear all clipboards.
		//
		// clearContents() only takes effect while this application still owns the
		// selection, so on its own it lets content set by an earlier test survive
		// into the next one. That matters because the connection wizard prefills
		// itself from the clipboard when it holds a DOCKER_HOST payload, and then
		// never consults the default connection settings at all. Overwriting with
		// a harmless placeholder is the part that actually sticks; an empty string
		// is not accepted by TextTransfer.
		Display.getDefault().syncExec(() -> {
			final Clipboard clip = new Clipboard(Display.getCurrent());
			try {
				clip.setContents(new Object[] { "-" }, new Transfer[] { TextTransfer.getInstance() }, DND.CLIPBOARD);
				clip.setContents(new Object[] { "-" }, new Transfer[] { TextTransfer.getInstance() },
						DND.SELECTION_CLIPBOARD);
				clip.clearContents(DND.CLIPBOARD);
				clip.clearContents(DND.SELECTION_CLIPBOARD);
			} finally {
				clip.dispose();
			}
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

	/**
	 * Opens the "New Docker Connection" wizard and makes sure it is the active
	 * shell: {@code bot.text(int)} and friends resolve against whichever shell
	 * is active, so without this they silently pick up the widgets of the
	 * workbench window instead of the ones in the wizard.
	 */
	private void openNewConnectionWizard() {
		addConnectionButton.click();
		final SWTBotShell wizardShell = bot.shell(WizardMessages.getString("NewDockerConnection.title")); //$NON-NLS-1$
		bot.waitUntil(Conditions.shellIsActive(WizardMessages.getString("NewDockerConnection.title"))); //$NON-NLS-1$
		wizardShell.activate();
	}

	@Test
	public void shouldShowCustomUnixSocketSettingsWhenNoConnectionAvailable() {
		// given
		MockDockerConnectionSettingsFinder.noDockerConnectionAvailable();
		// when
		openNewConnectionWizard();
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
		openNewConnectionWizard();
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
		openNewConnectionWizard();
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
		openNewConnectionWizard();
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
		openNewConnectionWizard();
		// when changing connection name
		bot.text(0).setText("foo");
		// then the wizard should not allow for completion because a connection
		// with the connection settings already exists.
		assertFalse(bot.button("Finish").isEnabled());
	}

	@Test
	public void shouldNotAllowNewConnectionWithDifferentNameAndSameTCPSettings() {
		// given
		MockDockerConnectionSettingsFinder.validTCPConnectionAvailable("Mock", "https://foo:1234", null);
		// add an existing connection based on the settings above
		configureTCPConnection("Mock", "https://foo:1234");
		// when open wizard
		openNewConnectionWizard();
		// when changing connection name
		bot.text(0).setText("foo");
		// then the wizard should not allow for completion because a connection
		// with the connection settings already exists.
		assertFalse(bot.button("Finish").isEnabled());
	}

	@Test
	public void shouldAllowNewConnectionWithDifferentNameAndUnixSettings() throws IOException {
		// given
		final String dockerSocketTmpPath = File.createTempFile("docker", ".sock").getAbsolutePath();
		configureUnixSocketConnection("Bar", dockerSocketTmpPath);
		MockDockerConnectionSettingsFinder.validUnixSocketConnectionAvailable("Mock", "unix://" + dockerSocketTmpPath);
		final String otherDockerSocketTmpPath = File.createTempFile("docker", ".sock").getAbsolutePath();
		// when open wizard
		openNewConnectionWizard();
		// when changing connection name
		bot.text(0).setText("foo");
		bot.checkBox(0).select();
		bot.text(1).setText(otherDockerSocketTmpPath);
		// then the wizard should allow for completion because no connection
		// with the same connection settings exists.
		assertTrue(bot.button("Finish").isEnabled());
	}

	@Test
	public void shouldAllowNewConnectionWithDifferentNameAndTCPSettings() {
		// given
		MockDockerConnectionSettingsFinder.validTCPConnectionAvailable("Mock", "https://foo:1234", null);
		// add an existing connection based on the settings above
		configureTCPConnection("Mock", "https://foo");
		// when open wizard
		openNewConnectionWizard();
		// when changing connection name
		bot.text(0).setText("foo");
		bot.checkBox(0).select();
		bot.text(2).setText("https://bar:1234");
		// then the wizard should allow for completion because no connection
		// with the same connection settings exists.
		assertTrue(bot.button("Finish").isEnabled());
	}

	@Test
	public void shouldPopulateConnectionWithClipboard() {
		verifyPopulateConnectionWithClipboard(DND.CLIPBOARD);

	}

	@Test
	public void shouldPopulateConnectionWithSelectionClipboard() {
		// SELECTION_CLIPBOARD does not seem to be supported on platforms other
		// than Linux (GTK/Motif)
		Assumptions.assumeTrue(SystemUtils.isLinux(), "This test only runs on Linux");
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
		openNewConnectionWizard();
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
