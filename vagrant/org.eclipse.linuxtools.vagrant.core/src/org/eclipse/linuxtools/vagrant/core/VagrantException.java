/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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
package org.eclipse.linuxtools.vagrant.core;

public class VagrantException extends Exception {

	private static final long serialVersionUID = 1L;

	public VagrantException(final String message) {
		super(message);
	}

	public VagrantException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public VagrantException(final Throwable cause) {
		super(cause);
	}

}
