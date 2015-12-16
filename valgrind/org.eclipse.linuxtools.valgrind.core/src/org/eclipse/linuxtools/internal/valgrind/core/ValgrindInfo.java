/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
