/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests.image;

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.impl.button.CancelButton;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ImageTagTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = IMAGE_BUSYBOX;
	private static final String IMAGE_TAG = "testtag";
	private static final String IMAGE_TAG_UPPERCASE = "UPPERCASETAG";

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_NAME);
		getConnection().refresh();
		new WaitWhile(new JobIsRunning());
		assertTrue("Image has not been deployed!", imageIsDeployed(IMAGE_NAME));
	}

	@Test
	public void testAddRemoveTagToImage() {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.activate();
		addTagToImage(IMAGE_NAME, IMAGE_TAG);
		new WaitWhile(new JobIsRunning());
		assertTrue("Image tag has not been added", imagesTab.getImageTags(IMAGE_NAME).contains(IMAGE_TAG));
		removeTagFromImage(IMAGE_NAME, IMAGE_TAG);
		new WaitWhile(new JobIsRunning());
		assertTrue("ImageTaghasNotBeenRemoved", !imagesTab.getImageTags(IMAGE_NAME).contains(IMAGE_TAG));
	}

	@Test
	public void testAddUpperCaseTagToImage() {
		DockerExplorerView explorer = new DockerExplorerView();
		explorer.open();
		try {
			getConnection().getImage(IMAGE_NAME).addTagToImage(IMAGE_TAG_UPPERCASE);
		} catch (WaitTimeoutExpiredException ex) {
			new CancelButton().click();
			// swallowing, it is not possible to tag image with upper case
		}
	}

	@Override
	@After
	public void after() {
		deleteImageContainerAfter(IMAGE_NAME);
		cleanUpWorkspace();
	}
}