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

import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 *
 */

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