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

import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.junit.After;
import org.junit.Test;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class BuildImageTest extends AbstractImageBotTest {

	@Test
	public void testBuildImage() {
		getConnection();
		DockerImagesTab imageTab = openDockerImagesTab();

		buildImage(IMAGE_TEST_BUILD, DOCKERFILE_FOLDER, imageTab);

		assertConsoleSuccess();
	}

	@After
	public void after() {
		deleteImageContainer(IMAGE_TEST_BUILD);
		cleanUpWorkspace();
	}

}