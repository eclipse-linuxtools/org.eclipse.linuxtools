/*******************************************************************************
 * Copyright (c) 2012-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
