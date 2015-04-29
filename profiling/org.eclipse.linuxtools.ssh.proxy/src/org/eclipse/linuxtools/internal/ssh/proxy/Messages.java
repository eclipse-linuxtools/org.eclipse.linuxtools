/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.ssh.proxy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.ssh.proxy.messages"; //$NON-NLS-1$
    public static String SSHFileStore_attrFailed;
    public static String SSHFileStore_attrMonitor;
    public static String SSHFileStore_childInfoFailed;
    public static String SSHFileStore_childInfoFailedDirectory;
    public static String SSHFileStore_childInfoMonitor;
    public static String SSHFileStore_childNamesFailed;
    public static String SSHFileStore_childNamesFailedDirectory;
    public static String SSHFileStore_childNamesMonitor;
    public static String SSHFileStore_childStoresFailed;
    public static String SSHFileStore_childStoresFailedDirectory;
    public static String SSHFileStore_childStoresMonitor;
    public static String SSHFileStore_getInputStreamFailed;
    public static String SSHFileStore_getInputStreamMonitor;
    public static String SSHFileStore_getOutputStreamFailed;
    public static String SSHFileStore_getOutputStreamMonitor;
    public static String SSHFileStore_mkdirFailed;
    public static String SSHFileStore_mkdirMonitor;
    public static String SSHFileStore_putInfoFailed;
    public static String SSHFileStore_putInfoMonitor;
    public static String SSHFileStore_rmFailed;
    public static String SSHFileStore_rmMonitor;
    public static String SSHCommandLauncher_malformed_env_var_string;
    public static String SSHBase_CreateSessionFailed;
    public static String SSHBase_CreateSessionCancelled;
    public static String SSHPasswordDialog_Password;
    public static String SSHPasswordDialog_Password_Title;
    public static String SSHPasswordDialog_Title;
    public static String SSHCommandLauncher_execution_problem;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
