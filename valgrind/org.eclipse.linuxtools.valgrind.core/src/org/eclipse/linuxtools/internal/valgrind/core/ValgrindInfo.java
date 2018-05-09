/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Rafael M Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
 *    Alena Laskavaia - javadoc comments and cleanup
 *******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

/**
 * Valgrind info message
 */
public class ValgrindInfo extends AbstractValgrindMessage {

    /**
     * Constructor
     * @param parent - parent message
     * @param text - message test cannot be null
     * @param launch - launch object can be null
     */
    public ValgrindInfo(IValgrindMessage parent, String text, ILaunch launch) {
        super(parent, text, launch);
    }

}
