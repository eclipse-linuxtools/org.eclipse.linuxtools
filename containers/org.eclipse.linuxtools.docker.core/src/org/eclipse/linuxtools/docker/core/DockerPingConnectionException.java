/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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
package org.eclipse.linuxtools.docker.core;

public class DockerPingConnectionException extends DockerException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DockerPingConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public DockerPingConnectionException(String message) {
		super(message);
	}

}
