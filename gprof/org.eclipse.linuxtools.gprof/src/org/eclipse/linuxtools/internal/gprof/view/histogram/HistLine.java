/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.view.histogram;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gprof.symbolManager.Bucket;


/**
 * Tree node corresponding to a line
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistLine extends AbstractTreeElement {

    public final int line;
    private final LinkedList<HistBucket> children = new LinkedList<>();

    /**
     * Constructor
     * @param parent
     * @param lineNumber
     */
    public HistLine(HistFunction parent, int lineNumber) {
        super(parent);
        this.line = lineNumber;
    }

    void addBucket(Bucket b) {
        this.children.add(new HistBucket(this,b));
    }

    @Override
    public LinkedList<? extends TreeElement> getChildren() {
        return this.children;
    }

    @Override
    public int getCalls() {
        return -1;
    }

    @Override
    public String getName() {
        String functionName = getParent().getName();
        return functionName + " (" + getParent().getParent().getName() + ":" + this.line + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int getSamples() {
        int ret = 0;
        for (HistBucket b : children) {
            ret += b.getSamples();
        }
        return ret;
    }

    @Override
    public int getSourceLine() {
        return this.line;
    }

    @Override
    public String getSourcePath() {
        return getParent().getParent().getSourcePath();
    }

}
