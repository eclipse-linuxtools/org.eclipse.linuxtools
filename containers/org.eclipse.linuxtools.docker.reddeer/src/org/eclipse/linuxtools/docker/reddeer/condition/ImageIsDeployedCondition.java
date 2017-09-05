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

import org.apache.commons.lang.StringUtils;
import org.eclipse.linuxtools.docker.reddeer.ui.resources.DockerConnection;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;

/**
 * @author adietish@redhat.com
 */
public class ImageIsDeployedCondition extends AbstractWaitCondition {

	private String name;
	private DockerConnection connection;
	private String tag;

	public ImageIsDeployedCondition(String name, DockerConnection connection) {
		this(name, null, connection);
	}

	public ImageIsDeployedCondition(String name, String tag, DockerConnection connection) {
		assertNotNull(this.name = name);
		assertNotNull(this.connection = connection);
		this.tag = tag;
	}

	@Override
	public boolean test() {
		if (StringUtils.isBlank(tag)) {
			return connection.imageIsDeployed(name);
		} else {
			return connection.getImage(name, tag) != null;
		}
	}
}
