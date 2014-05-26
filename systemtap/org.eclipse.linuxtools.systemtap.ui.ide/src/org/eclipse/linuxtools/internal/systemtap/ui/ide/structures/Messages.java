/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.messages"; //$NON-NLS-1$
    public static String ProbeParser_errorInitializingStaticProbes;
    public static String ProbeParser_staticProbes;
    public static String ProbeParser_aliasProbes;
    public static String ProbeParser_illegalArgMessage;
    public static String TapsetParser_CannotRunStapMessage;
    public static String TapsetParser_CannotRunStapTitle;
    public static String TapsetParser_ErrorRunningSystemtap;
    public static String TapsetParser_RemoteCredentialErrorTitle;
    public static String TapsetParser_RemoteCredentialErrorMessage;
    public static String SharedParser_NoOutput;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
