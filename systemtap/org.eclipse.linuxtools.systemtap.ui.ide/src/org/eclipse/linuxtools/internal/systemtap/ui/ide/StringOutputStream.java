/*******************************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import java.io.OutputStream;

public class StringOutputStream extends OutputStream {
    private StringBuffer str = new StringBuffer();

    @Override
    public String toString() {
        return str.toString();
    }

    @Override
    public void write(int b) {
        str.append((char)b);
    }
}
