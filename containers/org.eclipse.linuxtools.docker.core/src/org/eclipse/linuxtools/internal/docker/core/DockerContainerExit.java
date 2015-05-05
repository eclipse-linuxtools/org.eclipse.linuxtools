/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerContainerExit;

public class DockerContainerExit implements IDockerContainerExit {

	private Integer statusCode;

	public DockerContainerExit(Integer statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public Integer statusCode() {
		return statusCode;
	}

}
