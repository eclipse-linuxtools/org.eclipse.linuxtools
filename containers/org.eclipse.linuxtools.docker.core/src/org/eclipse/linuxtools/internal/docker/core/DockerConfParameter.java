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

import org.eclipse.linuxtools.docker.core.IDockerConfParameter;

import com.spotify.docker.client.messages.HostConfig.LxcConfParameter;

public class DockerConfParameter implements IDockerConfParameter {

	private final String key;
	private final String value;

	public DockerConfParameter(final LxcConfParameter confParameter) {
		this.key = confParameter.key();
		this.value = confParameter.value();
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (obj == null || this.getClass() != obj.getClass())
			return false;

		IDockerConfParameter that = (IDockerConfParameter) obj;

		if (this.key != null) {
			if (!this.key.equals(that.key()))
				return false;
		} else if (that.key() != null)
			return false;

		if (this.value != null) {
			if (!this.value.equals(that.value()))
				return false;
		} else if (that.value() != null)
			return false;

		return true;
	}

}
