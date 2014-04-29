/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core.model;

/**
 * Represents an OProfile sample.
 */
public class OpModelSample {
    private int count;
    private int line;
    private String file;

    public OpModelSample() {
        count = 0;
        line = 0;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setFilePath(String file) {
        this.file = file;
    }

    public int getCount() {
        return count;
    }

    public int getLine() {
        return line;
    }
    public String getFilePath() {
        return file;
    }

    @Override
    public String toString() {
        return "Line #: " + line + ", Count: " + count + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
