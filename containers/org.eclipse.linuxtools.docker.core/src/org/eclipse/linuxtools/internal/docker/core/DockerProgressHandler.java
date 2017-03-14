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

import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;

import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ProgressDetail;
import com.spotify.docker.client.messages.ProgressMessage;

public class DockerProgressHandler implements ProgressHandler {

	private IDockerProgressHandler handler;

	public DockerProgressHandler(IDockerProgressHandler handler) {
		this.handler = handler;
	}

	@Override
	public void progress(ProgressMessage message) throws DockerException {

		DockerProgressDetail detail = null;
		ProgressDetail d = message.progressDetail();
		if (d != null) {
			detail = new DockerProgressDetail(d.current(), d.start(), d.total());
		}
		DockerProgressMessage dpmessage = new DockerProgressMessage(
				message.id(), message.status(), message.stream(),
				message.error(), message.progress(), detail);
		try {
			handler.processMessage(dpmessage);
		} catch (org.eclipse.linuxtools.docker.core.DockerException e) {
			throw new DockerException(e);
		}
	}

}
