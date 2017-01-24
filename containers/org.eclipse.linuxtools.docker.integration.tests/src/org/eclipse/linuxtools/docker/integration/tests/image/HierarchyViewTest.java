/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests.image;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImageHierarchyTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.ShellWithTextIsAvailable;
import org.jboss.reddeer.swt.api.TreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;

/**
 * 
 * @author jkopriva@redhat.com
 * @cotributor adietish@redhat.com
 *
 */

public class HierarchyViewTest extends AbstractImageBotTest {
	private static final int DAEMON_MAJOR_VERSION = 11;
	private static final int DAEMON_MINOR_VERSION = 1;

	private DockerConnection connection;

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
	}

	@Test
	public void testHierarchyView() {
		DockerImagesTab imageTab = openDockerImagesTab();

		buildImage(IMAGE_TEST_BUILD, DOCKERFILE_FOLDER, imageTab);

		if (mockitoIsUsed()) {
			buildImage();
		}

		DockerImageHierarchyTab hierarchyTab = openDockerImageHierarchyTab();
		List<TreeItem> treeItems = hierarchyTab.getTreeItems();
		compareTextInFirstNode(treeItems, "alpine:3.3");
		List<TreeItem> treeItems2 = treeItems.get(0).getItems();
		compareTextInFirstNode(treeItems2, IMAGE_TEST_BUILD + NAME_TAG_SEPARATOR + IMAGE_TAG_LATEST);
	}

	public void compareTextInFirstNode(List<TreeItem> treeItems, String expectedValue) {
		String nodeText = treeItems.get(0).getText().replaceAll("\\(.*\\)", "").trim();
		if (!mockitoIsUsed() && isDockerDaemon(DAEMON_MAJOR_VERSION, DAEMON_MINOR_VERSION)) {
			nodeText = nodeText.replaceAll("docker.io/", ""); // On older
																// deamons is
																// this prefix
		}
		assertTrue("Hierarchy view contains string: " + nodeText + ", but it is expected: " + expectedValue,
				nodeText.startsWith(expectedValue));
	}

	private DockerImageHierarchyTab openDockerImageHierarchyTab() {
		getConnection().getImage(IMAGE_TEST_BUILD).openImageHierarchy();
		new WaitWhile(new ShellWithTextIsAvailable("Docker Image Hierarchy"));
		DockerImageHierarchyTab hierarchyTab = new DockerImageHierarchyTab();
		hierarchyTab.open();
		return hierarchyTab;
	}

	@After
	public void after() {
		deleteImageContainerAfter(IMAGE_TEST_BUILD);
		cleanUpWorkspace();
	}

	public void buildImage() {
		final Image rootImage = MockImageFactory.id("sha256:alpine:3.3").name("alpine:3.3").build();
		final Image fooImage1 = MockImageFactory
				.id("sha256:" + IMAGE_TEST_BUILD + NAME_TAG_SEPARATOR + IMAGE_TAG_LATEST)
				.name(IMAGE_TEST_BUILD + NAME_TAG_SEPARATOR + IMAGE_TAG_LATEST).parentId("sha256:alpine:3.3").build();
		final DockerClient client = MockDockerClientFactory.image(rootImage).image(fooImage1).build();
		this.connection = MockDockerConnectionFactory.from(DEFAULT_CONNECTION_NAME, client)
				.withDefaultTCPConnectionSettings();
		this.connection.getImages(true);
		this.connection.getContainers(true);
		MockDockerConnectionManager.configureConnectionManager(connection);
	}
}