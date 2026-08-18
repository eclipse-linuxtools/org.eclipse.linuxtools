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
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;

import org.mandas.docker.client.DockerClient;

/**
 * Testing the {@link ImageSearch} wizard.
 */
public class ImageSearchSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerExplorerViewBot;
	/** bot scoped to the "Pull Image" wizard shell, see {@link #openPullWizard(DockerClient)}. */
	private SWTBot pullWizardBot;
	/** the "Search" wizard shell, see {@link #openSearchWizard()}. */
	private SWTBotShell searchWizardShell;
	/** bot scoped to the "Search" wizard shell, see {@link #openSearchWizard()}. */
	private SWTBot searchWizardBot;

	@RegisterExtension
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Order(2)
	@RegisterExtension
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Order(1)
	@RegisterExtension
	public CloseShellRule closeShell = new CloseShellRule(IDialogConstants.CANCEL_LABEL);

	@BeforeEach
	public void lookupDockerExplorerView() {
		dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
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
		pullWizardBot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).setText("foo");
		// ... and then opening the search wizard
		openSearchWizard();
		// then the search should have been triggered and results should be available
		assertThat(searchResultsTable(1).rowCount()).isEqualTo(1);
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
		// then no search should have been triggered and no result should be available
		assertThat(searchResultsTable(0).rowCount()).isEqualTo(0);
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
		pullWizardBot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).setText("foo/bar");
		// ... and then opening the search wizard
		openSearchWizard();
		// then the search should have been triggered and a single result should be
		// available
		assertThat(searchResultsTable(1).rowCount()).isEqualTo(1);
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
		pullWizardBot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).setText("bar");
		// ... and then opening the search wizard
		openSearchWizard();
		// then the search should have been triggered and both results should be
		// available
		assertThat(searchResultsTable(2).rowCount()).isEqualTo(2);
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
		this.pullWizardBot = SWTUtils.waitForShell(bot, WizardMessages.getString("ImagePull.title")); //$NON-NLS-1$
	}

	/**
	 * Clicks on the "Search..." button of the Pull wizard and waits for the
	 * search wizard to show up. The search itself runs in the background and
	 * fills the results table asynchronously, so use
	 * {@link #searchResultsTable(int)} to get to the results.
	 */
	private void openSearchWizard() {
		// click on the "Search..." button
		pullWizardBot.button(WizardMessages.getString("ImagePull.search.label")).click();
		this.searchWizardShell = bot.shell(WizardMessages.getString("ImageSearch.title")); //$NON-NLS-1$
		this.searchWizardShell.activate();
		this.searchWizardBot = this.searchWizardShell.bot();
	}

	/**
	 * @return the search results table of the search wizard, once it holds the
	 *         given number of rows.
	 */
	private SWTBotTable searchResultsTable(final int expectedRowCount) {
		final SWTBotTable table = searchWizardBot.table();
		searchWizardBot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() {
				return table.rowCount() == expectedRowCount;
			}

			@Override
			public String getFailureMessage() {
				return "Expected " + expectedRowCount + " search result(s) but the table shows " + table.rowCount();
			}
		});
		return table;
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
		this.pullWizardBot = SWTUtils.waitForShell(bot, WizardMessages.getString("ImagePull.title")); //$NON-NLS-1$

		// when specifying a term
		pullWizardBot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).setText("foo");

		// when clicking on the "Search..." button
		openSearchWizard();

		// then the search should have been triggered and results should be
		// available
		final SWTBotTable searchResultsTable = searchResultsTable(1);
		assertThat(searchResultsTable.rowCount()).isEqualTo(1);
		// the first result is selected once the table is filled, which enables the
		// wizard buttons
		searchWizardBot.waitUntil(Conditions.widgetIsEnabled(searchWizardBot.button("Finish")));
		assertThat(searchWizardBot.button("Next >").isEnabled()).isTrue();
		assertThat(searchWizardBot.button("Finish").isEnabled()).isTrue();
		searchWizardBot.button("Finish").click();

		// when back to Pull wizard, the Image name field should be filled
		bot.waitUntil(Conditions.shellCloses(searchWizardShell));
		assertThat(pullWizardBot.textWithLabel(WizardMessages.getString("ImagePullPushPage.name.label")).getText())
				.isEqualTo("foo:latest");
	}

}
