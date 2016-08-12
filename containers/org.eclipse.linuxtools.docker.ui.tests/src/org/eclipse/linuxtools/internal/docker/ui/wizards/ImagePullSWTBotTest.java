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

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerProgressHandler;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountInfo;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockRegistryAccountManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ButtonAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWizardRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

/**
 * Testing the {@link ImagePull} wizard
 */
public class ImagePullSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private DockerExplorerView dockerExplorerView;
	private SWTBotView dockerExplorerViewBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule();

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule
	public CloseWizardRule closeWizard = new CloseWizardRule();
	private RegistryAccountStorageManager defaultRegistryAccountStorageManager;
	private DockerClient client;

	@Before
	public void lookupDockerExplorerView() {
		this.dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		this.dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
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
		ButtonAssertion.assertThat(bot.button("Finish")).isNotEnabled();
	}

	@Test
	public void shouldNotAllowFinishWhenImageNameIsMissing() {
		// given
		openPullWizard();
		// when no data is input for the image name
		// then
		ButtonAssertion.assertThat(bot.button("Finish")).isNotEnabled();
	}

	@Test
	public void shouldPullImageFromDockerHub() throws DockerException, InterruptedException {
		// given
		openPullWizard();
		// when
		bot.text(0).setText("jboss/wildfly:latest");
		bot.button("Finish").click();
		// then
		Mockito.verify(client, Mockito.times(1)).pull(Mockito.eq("jboss/wildfly:latest"),
				Mockito.any(DockerProgressHandler.class));
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
		Mockito.verify(client, Mockito.times(1)).pull(Mockito.eq("foo.com/jboss/wildfly:latest"),
				Mockito.any(DockerProgressHandler.class));
	}
}
