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

package org.eclipse.linuxtools.docker.integration.tests.container;

import java.io.IOException;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.BrowserView;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.docker.reddeer.utils.BrowserContentsCheck;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.junit.After;
import org.junit.Test;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class ExposePortTest extends AbstractImageBotTest {

	private static final String CONTAINER_NAME = "test_run_uhttpd";
	private static final String EXPOSED_PORT = "80";

	@Test
	public void testExposePort() throws IOException {
		pullImage(IMAGE_UHTTPD, IMAGE_TAG_LATEST);
		DockerImagesTab imagesTab = openDockerImagesTab();
		runContainer(IMAGE_UHTTPD, IMAGE_TAG_LATEST, CONTAINER_NAME, imagesTab);
		if(mockitoIsUsed()){
			MockUtils.runContainer(DEFAULT_CONNECTION_NAME, IMAGE_UHTTPD, IMAGE_TAG_LATEST, CONTAINER_NAME);
		}
		assertPortIsAccessible(EXPOSED_PORT);
	}

	private void assertPortIsAccessible(String exposedPort) {
		BrowserView browserView = new BrowserView();
		browserView.open();
		String url = createURL(":" + exposedPort);
		if (!mockitoIsUsed()) {
			BrowserContentsCheck.checkBrowserForErrorPage(browserView, url);
		}
	}

	private void runContainer(String imageName, String imageTag, String containerName, DockerImagesTab imagesTab) {
		imagesTab.runImage(imageName + ":" + imageTag);
		ImageRunSelectionPage firstPage = new ImageRunSelectionPage();
		firstPage.setContainerName(containerName);
		firstPage.setPublishAllExposedPorts(false);
		firstPage.finish();
		new WaitWhile(new JobIsRunning());
		new WaitWhile(new ConsoleHasNoChange());
	}

	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME);
	}

}