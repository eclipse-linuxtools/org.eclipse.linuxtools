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
package org.eclipse.linuxtools.docker.reddeer.condition;

import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.docker.reddeer.ui.resources.DockerConnection;
import org.jboss.reddeer.common.condition.AbstractWaitCondition;

/**
 * @author adietish@redhat.com
 */
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
