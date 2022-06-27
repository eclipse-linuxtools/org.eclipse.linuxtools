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
 *     Mathema - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.docker.core;

import org.eclipse.linuxtools.internal.docker.core.DockerMessages;

/**
 * The operation was cancelled by the user
 *
 * @since 5.8
 */
public class DockerOperationCancelledException extends DockerException {

	public DockerOperationCancelledException(String message, Throwable cause) {
		super(message, cause);
	}

	public DockerOperationCancelledException(String message) {
		super(message);
	}

	public DockerOperationCancelledException(Throwable e) {
		super(e);
	}

	public DockerOperationCancelledException() {
		super(DockerMessages
				.getString("DockerOperationCancelledException_Message"));
	}

	private static final long serialVersionUID = -719975300488986809L;
}
