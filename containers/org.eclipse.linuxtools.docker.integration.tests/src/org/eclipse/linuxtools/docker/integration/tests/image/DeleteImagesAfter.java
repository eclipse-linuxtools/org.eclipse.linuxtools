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

import org.junit.Before;
import org.junit.Test;

/**
 * This class is deleting all used images in tests. Images are not deleted after
 * every test to speed up the suite.
 *
 */
public class DeleteImagesAfter extends AbstractImageBotTest {

	@Before
	public void before (){
		prepareConnections();
	}

	@Test
	public void deleteUsedImages() {
		deleteImageIfExists(IMAGE_ALPINE, IMAGE_ALPINE_TAG);
		deleteImageIfExists(IMAGE_BUSYBOX);
		deleteImageIfExists(IMAGE_CIRROS, IMAGE_CIRROS_TAG);
		deleteImageIfExists(IMAGE_UHTTPD);
		deleteImageIfExists(IMAGE_HELLO_WORLD);
		deleteImageIfExists(REGISTRY_SERVER_ADDRESS + "/" + IMAGE_RHEL);
	}
}