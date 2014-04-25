/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.parser;

import java.io.Serializable;
import java.util.List;

public class Arc implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 4104429137191407662L;

    private static final int VCOV_ARC_ON_TREE = (1 << 0);
    private static final int VCOV_ARC_FAKE = (1 << 1);
    private static final int VCOV_ARC_FALLTHROUGH = (1 << 2);

    private final Block srcBlock;
    private final Block dstnatnBlock;
    private final long flag;
    private final boolean fake;
    private final boolean onTree;
    private final boolean fallthrough;

    private long count = 0;
    private boolean countValid = false;
    private boolean isCallNonReturn = false; // Arc is for a function that abnormally returns
    private boolean isNonLoclaReturn = false; // Arc is for catch/setjump
    private boolean isUnconditionnal = false; // Is an unconditional branch.

    /**
     * Constructor
     */
    public Arc(int srcBlockIndice, int dstnatnBlockIndice, long flag, List<Block> otherArcParams) {
        this.flag = flag;
        this.dstnatnBlock = otherArcParams.get(dstnatnBlockIndice);
        this.srcBlock = otherArcParams.get(srcBlockIndice);
        this.count = 0;
        this.countValid = false;
        if ((flag & VCOV_ARC_ON_TREE) != 0) {
            onTree = true;
            fake = false;
            fallthrough = false;
        } else if ((flag & VCOV_ARC_FAKE) != 0) {
            onTree = false;
            fake = true;
            fallthrough = true;
        } else if ((flag & VCOV_ARC_FALLTHROUGH) != 0) {
            onTree = false;
            fake = false;
            fallthrough = true;
        } else {
            onTree = false;
            fake = false;
            fallthrough = false;
        }
    }

    public Block getDstnatnBlock() {
        return dstnatnBlock;
    }

    public long getFlag() {
        return flag;
    }

    public boolean isFake() {
        return fake;
    }

    public boolean isOnTree() {
        return onTree;
    }

    public boolean isFallthrough() {
        return fallthrough;
    }

    public boolean isUnconditionnal() {
        return isUnconditionnal;
    }

    public boolean isNonLoclaReturn() {
        return isNonLoclaReturn;
    }

    public boolean isCallNonReturn() {
        return isCallNonReturn;
    }

    public void setCallNonReturn(boolean isCallNonReturn) {
        this.isCallNonReturn = isCallNonReturn;
    }

    public void setNonLoclaReturn(boolean isNonLoclaReturn) {
        this.isNonLoclaReturn = isNonLoclaReturn;
    }

    public void setUnconditionnal(boolean isUnconditionnal) {
        this.isUnconditionnal = isUnconditionnal;
    }

    public Block getSrcBlock() {
        return srcBlock;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setCountValid(boolean countValid) {
        this.countValid = countValid;
    }

    public long getCount() {
        return count;
    }

    public boolean isCountValid() {
        return countValid;
    }
}
