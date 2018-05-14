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

import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PullImageTest extends AbstractImageBotTest {

	@Before
	public void before() {
		deleteAllConnections();
		getConnection();
		deleteImageIfExists(IMAGE_ALPINE_33);
		deleteImageIfExists(IMAGE_BUSYBOX_LATEST);
	}

	@Test
	public void testPullImageWithTag() {
		clearConsole();
		pullImage(IMAGE_ALPINE, IMAGE_ALPINE_TAG, null);
		new WaitWhile(new JobIsRunning());
		assertTrue("Image has not been deployed!", imageIsDeployed(IMAGE_ALPINE_33));
	}

	@Test
	public void testPullImageWithoutTag() {
		clearConsole();
		pullImage(IMAGE_BUSYBOX);
		new WaitWhile(new JobIsRunning());
		assertTrue("Image has not been deployed!", imageIsDeployed(IMAGE_BUSYBOX_LATEST));
		assertTrue("Multiple tags of the same image has been deployed, not only latest tag!",
				deployedImagesCount(IMAGE_BUSYBOX) == 1);
	}

	@Override
	@After
	public void after() {
		// cleanup for testPullImageWithoutTag()
		for (String imageName : getConnection().getImagesNames(true)) {
			if (imageName.contains(IMAGE_BUSYBOX)) {
				deleteImageContainer(imageName);// cleanup for
												// testPullImageWithoutTag()
			}
		}
		cleanUpWorkspace();
	}
}