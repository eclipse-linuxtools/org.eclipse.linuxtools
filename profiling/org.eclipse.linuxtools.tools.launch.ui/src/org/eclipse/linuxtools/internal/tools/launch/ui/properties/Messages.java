/*******************************************************************************
 * Copyright (c) 2011, 20148 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *    Rafael Peria de Sene <rpsene@br.ibm.com>
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tools.launch.ui.properties;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tools.launch.ui.properties.messages"; //$NON-NLS-1$
    public static String LINUXTOOLS_PATH;
    public static String LINUXTOOLS_PATH_COMBO;
    public static String LINUXTOOLS_PATH_CUSTOM;
    public static String LINUXTOOLS_PATH_CUSTOM_TOOLTIP;
    public static String LINUXTOOLS_PATH_SYSTEM_ENV;
    public static String LINUXTOOLS_PATH_TOOLTIP;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
