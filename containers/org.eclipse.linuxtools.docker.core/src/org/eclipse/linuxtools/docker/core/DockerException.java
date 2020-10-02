/*******************************************************************************
 * Copyright (c) 2014, 2020 Red Hat and others.
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

import org.mandas.docker.client.exceptions.DockerRequestException;

public class DockerException extends Exception {

	private static final long serialVersionUID = 1L;

	private static String JSON_MESSAGE_PREFIX = "{\"message\":\""; //$NON-NLS-1$
	private static String JSON_MESSAGE_SUFFIX = "\"}"; //$NON-NLS-1$

	public DockerException(final String message) {
		super(message);
	}

	public DockerException(final String message, final Throwable cause) {
		super(calculateMessage(message, cause), cause);
	}

	public DockerException(final Throwable cause) {
		super(calculateMessage(null, cause), cause);
	}

	static private String calculateMessage(final String message, final Throwable cause) {
		Throwable dre = cause;
		// Search for DockerRequestException
		while (dre != null && !(dre instanceof DockerRequestException)) {
			dre = dre.getCause();
		}

		// Handle DockerRequestException
		if (dre != null) {
			// Bug 499917 - temporarily massage any message we get back from
			// docker client if it is in JSON format. This code can be removed
			// once docker client has fixed itself to work with error messages
			// from docker 1.12.0 and beyond.
			DockerRequestException re = (DockerRequestException) dre;
			String s = re.getResponseBody();
			if (s.startsWith(JSON_MESSAGE_PREFIX)) {
				s = s.substring(JSON_MESSAGE_PREFIX.length());
				s = s.replaceAll(JSON_MESSAGE_SUFFIX, ""); //$NON-NLS-1$
				if (message != null) {
					return message + "; " + s;
				} else {
					return s;
				}
			}
		}

		// As it's not possible to select the super-constructor Throwable's
		// behavior must be simulated
		if ((message == null) && (cause != null)) {
			return cause.toString();
		} else {
			return message;
		}
	}

}
