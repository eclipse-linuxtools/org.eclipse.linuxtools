/*******************************************************************************
 * Copyright (c) 2017,2018 Red Hat, Inc.
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

import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.junit.After;
import org.junit.Test;

public class BuildImageTest extends AbstractImageBotTest {

	@Test
	public void testBuildImage() {
		getConnection();
		DockerImagesTab imageTab = openDockerImagesTab();

		buildImage(IMAGE_TEST_BUILD, DOCKERFILE_FOLDER, imageTab);

		assertConsoleSuccess();
	}

	@Override
	@After
	public void after() {
		deleteImageContainer(IMAGE_TEST_BUILD);
		cleanUpWorkspace();
	}

}