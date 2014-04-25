/*******************************************************************************
 * Copyright (c) 2010, 2011 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *    Red Hat Inc. - modified to be shared by remote tools
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.launch;

import org.eclipse.osgi.util.NLS;

/**
 * @since 1.1
 */
public class RemoteMessages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.profiling.launch.remoteMessages"; //$NON-NLS-1$
    public static String RemoteConnection_failed;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, RemoteMessages.class);
    }

    private RemoteMessages() {
    }
}
