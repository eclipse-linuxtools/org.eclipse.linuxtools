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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.docker.core.AbstractRegistry;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountInfo;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockRegistryAccountManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWizardRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ComboAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

/**
 * Testing the {@link ImagePush} wizard
 */
public class ImagePushSWTBotTests {

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

	private void openPushWizard() {
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imageTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo/bar");
		// when opening the "Push Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imageTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Push...").click();
	}

	@Test
	public void shouldPrefillWizardWithSelectedImageTag() {
		// given
		MockRegistryAccountManagerFactory
				.registryAccount(new RegistryAccountInfo("http://foo.com", "foo", null, "secret".toCharArray(), false))
				.build();

		// expand the 'Images' node
		openPushWizard();
		// then the registry should be set with the first element available, too
		ComboAssertion.assertThat(bot.comboBox(0)).itemSelected(AbstractRegistry.DOCKERHUB_REGISTRY);
		// and the "Image Name" combo should have a selection
		ComboAssertion.assertThat(bot.comboBox(1)).itemSelected("foo/bar:latest");
	}

	@Test
	public void shouldPushImage() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory
				.registryAccount(new RegistryAccountInfo("http://foo.com", null, null, null, false)).build();
		openPushWizard();
		// when selecting other registry
		bot.comboBox(0).setSelection(1);
		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "foo.com/foo/bar:latest", false);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.times(1)).removeImage("foo.com/foo/bar:latest", false, false);
	}

	@Test
	public void shouldPushImageToDockerHub() throws DockerException, InterruptedException {
		// when
		openPushWizard();
		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.never()).tag(Matchers.anyString(), Matchers.anyString(), Matchers.anyBoolean());
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.never()).removeImage(Matchers.anyString(), Matchers.anyBoolean(),
				Matchers.anyBoolean());
	}

	@Test
	public void shouldPushImageWithForceTagging() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory
				.registryAccount(new RegistryAccountInfo("http://foo.com", "foo", null, "secret".toCharArray(), false))
				.build();
		// when
		openPushWizard();
		// when tagging image with '--force'
		bot.checkBox(0).select();
		// when selecting other registry
		bot.comboBox(0).setSelection(1);
		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "foo.com/foo/bar:latest", true);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.times(1)).removeImage("foo.com/foo/bar:latest", false, false);
	}

	@Test
	public void shouldPushImageAndKeepTaggedImage() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory
				.registryAccount(new RegistryAccountInfo("http://foo.com", "foo", null, "secret".toCharArray(), false))
				.build();
		openPushWizard();
		// when selecting other registry
		bot.comboBox(0).setSelection(1);
		// when keeping tagged image
		bot.checkBox(1).select();
		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "foo.com/foo/bar:latest", false);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.never()).removeImage(Matchers.anyString(), Matchers.anyBoolean(),
				Matchers.anyBoolean());
	}

	@Test
	public void shouldPushImageWithNewTagToDockerHub() throws DockerException, InterruptedException {
		// given
		openPushWizard();
		// when providing a new name to the image
		bot.comboBox(1).setText("another/name");
		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "another/name:latest", false);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.times(1)).removeImage(Matchers.anyString(), Matchers.anyBoolean(),
				Matchers.anyBoolean());
	}

	@Test
	public void shouldPushImageWithNewTagToAnotherRepo() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory
				.registryAccount(new RegistryAccountInfo("http://foo.com", "foo", null, "secret".toCharArray(), false))
				.build();
		openPushWizard();
		// when selecting other registry
		bot.comboBox(0).setSelection(1);
		// when providing a new name to the image
		bot.comboBox(1).setText("another/name");
		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "foo.com/another/name:latest", false);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.times(1)).removeImage(Matchers.anyString(), Matchers.anyBoolean(),
				Matchers.anyBoolean());
	}

	@Test
	public void shouldAddRegistry() {
		// given
		final String registryAddress = "https://foo.com";
		openPushWizard();
		// when click on the "Add..." link
		bot.link(0).click(WizardMessages.getString("ImagePullPushPage.add.label"));
		// fill the registry settings
		bot.text(0).setText(registryAddress);
		// finish
		bot.button("OK").click();
		// wait for the model updates to complete
		SWTUtils.wait(1, TimeUnit.SECONDS);
		assertThat(bot.comboBox(0).getText()).isEqualTo(registryAddress);
	}

}
