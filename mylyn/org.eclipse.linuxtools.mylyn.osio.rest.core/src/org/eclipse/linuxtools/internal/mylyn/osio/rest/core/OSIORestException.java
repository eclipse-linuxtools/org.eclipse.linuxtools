/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
