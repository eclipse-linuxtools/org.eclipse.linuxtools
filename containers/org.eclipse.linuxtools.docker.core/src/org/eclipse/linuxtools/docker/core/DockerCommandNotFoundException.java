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

/**
 * Exception thrown when the Docker command to run was not found (eg: usually because of wrong settings)
 */
public class DockerCommandNotFoundException extends DockerException {

	private static final long serialVersionUID = -7118879033799481279L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            the error message
	 */
	public DockerCommandNotFoundException(final String message) {
		super(message);
	}


}
