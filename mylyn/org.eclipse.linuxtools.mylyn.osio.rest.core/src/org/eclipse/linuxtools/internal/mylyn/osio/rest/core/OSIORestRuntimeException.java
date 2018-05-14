/*******************************************************************************
 * Copyright (c) 2016, 2018 Frank Becker and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

public class OSIORestRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 5028038807086982720L;

	public OSIORestRuntimeException() {
	}

	public OSIORestRuntimeException(String message) {
		super(message);
	}

	public OSIORestRuntimeException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	public OSIORestRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
