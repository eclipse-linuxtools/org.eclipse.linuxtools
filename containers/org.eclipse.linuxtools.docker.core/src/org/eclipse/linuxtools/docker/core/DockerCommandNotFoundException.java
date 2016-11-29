/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
