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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageSearchResultFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseShellRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link ImageSearch} wizard.
 */
public class ImageSearchSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private DockerExplorerView dockerExplorerView;
	private SWTBotView dockerExplorerViewBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);

	@Before
	public void lookupDockerExplorerView() {
		dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		dockerExplorerViewBot.show();
		dockerExplorerViewBot.setFocus();
	}

	@Test
	public void shouldTriggerSearchIfTermWasGiven() {
		// given
		final DockerClient client = MockDockerClientFactory.onSearch("foo", MockImageSearchResultFactory.name("foo").build())
				.build();
		// when opening the pull wizard...
		openPullWizard(client);
		// ... and specifying a term...
		bot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).setText("foo");
		// ... and then opening the search wizard
		openSearchWizard();
		// then the search should have been triggered and results should be available
		assertThat(bot.table().rowCount()).isEqualTo(1);
	}

	@Test
	public void shouldNotTriggerSearchIfNoTermWasGiven() {
		// given
		final DockerClient client = MockDockerClientFactory.onSearch("foo", MockImageSearchResultFactory.name("foo").build())
				.build();
		// when opening the pull wizard...
		openPullWizard(client);
		// ... and directly opening the search wizard
		openSearchWizard();
		// then the search should have been triggered and results should be available
		assertThat(bot.table().rowCount()).isEqualTo(0);
	}

	@Test
	public void shouldReduceSearchResultsToExactGivenTerm() {
		// given
		final DockerClient client = MockDockerClientFactory
				.onSearch("foo/bar", MockImageSearchResultFactory.name("foo/bar").build(),
						MockImageSearchResultFactory.name("other/bar").build())
				.build();
		// when opening the pull wizard...
		openPullWizard(client);
		// ... and specifying a term...
		bot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).setText("foo/bar");
		// ... and then opening the search wizard
		openSearchWizard();
		// then the search should have been triggered and a single result should be
		// available
		assertThat(bot.table().rowCount()).isEqualTo(1);
	}

	@Test
	public void shouldShowAllSearchResultsForGivenTerm() {
		// given
		final DockerClient client = MockDockerClientFactory
				.onSearch("bar", MockImageSearchResultFactory.name("foo/bar").build(),
						MockImageSearchResultFactory.name("other/bar").build())
				.build();
		// when opening the pull wizard...
		openPullWizard(client);
		// ... and specifying a term...
		bot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).setText("bar");
		// ... and then opening the search wizard
		openSearchWizard();
		// then the search should have been triggered and both results should be
		// available
		assertThat(bot.table().rowCount()).isEqualTo(2);
	}

	private void openPullWizard(final DockerClient client) {
		// given
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// expand the 'Images' node
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		// when opening the "Pull..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		imagesTreeItem.select();
		dockerExplorerViewTreeBot.contextMenu("Pull...").click();
	}

	private void openSearchWizard() {
		// click on the "Search..." button
		bot.button(WizardMessages.getString("ImagePull.search.label")).click();
	}

	@Test
	public void shouldAllowForDefaultLatestTag() {
		// given
		final DockerClient client = MockDockerClientFactory
				.onSearch("foo", MockImageSearchResultFactory.name("foo").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when opening the "Pull..." wizard
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images").select();
		dockerExplorerViewBot.bot().tree().contextMenu("Pull...").click();

		// when specifying a term
		bot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).setText("foo");

		// when clicking on the "Search..." button
		bot.button(WizardMessages.getString("ImagePull.search.label")).click();

		// then the search should have been triggered and results should be
		// available
		assertThat(bot.table().rowCount()).isEqualTo(1);
		assertThat(bot.button("Next >").isEnabled()).isTrue();
		assertThat(bot.button("Finish").isEnabled()).isTrue();
		bot.button("Finish").click();

		// when back to Pull wizard, the Image name field should be filled
		assertThat(bot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).getText())
				.isEqualTo("foo:latest");
	}

}
