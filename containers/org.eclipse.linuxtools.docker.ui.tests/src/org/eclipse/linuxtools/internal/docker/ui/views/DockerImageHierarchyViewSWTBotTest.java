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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerImageHierarchyViewAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.MenuAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TestLoggerRule;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Image;

/**
 * Testing the {@link DockerImageHierarchyView} call from the
 * {@link DockerExplorerView}.
 */
public class DockerImageHierarchyViewSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerExplorerViewBot;
	private DockerExplorerView dockerExplorerView;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.DOCKER_PERSPECTIVE_ID);

	@Rule
	public TestLoggerRule watcher = new TestLoggerRule();

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();
	private DockerConnection connection;

	@Before
	public void setupViews() {
		this.bot = new SWTWorkbenchBot();
		SWTUtils.asyncExec(() -> {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(DockerExplorerView.VIEW_ID);
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
			}
		});
		this.dockerExplorerViewBot = bot.viewById(DockerExplorerView.VIEW_ID);
		this.dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		// make sure that the Docker Image Hierarchy view is closed
		this.bot.views().stream().filter(v -> v.getReference().getId().equals(DockerImageHierarchyView.VIEW_ID))
				.forEach(v -> v.close());
	}

	@Before
	public void setupData() {
		// data is built as follows:
		// root_image
		// |- foo_image1
		// _|- foo_container1 (Up)
		// _|- foo_image2
		// __|- foo_container21 (Exited)
		// __|- foo_container22 (Paused)
		// |- bar_image11
		// _|- bar_container1

		final Image rootImage = MockImageFactory.id("sha256:root_image").name("root_image").build();
		final Image fooImage1 = MockImageFactory.id("sha256:foo_image1").name("foo_image1")
				.parentId("sha256:root_image").build();
		final Image fooImage2 = MockImageFactory.id("sha256:foo_image2").name("foo_image2", "foo_image2_alias")
				.parentId("sha256:foo_image1").build();
		final Container fooContainer1 = MockContainerFactory.id("sha256:foo_container1").name("foo_container1")
				.imageName("foo_image1").status("Up").build();
		final Container fooContainer21 = MockContainerFactory.id("sha256:foo_container21").name("foo_container21")
				.imageName("foo_image2").status("Exited").build();
		final Container fooContainer22 = MockContainerFactory.id("sha256:foo_container22").name("foo_container22")
				.imageName("foo_image2_alias").status("Up (Paused)").build();
		final Image barImage1 = MockImageFactory.id("sha256:bar_image1").name("bar_image1")
				.parentId("sha256:root_image").build();
		final Container barContainer1 = MockContainerFactory.id("sha256:bar_container1").name("bar_container1")
				.imageName("bar_image1").build();
		final DockerClient client = MockDockerClientFactory.image(rootImage).image(fooImage1).container(fooContainer1)
				.image(fooImage2).container(fooContainer21).container(fooContainer22).image(barImage1)
				.container(barContainer1).build();
		this.connection = MockDockerConnectionFactory.from("Test", client).withDefaultTCPConnectionSettings();
		this.connection.getImages(true);
		this.connection.getContainers(true);
		DockerConnectionManagerUtils.configureConnectionManager(connection);

	}

	@After
	public void hideMenu() {
		try {
			SWTUtils.hideMenu(dockerExplorerViewBot.bot().tree());
		} catch (WidgetNotFoundException e) {
			// ignore if widget is not found, that's probably because there's no
			// tree in the
			// Docker Explorer view for the test that just ran.
		}
	}

	private List<String> getChildrenElementIds(final IDockerImageHierarchyNode fooImageHierarchy) {
		return fooImageHierarchy.getChildren().stream().map(e -> {
			if (e.getElement() instanceof IDockerImage) {
				return ((IDockerImage) e.getElement()).id();
			}
			return ((IDockerContainer) e.getElement()).id();
		}).collect(Collectors.toList());
	}

	private DockerImageHierarchyView getDockerImageHierarchyView() {
		final SWTBotView hierarchyViewBot = bot.viewById(DockerImageHierarchyView.VIEW_ID);
		return (DockerImageHierarchyView) hierarchyViewBot.getViewReference().getView(true);
	}

	private static SWTBotTreeItem selectImageInTreeView(final SWTWorkbenchBot bot, final String... path) {
		final SWTBotView dockerImageHierarchyViewBot = bot.viewById(DockerImageHierarchyView.VIEW_ID);
		final DockerImageHierarchyView dockerImageHierarchyView = (DockerImageHierarchyView) (dockerImageHierarchyViewBot
				.getViewReference().getView(true));
		SWTUtils.asyncExec(() -> dockerImageHierarchyView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		return SWTUtils.getTreeItem(dockerImageHierarchyViewBot, path).select();
	}

	@Test
	public void shouldDisplayImageHierarchyViewWhenSelectingImage() {
		// given
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo_image1").select();
		// when
		dockerExplorerViewBot.bot().tree().contextMenu("Open Image Hierarchy").click(); //$NON-NLS-1$
		// then the view should be visible and selection should be on
		// foo_image1
		DockerImageHierarchyViewAssertion.assertThat(getDockerImageHierarchyView())
				.hasSelectedElement(this.connection.getImage("sha256:foo_image1"));
	}

	@Test
	public void shouldDisplayImageHierarchyViewWhenSelectingImageAlias() {
		// given
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo_image2_alias").select();
		// when
		dockerExplorerViewBot.bot().tree().contextMenu("Open Image Hierarchy").click(); //$NON-NLS-1$
		// then the view should be visible and selection should be on
		// foo_image2
		DockerImageHierarchyViewAssertion.assertThat(getDockerImageHierarchyView())
				.hasSelectedElement(this.connection.getImage("sha256:foo_image2"));

	}

	@Test
	public void shouldDisplayImageHierarchyViewWhenSelectingContainer() {
		// given
		SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Containers", "foo_container1").select();
		// when
		dockerExplorerViewBot.bot().tree().contextMenu("Open Image Hierarchy").click(); //$NON-NLS-1$
		// then the view should be visible and selection should be on
		// foo_container1
		DockerImageHierarchyViewAssertion.assertThat(getDockerImageHierarchyView())
				.hasSelectedElement(this.connection.getContainer("sha256:foo_container1"));
	}

	@Test
	public void shouldRetrieveImageHierarchyFromRootImage() {
		// given
		final IDockerImage rootImage = this.connection.getImage("sha256:root_image");
		// when
		final IDockerImageHierarchyNode rootImageHierarchy = this.connection.resolveImageHierarchy(rootImage);
		// then
		assertThat(rootImageHierarchy).isNotNull();
		assertThat(rootImageHierarchy.getElement()).isEqualTo(rootImage);
		// 2 direct children: foo_image1 and bar_image1
		assertThat(rootImageHierarchy.getChildren()).hasSize(2);
		assertThat(rootImageHierarchy.getParent()).isNull();
		final IDockerImageHierarchyNode fooImage1Hierarchy = rootImageHierarchy.getChild("sha256:foo_image1");
		assertThat(fooImage1Hierarchy.getParent()).isEqualTo(rootImageHierarchy);
		// 2 direct children: foo_image2/foo_image2_alias and foo_container1
		assertThat(fooImage1Hierarchy.getChildren()).hasSize(2);
	}

	@Test
	public void shouldRetrieveImageHierarchyFromIntermediateImage() {
		// given
		final IDockerImage fooImage1 = this.connection.getImage("sha256:foo_image1");
		// when
		final IDockerImageHierarchyNode fooImage1Hierarchy = this.connection.resolveImageHierarchy(fooImage1);
		// then
		assertThat(fooImage1Hierarchy).isNotNull();
		assertThat(fooImage1Hierarchy.getElement()).isEqualTo(fooImage1);
		assertThat(getChildrenElementIds(fooImage1Hierarchy)).contains("sha256:foo_container1", "sha256:foo_image2");
		final IDockerImage rootElement = (IDockerImage) fooImage1Hierarchy.getParent().getElement();
		assertThat(rootElement.id()).isEqualTo("sha256:root_image");
		// the parent only shows this child element, not its whole descendants
		assertThat(fooImage1Hierarchy.getParent().getChildren()).containsExactly(fooImage1Hierarchy);
		final IDockerImageHierarchyNode fooImage2HierarchyNode = fooImage1Hierarchy.getChild("sha256:foo_image2");
		assertThat(fooImage2HierarchyNode).isNotNull();
		// 2 child containers: foo_container21 and foo_container22
		assertThat(fooImage2HierarchyNode.getChildren()).hasSize(2);
		final IDockerImageHierarchyNode fooContainer1HierarchyNode = fooImage1Hierarchy
				.getChild("sha256:foo_container1");
		assertThat(fooContainer1HierarchyNode).isNotNull();
		assertThat(fooContainer1HierarchyNode.getChildren()).isEmpty();
	}

	@Test
	public void shouldRetrieveImageHierarchyFromLeafImage() {
		// given
		final IDockerImage fooImage2 = this.connection.getImage("sha256:foo_image2");
		// when
		final IDockerImageHierarchyNode fooImage2Hierarchy = this.connection.resolveImageHierarchy(fooImage2);
		// then
		assertThat(fooImage2Hierarchy).isNotNull();
		assertThat(fooImage2Hierarchy.getElement()).isEqualTo(fooImage2);
		// 2 containers: foo_container21 and foo_container22
		assertThat(fooImage2Hierarchy.getChildren()).hasSize(2);
		assertThat((IDockerContainer) fooImage2Hierarchy.getChild("sha256:foo_container21").getElement()).isNotNull();
		final IDockerImageHierarchyNode fooImage1Hierarchy = fooImage2Hierarchy.getParent();
		final IDockerImage fooImage = (IDockerImage) fooImage1Hierarchy.getElement();
		assertThat(fooImage.id()).isEqualTo("sha256:foo_image1");
		// in this case, intermediate image shows a single child
		assertThat(fooImage1Hierarchy.getChildren()).hasSize(1);
		assertThat(fooImage1Hierarchy.getChildren()).containsExactly(fooImage2Hierarchy);
	}

	@Test
	public void shouldRetrieveImageHierarchyFromContainerBasedOnIntermediateImage() {
		// given
		final IDockerContainer fooContainer1 = this.connection.getContainer("sha256:foo_container1");
		// when
		final IDockerImageHierarchyNode fooContainer1Hierarchy = this.connection.resolveImageHierarchy(fooContainer1);
		// then
		assertThat(fooContainer1Hierarchy).isNotNull();
		assertThat(fooContainer1Hierarchy.getElement()).isEqualTo(fooContainer1);
		assertThat(fooContainer1Hierarchy.getChildren()).isEmpty();
		final IDockerImage fooImage1 = (IDockerImage) fooContainer1Hierarchy.getParent().getElement();
		assertThat(fooImage1.id()).isEqualTo("sha256:foo_image1");
		// parent image hierarchy only shows the selected container as its
		// child.
		assertThat(fooContainer1Hierarchy.getParent().getChildren()).containsExactly(fooContainer1Hierarchy);
	}

	@Test
	public void shouldRetrieveImageHierarchyFromContainerBasedOnLeafImage() {
		// given
		// when
		final IDockerContainer barContainer1 = this.connection.getContainer("sha256:bar_container1");
		final IDockerImageHierarchyNode barContainer1Hierarchy = this.connection.resolveImageHierarchy(barContainer1);
		// then
		assertThat(barContainer1Hierarchy).isNotNull();
		assertThat(barContainer1Hierarchy.getElement()).isEqualTo(barContainer1);
		assertThat(barContainer1Hierarchy.getChildren()).isEmpty();
		final IDockerImage barImageElement = (IDockerImage) barContainer1Hierarchy.getParent().getElement();
		assertThat(barImageElement.id()).isEqualTo("sha256:bar_image1");
	}

	@Test
	public void shouldShowSelectedImageInPropertiesView() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root");
		// show container info in Properties view
		SWTUtils.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Show In", "Properties")
				.click();
		// the properties view should be visible and filled with image details
		final SWTBotView propertiesBotView = this.bot.viewById("org.eclipse.ui.views.PropertySheet");
		assertThat(propertiesBotView.isActive()).isEqualTo(true);
		final PropertySheet propertiesView = (PropertySheet) propertiesBotView.getViewReference().getView(false);
		assertThat(((TabbedPropertySheetPage) propertiesView.getCurrentPage()).getCurrentTab()).isNotNull();
	}

	@Test
	public void shouldShowSelectedContainerInPropertiesView() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_container1");
		// show container info in Properties view
		SWTUtils.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Show In", "Properties")
				.click();
		// the properties view should be visible and filled with image details
		final SWTBotView propertiesBotView = this.bot.viewById("org.eclipse.ui.views.PropertySheet");
		assertThat(propertiesBotView.isActive()).isEqualTo(true);
		final PropertySheet propertiesView = (PropertySheet) propertiesBotView.getViewReference().getView(false);
		assertThat(((TabbedPropertySheetPage) propertiesView.getCurrentPage()).getCurrentTab()).isNotNull();
	}

	@Test
	public void shouldProvideEnabledRemoveCommandOnSelectedImage() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Remove");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledAddTagCommandOnSelectedImage() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Add Tag");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledPushCommandOnSelectedImage() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Push...");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledStartCommandOnSelectedContainer() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_image2", "foo_container21");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Start");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledPauseCommandOnSelectedContainer() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_container1");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Pause");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledUnpauseCommandOnSelectedContainer() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_image2", "foo_container22");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Unpause");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledKillCommandOnSelectedContainer() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_container1");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Kill");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledRemoveCommandOnSelectedContainer() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_image2", "foo_container21");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Remove");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledCommitCommandOnSelectedContainer() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_image2", "foo_container21");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Commit");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledDisplayLogCommandOnSelectedContainer() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_image2", "foo_container21");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Display Log");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

	@Test
	public void shouldProvideEnabledRemoveLogCommandOnSelectedContainer() {
		// given
		shouldDisplayImageHierarchyViewWhenSelectingImage();
		// when
		selectImageInTreeView(bot, "root", "foo_image1", "foo_image2", "foo_container21");
		final SWTBotMenu menu = SWTUtils
				.getContextMenu(bot.viewById(DockerImageHierarchyView.VIEW_ID).bot().tree(), "Remove Log");
		// then
		MenuAssertion.assertThat(menu).isVisible().isEnabled();
	}

}
