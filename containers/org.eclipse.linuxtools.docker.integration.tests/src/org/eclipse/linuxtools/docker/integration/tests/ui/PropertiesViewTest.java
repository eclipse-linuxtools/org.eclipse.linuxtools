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

package org.eclipse.linuxtools.docker.integration.tests.ui;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerContainersTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.eclipse.ui.views.properties.PropertiesView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 *
 */
public class PropertiesViewTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = IMAGE_BUSYBOX;
	private static final String CONTAINER_NAME = "test_run_docker_busybox";

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_NAME);
	}

	@Test
	public void testContainerPropertiesTab() {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(IMAGE_NAME);
		ImageRunSelectionPage firstPage = new ImageRunSelectionPage();
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.finish();
		// new WaitWhile(new ContainerIsDeployedCondition(CONTAINER_NAME,
		// getConnection()));
		DockerContainersTab containerTab = new DockerContainersTab();
		containerTab.activate();
		containerTab.refresh();
		new WaitWhile(new JobIsRunning());
		if (mockitoIsUsed()) {
			MockUtils.runContainer(DEFAULT_CONNECTION_NAME, IMAGE_NAME, IMAGE_TAG_LATEST, CONTAINER_NAME);
		}
		getConnection();
		// open Properties view
		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		containerTab.select(CONTAINER_NAME);
		propertiesView.selectTab("Info");
	}

	@Test
	public void testImagePropertiesTab() {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.selectImage(IMAGE_NAME);
		// DockerExplorerView de = new DockerExplorerView();
		// de.open();
		// de.getDockerConnectionByName(getConnection().getName()).getImage(IMAGE_NAME).select();
		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		propertiesView.selectTab("Info");
	}

	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME);
	}
}