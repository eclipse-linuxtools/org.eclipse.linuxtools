/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

public class OSIORestException extends Exception {

	private static final long serialVersionUID = 1L;

	public OSIORestException() {
	}

	public OSIORestException(String message) {
		super(message);
	}

	public OSIORestException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	public OSIORestException(String message, Throwable cause) {
		super(message, cause);
	}
}
