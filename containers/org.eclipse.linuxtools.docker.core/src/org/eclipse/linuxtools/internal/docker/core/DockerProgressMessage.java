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

import org.eclipse.linuxtools.docker.core.IDockerProgressDetail;
import org.eclipse.linuxtools.docker.core.IDockerProgressMessage;

public class DockerProgressMessage implements IDockerProgressMessage {

	private String id;
	private String status;
	private String stream;
	private String error;
	private String progress;
	private IDockerProgressDetail progressDetail;

	public DockerProgressMessage(String id, String status, String stream,
			String error, String progress, IDockerProgressDetail progressDetail) {
		this.id = id;
		this.status = status;
		this.stream = stream;
		this.error = error;
		this.progress = progress;
		this.progressDetail = progressDetail;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String status() {
		return status;
	}

	@Override
	public String stream() {
		return stream;
	}

	@Override
	public String error() {
		return error;
	}

	@Override
	public String progress() {
		return progress;
	}

	@Override
	public IDockerProgressDetail progressDetail() {
		return progressDetail;
	}

	@Override
	public String toString() {
		return "Message: id=" + id() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"status=" + status() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"stream=" + stream() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"error=" + error() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"progress=" + progress() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"progressDetail=" + progressDetail(); //$NON-NLS-1$
	}

}
