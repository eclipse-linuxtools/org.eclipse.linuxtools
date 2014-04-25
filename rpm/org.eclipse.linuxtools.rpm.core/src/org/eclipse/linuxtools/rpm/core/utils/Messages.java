/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.utils;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the o.e.linuxtools.rpm.core.utils package.
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.rpm.core.utils.messages"; //$NON-NLS-1$
    /**
     * Message when runCommand returns non zero code.
     */
    public static String Utils_NON_ZERO_RETURN_CODE;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
