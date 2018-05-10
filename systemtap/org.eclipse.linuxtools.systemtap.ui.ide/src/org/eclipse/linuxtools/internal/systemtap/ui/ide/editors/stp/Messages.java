/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.messages"; //$NON-NLS-1$
    public static String STPCompletionProcessor_global;
    public static String STPCompletionProcessor_probe;
    public static String STPCompletionProcessor_function;
    public static String STPMetadataSingleton_noCompletions;
    public static String NewFileHandler_NewFile;
    public static String SimpleDocumentProvider_errorCreatingFile;
    public static String SimpleDocumentProvider_incorrectURL;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
