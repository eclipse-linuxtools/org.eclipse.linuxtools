/*******************************************************************************
 * Copyright (c) 2010, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
