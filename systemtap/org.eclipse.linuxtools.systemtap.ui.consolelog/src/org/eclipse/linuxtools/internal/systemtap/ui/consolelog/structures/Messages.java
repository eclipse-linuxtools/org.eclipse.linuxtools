/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.structures;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    public static String ConsoleStreamDaemon_errorWritingToConsole;
    public static String RemoteScriptOptions_invalidArguments;
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.consolelog.structures.messages"; //$NON-NLS-1$
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
