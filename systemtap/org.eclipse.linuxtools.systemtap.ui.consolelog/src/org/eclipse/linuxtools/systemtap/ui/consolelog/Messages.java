/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Initial version.
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.consolelog;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.ui.consolelog.messages"; //$NON-NLS-1$
    public static String ScpExec_errorConnectingToServer;
    public static String ScpExec_ConnTimedOut;
    public static String ScpExec_Error;
    public static String ScpExec_FileTransferFailed;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
