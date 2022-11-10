/*******************************************************************************
 * Copyright (c) 2022 Mathema
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mathema - Initial Contribution that this class was copied from
 *     Red Hat Inc. - modification to DockerCertificateException
 *******************************************************************************/

package org.eclipse.linuxtools.docker.core;

import org.eclipse.linuxtools.internal.docker.core.DockerMessages;

/**
 * An issue with the Docker Certificate.
 *
 * @since 5.9
 */
public class DockerCertificateException extends DockerException {

	public DockerCertificateException(String message, Throwable cause) {
		super(message, cause);
	}

	public DockerCertificateException(String message) {
		super(message);
	}

	public DockerCertificateException(Throwable e) {
		super(e);
	}

	public DockerCertificateException() {
		super(DockerMessages
				.getString("DockerOperationCancelledException_Message"));
	}

	private static final long serialVersionUID = -719975300488986809L;
}
