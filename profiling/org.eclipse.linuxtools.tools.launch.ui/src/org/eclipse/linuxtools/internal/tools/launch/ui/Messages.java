/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tools.launch.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = Activator.PLUGIN_ID + ".messages"; //$NON-NLS-1$
    public static String LINUXTOOLS_PATH;
    public static String LINUXTOOLS_PATH_COMBO;
    public static String LINUXTOOLS_PATH_TOOLTIP;
    public static String LINUXTOOLS_PATH_SYSTEM_ENV;
    public static String LINUXTOOLS_PATH_CUSTOM;
    public static String LINUXTOOLS_PATH_CUSTOM_TOOLTIP;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
