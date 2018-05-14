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

public class PMLineRef extends TreeParent {
    protected int ln;

    public PMLineRef(int lineNum, float percent) {
        super(""+lineNum, percent); //$NON-NLS-1$
        ln = lineNum;
    }

    public void addPercent(Float addpc) {
        setPercent(getPercent() + addpc);
    }

    @Override
    public String toString() {
        return getPercent() + "% (" + getFormattedSamples() + " samples) on line " + ln; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
