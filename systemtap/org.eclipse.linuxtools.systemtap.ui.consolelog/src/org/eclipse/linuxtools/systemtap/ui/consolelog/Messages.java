/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
