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

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 */
public class DifferentRegistryTest extends AbstractImageBotTest {

	private static final String EMAIL = "test@test.com";
	private static final String USERNAME = "test";
	private static final String PASSWORD = "password";
	private static final String IMAGE_RHEL = "rhel";
	private static final String IMAGE_RHEL_TAG = "7.2";

	@Before
	public void before() {
		prepareConnections();
		deleteImageIfExists(REGISTRY_SERVER_ADDRESS + "/" + IMAGE_RHEL);
		deleteRegisterIfExists(REGISTRY_SERVER_ADDRESS);
	}

	@Test
	public void testDifferentRegistry() {
		clearConsole();
		setUpRegister(REGISTRY_SERVER_ADDRESS, EMAIL, USERNAME, PASSWORD);
		setSecureStorage(PASSWORD);
		pullImage(IMAGE_RHEL, IMAGE_RHEL_TAG, USERNAME + "@" + REGISTRY_SERVER_ADDRESS);
		assertTrue("Image is not deployed!", imageIsDeployed(IMAGE_RHEL));
	}

}