/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import java.io.OutputStream;

public class StringOutputStream extends OutputStream {
	private StringBuilder str = new StringBuilder();

    @Override
    public String toString() {
        return str.toString();
    }

    @Override
    public void write(int b) {
        str.append((char)b);
    }
}
