/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael M Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

public class ValgrindInfo extends AbstractValgrindMessage {

    public ValgrindInfo(IValgrindMessage parent, String text, ILaunch launch) {
        super(parent, text, launch);
    }

}
