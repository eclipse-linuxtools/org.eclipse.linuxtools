/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;

/**
 * @since 1.1
 */
public class RemoteConnectionException extends CoreException {

    private static final long serialVersionUID = 1L;

    public RemoteConnectionException(String message, Throwable t) {
        super(new Status(IStatus.ERROR, ProfileLaunchPlugin.PLUGIN_ID, message, t));
    }

}
