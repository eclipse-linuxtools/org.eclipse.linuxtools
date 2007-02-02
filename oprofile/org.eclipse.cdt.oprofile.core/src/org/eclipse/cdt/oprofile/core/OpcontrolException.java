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
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class OpcontrolException extends CoreException {
	public OpcontrolException(IStatus status) {
		super(status);
	}
}
