/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Alena Laskavaia - javadoc comments and cleanup
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

/**
 * Valgrind Error message object
 */
public class ValgrindError extends AbstractValgrindMessage {
    protected int pid;

    /**
     * Constructor
     * @param parent - parent message
     * @param text - message test cannot be null
     * @param launch - launch object can be null
     * @param pid - process pid
     */
    public ValgrindError(IValgrindMessage parent, String text, ILaunch launch, int pid) {
        super(parent, text, launch);
        this.pid = pid;
    }

    @Override
    public String getText() {
        return super.getText() + " [PID: " + pid + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
