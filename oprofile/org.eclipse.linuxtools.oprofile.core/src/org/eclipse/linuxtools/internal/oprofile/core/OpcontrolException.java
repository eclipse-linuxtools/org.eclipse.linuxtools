/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
/**
* OpcontrolException class represents a failure
* It extends super class CoreExceptions which contains a status object describing
* the cause of the exception.
*/
public class OpcontrolException extends CoreException {
    //without this there's a warning..
    private static final long serialVersionUID = 8508930482724912901L;

    /**
     * Constructor to this class
     * @param status The status object of this exception
     */
    public OpcontrolException(IStatus status) {
        super(status);
    }
}
