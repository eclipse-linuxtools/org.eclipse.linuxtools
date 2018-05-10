/*******************************************************************************
 * Copyright (c) 2013, 2018 Red Hat, Inc. and others.
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
package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.messages"; //$NON-NLS-1$
    public static String SharedParser_name;
    public static String FunctionParser_name;
    public static String ProbeParser_name;
    public static String ProbeParser_errorInitializingStaticProbes;
    public static String ProbeParser_staticProbes;
    public static String ProbeParser_aliasProbes;
    public static String TapsetParser_ErrorCannotRunStap;
    public static String TapsetParser_ErrorCannotRunRemoteStap;
    public static String TapsetParser_ErrorInvalidTapsetTree;
    public static String TapsetParser_RemoteCredentialErrorTitle;
    public static String TapsetParser_RemoteCredentialErrorMessage;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
