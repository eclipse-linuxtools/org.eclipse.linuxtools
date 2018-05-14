/*******************************************************************************
 * (C) Copyright 2010, 2018 IBM Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
