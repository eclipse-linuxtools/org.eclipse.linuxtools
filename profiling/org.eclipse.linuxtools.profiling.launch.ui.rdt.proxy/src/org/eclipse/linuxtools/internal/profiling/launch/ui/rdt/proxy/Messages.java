/*******************************************************************************
 * Copyright (c) 2012, 2018 IBM Corporation adn others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Corey Ashford <cjashfor@linux.vnet.ibm.com> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch.ui.rdt.proxy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.profiling.launch.ui.rdt.proxy.messages"; //$NON-NLS-1$
    public static String RDTResourceSelectorProxy_unsupported_resourceType;
    public static String RDTResourceSelectorProxy_URI_syntax_error;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
