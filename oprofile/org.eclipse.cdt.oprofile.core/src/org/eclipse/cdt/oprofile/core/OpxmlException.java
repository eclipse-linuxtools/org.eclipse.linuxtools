/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * An exception thrown by any of the IOpxmlProvider functions
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class OpxmlException extends CoreException {

	/**
	 * Constructor
	 * @param status <code>IStatus</code> for the exception
	 */
	public OpxmlException(IStatus status) {
		super(status);
	}
}
