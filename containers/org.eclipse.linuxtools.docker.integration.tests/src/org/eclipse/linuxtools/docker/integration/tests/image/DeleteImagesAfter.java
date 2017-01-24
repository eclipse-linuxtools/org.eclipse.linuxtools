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

import org.junit.Before;
import org.junit.Test;

/**
 * This class is deleting all used images in tests. Images are not deleted after
 * every test to speed up the suite.
 * 
 * @author jkopriva@redhat.com
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