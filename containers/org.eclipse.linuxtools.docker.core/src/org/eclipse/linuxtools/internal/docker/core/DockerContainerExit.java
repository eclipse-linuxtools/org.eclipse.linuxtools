/*******************************************************************************
 * Copyright (c) 2014, 2020 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerContainerExit;

public class DockerContainerExit implements IDockerContainerExit {

	private Long statusCode;

	public DockerContainerExit(Long long1) {
		this.statusCode = long1;
	}

	@Override
	public Long statusCode() {
		return statusCode;
	}

}
