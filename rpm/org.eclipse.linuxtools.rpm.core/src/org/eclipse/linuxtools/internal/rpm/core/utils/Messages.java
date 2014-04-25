/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.core.utils;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for this package.
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.rpm.core.utils.messages"; //$NON-NLS-1$
    /** Download + name  message     */
    public static String DownloadJob_0;
    /** Message shown when trying to export a RPM project that doesn't have a specfile*/
    public static String Specfile_not_found;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
