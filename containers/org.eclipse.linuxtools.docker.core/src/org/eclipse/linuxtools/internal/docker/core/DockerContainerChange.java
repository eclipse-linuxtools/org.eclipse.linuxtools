/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat.
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
