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
package org.eclipse.linuxtools.docker.core;

public class DockerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String JSON_MESSAGE_PREFIX = "{\"message\":\""; //$NON-NLS-1$
	private String JSON_MESSAGE_SUFFIX = "\"}"; //$NON-NLS-1$

	public DockerException(final String message) {
		super(message);
	}

	public DockerException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DockerException(final Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		String s = super.getMessage();
		// Bug 499917 - temporarily massage any message we get back from docker
		// client if it is in JSON format. This code can be removed once docker
		// client has fixed itself to work with error messages from docker
		// 1.12.0 and beyond.
		if (s.startsWith(JSON_MESSAGE_PREFIX)) {
			s = s.substring(JSON_MESSAGE_PREFIX.length());
			if (s.endsWith(JSON_MESSAGE_SUFFIX)) {
				s = s.substring(0, s.indexOf(JSON_MESSAGE_SUFFIX));
				return s;
			}
		}
		return super.getMessage();
	}

}
