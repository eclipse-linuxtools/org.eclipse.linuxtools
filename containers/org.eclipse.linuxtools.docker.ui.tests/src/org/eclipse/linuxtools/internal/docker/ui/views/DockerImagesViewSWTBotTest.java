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

package org.eclipse.linuxtools.internal.docker.ui.views;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerImageHierarchyViewAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TabDescriptorAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TestLoggerRule;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link DockerImagesView} using {@link SWTBot}.
 */
public class DockerImagesViewSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerImagesBotView;
	private DockerImagesView dockerImagesView;
	private SWTBotView dockerExplorerBotView;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public TestLoggerRule watcher = new TestLoggerRule();

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Before
	public void setup() {
		this.bot = new SWTWorkbenchBot();
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name("angry_bar").status("Stopped").build())
				.image(MockImageFactory.id("987654321abcde").name("default:1").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Default", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);

		SWTUtils.asyncExec(() -> {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(DockerExplorerView.VIEW_ID);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DockerImagesView.VIEW_ID);
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Failed to open Docker Images view: " + e.getMessage());
			}
		});
		this.dockerImagesBotView = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerImagesView");
		this.dockerImagesView = (DockerImagesView) (dockerImagesBotView.getViewReference().getView(true));
		this.dockerExplorerBotView = bot.viewById(DockerExplorerView.VIEW_ID);
	}

	private SWTBotTableItem selectImageInTable(final String imageName) {
		final SWTBotTableItem tableItem = SWTUtils.getListItem(dockerImagesBotView.bot().table(), imageName);
		assertThat(tableItem).isNotNull();
		return tableItem.click().select();
	}

	@Test
	public void shouldShowDefaultImages() {
		// then default connection with 1 image should be displayed
		SWTUtils.syncAssert(() -> {
			final TableItem[] images = dockerImagesView.getViewer().getTable().getItems();
			assertThat(images).hasSize(1);
			assertThat(images[0].getText(1)).isEqualTo("default:1");
		});

	}

	@Test
	public void shouldShowAllImageVariants() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.id("1a2b3c4d5e6f7g")
				.name("foo:1.0", "foo:latest", "bar:1.0", "bar:latest").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// when
		SWTUtils.getTreeItem(dockerExplorerBotView, "Test").select();
		// then 1 image with all repo/tags should be displayed
		SWTUtils.syncAssert(() -> {
			final TableItem[] images = dockerImagesView.getViewer().getTable().getItems();
			assertThat(images).hasSize(1);
			assertThat(images[0].getText(1)).isEqualTo("bar:1.0\nbar:latest\nfoo:1.0\nfoo:latest");
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
		dockerConnection.removeContainerListener(DockerContainerRefreshManager.getInstance());
		// DockerExplorerView inner classes
		assertThat(dockerConnection.getImageListeners()).hasSize(2);
		// close the Docker Images View
		dockerImagesBotView.close();
		// there should be one listener left: DockerExplorerView
		assertThat(dockerConnection.getImageListeners()).hasSize(1);
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
		dockerConnection.removeContainerListener(DockerContainerRefreshManager.getInstance());
		assertThat(dockerConnection.getImageListeners()).hasSize(0);
		// close the Docker Images View
		dockerImagesBotView.close();
		// there should be one listener left: DockerExplorerView
		assertThat(dockerConnection.getImageListeners()).hasSize(0);
	}

	@Test
	public void shouldOpenImageHierarchyView() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("angry_bar").build())
				.image(MockImageFactory.name("gentle_foo").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// make sure the hierarchy view is closed.
		SWTUtils.closeView(this.bot, DockerImageHierarchyView.VIEW_ID);
		// open the context menu on one of the containers
		selectImageInTable("angry_bar");
		SWTUtils.getContextMenu(dockerImagesBotView.bot().table(), "Open Image Hierarchy").click();
		// wait 1sec
		SWTUtils.wait(1, TimeUnit.SECONDS);
		DockerImageHierarchyViewAssertion.assertThat(SWTUtils.getView(bot, DockerImageHierarchyView.VIEW_ID))
				.isNotNull();
	}

	@Test
	public void shouldShowSelectedImageInPropertiesView() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockImageFactory.name("angry_bar").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		final PropertySheet propertySheet = SWTUtils
				.syncExec(() -> SWTUtils.getView(bot, "org.eclipse.ui.views.PropertySheet", true));
		this.dockerImagesView.setFocus();
		// select the container in the table
		selectImageInTable("angry_bar");
		// then the properties view should have a selected tab with container
		// info
		assertThat(propertySheet.getCurrentPage()).isInstanceOf(TabbedPropertySheetPage.class);
		final TabbedPropertySheetPage currentPage = (TabbedPropertySheetPage) propertySheet.getCurrentPage();
		TabDescriptorAssertion.assertThat(currentPage.getSelectedTab()).isNotNull()
				.hasId("org.eclipse.linuxtools.docker.ui.properties.image.info");
	}


}
