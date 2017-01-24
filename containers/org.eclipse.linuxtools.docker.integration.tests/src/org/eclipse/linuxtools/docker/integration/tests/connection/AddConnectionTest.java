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

package org.eclipse.linuxtools.docker.integration.tests.connection;

import org.eclipse.linuxtools.docker.integration.tests.AbstractDockerBotTest;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 */
public class AddConnectionTest extends AbstractDockerBotTest {

	@Before
	public void setUp() {
		deleteAllConnections();
	}

	@Test
	public void testAddConnection() {
		createConnection();
	}
}
