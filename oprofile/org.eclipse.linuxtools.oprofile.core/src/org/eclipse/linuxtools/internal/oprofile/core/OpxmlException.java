/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.oprofile.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * An exception thrown by any of the IOpxmlProvider functions
 */
public class OpxmlException extends CoreException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2788304536155025911L;

	/**
	 * Constructor
	 * @param status <code>IStatus</code> for the exception
	 */
	public OpxmlException(IStatus status) {
		super(status);
	}
}
