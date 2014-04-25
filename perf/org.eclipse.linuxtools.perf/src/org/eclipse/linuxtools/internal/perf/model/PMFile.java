/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.model;

public class PMFile extends TreeParent {
    private String path;

    public PMFile(String fileName) {
        super(fileName, 0);
        path = fileName;
    }

    public String getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        String prefix = ""; //$NON-NLS-1$
        if (getPercent() != -1) {
            prefix = getPercent() + "% in "; //$NON-NLS-1$
        }
        return prefix + path;
    }
}
