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

package org.eclipse.linuxtools.internal.docker.ui.views;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerImageHierarchyViewAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.MenuAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TabDescriptorAssertions;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mandas.docker.client.DockerClient;

/**
 * Testing the {@link DockerContainersView}
 */
public class DockerContainersViewSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerContainersViewBot;
	private DockerContainersView dockerContainersView;
	private SWTBotView dockerExplorerBotView;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Before
	public void setup() {
		this.bot = new SWTWorkbenchBot();
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("defaultcon").status("Running").build())
				.image(MockImageFactory.id("987654321abcde").name("default:1").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Default", client)
				.withDefaultTCPConnectionSettings();
		dockerConnection.removeContainerListener(DockerContainerRefreshManager.getInstance());
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> {try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(DockerContainersView.VIEW_ID);
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
			.showView(DockerExplorerView.VIEW_ID);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
		}});
		this.dockerContainersViewBot = bot.viewById(DockerContainersView.VIEW_ID);
		this.dockerContainersView = (DockerContainersView) (dockerContainersViewBot.getViewReference().getView(true));
		this.dockerExplorerBotView = bot.viewById(DockerExplorerView.VIEW_ID);
	}

	private SWTBotTableItem selectContainerInTable(final String containerName) {
		final SWTBotTableItem tableItem = SWTUtils.getListItem(dockerContainersViewBot.bot().table(), containerName);
		assertThat(tableItem).isNotNull();
		return tableItem.click().select();
	}

	private void selectContainersInTable(final String... items) {
		final SWTBotTable table = dockerContainersViewBot.bot().table();
		assertThat(table).isNotNull();
		table.select(items);
	}

	@Test
	public void defaultContainersTest() {
		// default connection with 1 images should be displayed
		SWTUtils.syncAssert(() -> {
			final TableItem[] containers = dockerContainersView.getViewer().getTable().getItems();
			assertThat(containers).hasSize(1);
			assertThat(containers[0].getText(0)).isEqualTo("defaultcon");
		});

	}

	@Test
	public void shouldRemoveListenersWhenClosingView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.getTreeItem(dockerExplorerBotView, "Test").select();
		// remove the DockerContainerRefreshManager
		dockerConnection.removeContainerListener(DockerContainerRefreshManager
								.getInstance());
		assertThat(dockerConnection.getContainerListeners()).hasSize(2);
		// close the Docker Containers View
		dockerContainersViewBot.close();
		// there should be one listener left: DockerExplorerView
		assertThat(dockerConnection.getContainerListeners()).hasSize(1);
	}

	@Test
	public void shouldNotRemoveListenersWhenNoSelectedConnectionBeforeClosingView() {
		// given
		dockerExplorerBotView.close();
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// remove the DockerContainerRefreshManager
		dockerConnection.removeContainerListener(DockerContainerRefreshManager
								.getInstance());
		assertThat(dockerConnection.getContainerListeners()).hasSize(0);
		// close the Docker Containers View
		dockerContainersViewBot.close();
		// there should be one listener left: DockerExplorerView
		assertThat(dockerConnection.getContainerListeners()).hasSize(0);
	}

	@Test
	public void shouldOpenImageHierarchyView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// make sure the hierarchy view is closed.
		SWTUtils.closeView(this.bot, DockerImageHierarchyView.VIEW_ID);
		// open the context menu on one of the containers
		selectContainerInTable("angry_bar");
		SWTUtils.getContextMenu(dockerContainersViewBot.bot().table(), "Open Image Hierarchy").click();
		// wait 1sec
		SWTUtils.wait(1, TimeUnit.SECONDS);
		DockerImageHierarchyViewAssertions.assertThat(SWTUtils.getView(bot, DockerImageHierarchyView.VIEW_ID))
				.isNotNull();
	}

	@Test
	public void shouldProvideEnabledRestartOnMultipleContainers() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockContainerFactory.name("bold_eagle").status("Stopped").build())
				.container(MockContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// make sure the hierarchy view is closed.
		SWTUtils.closeView(this.bot, DockerImageHierarchyView.VIEW_ID);
		// open the context menu on one of the containers
		selectContainersInTable("gentle_foo", "bold_eagle", "angry_bar");
		final SWTBotMenu menuCommand = dockerContainersViewBot.bot().table().contextMenu("Restart");
		// then
		MenuAssertion.assertThat(menuCommand).isVisible().isEnabled();
	}

	@Test
	public void shouldShowSelectedContainerInPropertiesView() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build(),
						MockContainerInfoFactory.networkMode("host").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final PropertySheet propertySheet = SWTUtils
				.syncExec(() -> SWTUtils.getView(bot, "org.eclipse.ui.views.PropertySheet", true));
		this.dockerContainersView.setFocus();
		// select the container in the table
		selectContainerInTable("angry_bar");
		// then the properties view should have a selected tab with container
		// info
		assertThat(propertySheet.getCurrentPage()).isInstanceOf(TabbedPropertySheetPage.class);
		final TabbedPropertySheetPage currentPage = (TabbedPropertySheetPage) propertySheet.getCurrentPage();
		TabDescriptorAssertions.assertThat(currentPage.getSelectedTab()).isNotNull()
				.hasId("org.eclipse.linuxtools.docker.ui.properties.container.info");
	}

}
