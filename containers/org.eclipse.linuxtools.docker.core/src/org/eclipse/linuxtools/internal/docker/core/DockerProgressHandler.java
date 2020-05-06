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

import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;

import org.mandas.docker.client.ProgressHandler;
import org.mandas.docker.client.exceptions.DockerException;
import org.mandas.docker.client.messages.ProgressDetail;
import org.mandas.docker.client.messages.ProgressMessage;

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
