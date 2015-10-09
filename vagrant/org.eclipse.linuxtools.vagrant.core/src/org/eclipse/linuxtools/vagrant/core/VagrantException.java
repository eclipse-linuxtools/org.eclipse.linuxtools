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
