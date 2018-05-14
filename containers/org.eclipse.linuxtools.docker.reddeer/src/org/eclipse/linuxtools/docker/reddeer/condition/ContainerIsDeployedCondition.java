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
package org.eclipse.linuxtools.docker.reddeer.condition;

import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.docker.reddeer.ui.resources.DockerConnection;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;

public class ContainerIsDeployedCondition extends AbstractWaitCondition {

	private String name;
	private DockerConnection connection;

	public ContainerIsDeployedCondition(String name, DockerConnection connection) {
		assertNotNull(this.name = name);
		assertNotNull(this.connection = connection);
	}

	@Override
	public boolean test() {
		connection.refresh();
		return connection.getContainer(name) != null;
	}
}
