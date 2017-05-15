/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerContainerChange;

public class DockerContainerChange implements IDockerContainerChange {

	private String path;
	private ChangeKind changeKind;

	public DockerContainerChange(String path, Integer kind) {
		this.path = path;
		this.changeKind = ChangeKind.values()[kind.intValue()];
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public ChangeKind kind() {
		return changeKind;
	}

}
