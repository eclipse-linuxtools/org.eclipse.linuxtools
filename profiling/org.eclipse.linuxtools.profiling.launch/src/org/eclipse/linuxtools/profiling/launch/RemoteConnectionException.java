/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
