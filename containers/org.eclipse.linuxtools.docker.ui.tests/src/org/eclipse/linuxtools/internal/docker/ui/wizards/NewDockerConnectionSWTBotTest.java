/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CheckBoxAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWizardRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.RadioAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTBotTreeItemAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TextAssertion;
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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing the {@link NewDockerConnection} {@link Wizard}
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class NewDockerConnectionSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotToolbarButton addConnectionButton;
	private SWTBotView dockerExplorerViewBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule();

	@Rule
	public CloseWizardRule closeWizard = new CloseWizardRule();

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

	@Test
	public void shouldShowCustomUnixSocketSettingsWhenNoConnectionAvailable() {
		// given
		MockDockerConnectionSettingsFinder.noDockerConnectionAvailable();
		// when
		addConnectionButton.click();
		// then
		// Empty Connection name
		TextAssertion.assertThat(bot.text(0)).isEnabled().isEmpty();
		// "Use custom connection settings" should be enabled and checked
		CheckBoxAssertion.assertThat(bot.checkBox(0)).isEnabled().isChecked();
		// "Unix socket" radio should be enabled and selected
		RadioAssertion.assertThat(bot.radio(0)).isEnabled().isSelected();
		// "Unix socket path" text should be enabled and empty
		TextAssertion.assertThat(bot.text(1)).isEnabled().isEmpty();
		// "TCP Connection" radio should be enabled but unselected
		RadioAssertion.assertThat(bot.radio(1)).isEnabled().isNotSelected();
		// "URI" should be disabled but empty
		TextAssertion.assertThat(bot.text(2)).isNotEnabled().isEmpty();
		// "Enable Auth" checkbox should be unselected and disabled
		CheckBoxAssertion.assertThat(bot.checkBox(1)).isNotEnabled().isNotChecked();
		// "Path" for certs should be disabled and empty
		TextAssertion.assertThat(bot.text(3)).isNotEnabled().isEmpty();
	}

	@Test
	public void shouldShowDefaultUnixSocketConnectionSettingsWithValidConnectionAvailable() throws IOException {
		// given
		MockDockerConnectionSettingsFinder.validUnixSocketConnectionAvailable();
		// when
		addConnectionButton.click();
		// then
		// Connection name
		TextAssertion.assertThat(bot.text(0)).isEnabled().textEquals("mock");
		// "Use custom connection settings" should be enabled but unchecked
		CheckBoxAssertion.assertThat(bot.checkBox(0)).isEnabled().isNotChecked();
		// "Unix socket" radio should be disabled and selected
		RadioAssertion.assertThat(bot.radio(0)).isNotEnabled().isSelected();
		// "Unix socket path" text should be disabled and not empty
		TextAssertion.assertThat(bot.text(1)).isNotEnabled().textEquals("unix:///var/run/docker.sock");
		// "TCP Connection" radio should be unselected and disabled
		RadioAssertion.assertThat(bot.radio(1)).isNotEnabled().isNotSelected();
		// "URI" should be disabled and empty
		TextAssertion.assertThat(bot.text(2)).isNotEnabled().isEmpty();
		// "Enable Auth" checkbox should be unselected and disabled
		CheckBoxAssertion.assertThat(bot.checkBox(1)).isNotEnabled().isNotChecked();
		// "Path" for certs should be disabled but not empty
		TextAssertion.assertThat(bot.text(3)).isNotEnabled().isEmpty();
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
		TextAssertion.assertThat(bot.text(0)).isEnabled().textEquals("mock");
		// "Use custom connection settings" should be enabled but unchecked
		CheckBoxAssertion.assertThat(bot.checkBox(0)).isEnabled().isNotChecked();
		// "Unix socket" radio should be disabled and unselected
		RadioAssertion.assertThat(bot.radio(0)).isNotEnabled().isNotSelected();
		// "Unix socket path" text should be disabled and not empty
		TextAssertion.assertThat(bot.text(1)).isNotEnabled().isEmpty();
		// "TCP Connection" radio should be selected but diabled
		RadioAssertion.assertThat(bot.radio(1)).isNotEnabled().isSelected();
		// "URI" should be disabled but not empty
		TextAssertion.assertThat(bot.text(2)).isNotEnabled().textEquals("https://1.2.3.4:1234");
		// "Enable Auth" checkbox should be selected but disabled
		CheckBoxAssertion.assertThat(bot.checkBox(1)).isNotEnabled().isChecked();
		// "Path" for certs should be disabled but not empty
		TextAssertion.assertThat(bot.text(3)).isNotEnabled().textEquals("/path/to/certs");
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
	public void shouldPopulateConnectionWithClipboard() {
		final int [] DND_TYPE = {DND.SELECTION_CLIPBOARD, DND.CLIPBOARD};
		for (int i = 0; i < DND_TYPE.length; i++) {
			// Clear the clipboards
			Display.getDefault().syncExec(() -> {
				Clipboard clip = new Clipboard(Display.getCurrent());
				clip.clearContents(DND.CLIPBOARD);
				clip.clearContents(DND.SELECTION_CLIPBOARD);
			});

			// given
			final int DND_VALUE = DND_TYPE[i];
			final String[] connectionData = new String[] {
					"DOCKER_HOST=https://1.2.3.4:1234 DOCKER_CERT_PATH=/path/to/certs DOCKER_TLS_VERIFY=1" };
			Display.getDefault().syncExec(() -> {
				Clipboard clip = new Clipboard(Display.getCurrent());
				clip.setContents(connectionData, new Transfer[] { TextTransfer.getInstance() },
						DND_VALUE);
			});
			// when
			addConnectionButton.click();
			// then
			// Connection name
			TextAssertion.assertThat(bot.text(0)).isEnabled().isEmpty();
			// "Use custom connection settings" should be enabled and checked
			CheckBoxAssertion.assertThat(bot.checkBox(0)).isEnabled().isChecked();
			// "Unix socket" radio should be enabled and unselected
			RadioAssertion.assertThat(bot.radio(0)).isEnabled().isNotSelected();
			// "Unix socket path" text should be disabled and empty
			TextAssertion.assertThat(bot.text(1)).isNotEnabled().isEmpty();
			// "TCP Connection" radio should be enabled and selected
			RadioAssertion.assertThat(bot.radio(1)).isEnabled().isSelected();
			// "URI" should be enabled and not empty
			TextAssertion.assertThat(bot.text(2)).isEnabled().textEquals("https://1.2.3.4:1234");
			// "Enable Auth" checkbox should be enabled and selected
			CheckBoxAssertion.assertThat(bot.checkBox(1)).isEnabled().isChecked();
			// "Path" for certs should be enabled and not empty
			TextAssertion.assertThat(bot.text(3)).isEnabled().textEquals("/path/to/certs");

			// Close wizard
			bot.button("Cancel").click();
		}

	}

}
