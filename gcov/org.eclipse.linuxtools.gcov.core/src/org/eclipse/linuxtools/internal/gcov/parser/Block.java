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
import java.util.ArrayList;

public class Block implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -7665287885679756014L;
    private final ArrayList<Arc> entryArcs = new ArrayList<>();
    private final ArrayList<Arc> exitArcs = new ArrayList<>();
    private final long flag;
    private long numSuccs = 0;
    private long  numPreds = 0;
    private long  count = 0;
    private boolean isCallSite = false;// Does the call
    private boolean isCallReturn = false; // Is the return
    private boolean isNonLocalReturn = false;
    private boolean validChain = false;
    private boolean invalidChain = false;
    private boolean countValid = false;
    private final BlkLine blkline = new BlkLine();

    /**
     * Constructor
     */
    public Block(long flag) {
        this.flag = flag;
    }


    /* getters & setters */
    public long getFlag() {
        return flag;
    }

    public ArrayList<Arc> getEntryArcs() {
        return entryArcs;
    }

    public ArrayList<Arc> getExitArcs() {
        return exitArcs;
    }

    public boolean isCallSite() {
        return isCallSite;
    }

    public boolean isCallReturn() {
        return isCallReturn;
    }

    public boolean isNonLocalReturn() {
        return isNonLocalReturn;
    }

    public void addEntryArcs(Arc arcEntry) {
        this.entryArcs.add(arcEntry);
    }

    public void addExitArcs(Arc arcExit) {
        this.exitArcs.add(arcExit);
    }

    public void setCallSite(boolean isCallSite) {
        this.isCallSite = isCallSite;
    }

    public void setCallReturn(boolean isCallReturn) {
        this.isCallReturn = isCallReturn;
    }

    public void setNonLocalReturn(boolean isNonLocalReturn) {
        this.isNonLocalReturn = isNonLocalReturn;
    }

    public void decNumSuccs() {
        this.numSuccs--;
    }

    public void decNumPreds() {
        this.numPreds--;
    }

    public void incNumPreds() {
        this.numPreds++;
    }

    public void incNumSuccs() {
        this.numSuccs++;
    }

    public boolean isValidChain() {
        return validChain;
    }

    public void setValidChain(boolean validChain) {
        this.validChain = validChain;
    }

    public boolean isInvalidChain() {
        return invalidChain;
    }

    public void setInvalidChain(boolean invalidChain) {
        this.invalidChain = invalidChain;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setCountValid(boolean countValid) {
        this.countValid = countValid;
    }

    public boolean isCountValid() {
        return countValid;
    }

    public long getNumSuccs() {
        return numSuccs;
    }

    public long getNumPreds() {
        return numPreds;
    }

    public void setNumSuccs(long numSuccs) {
        this.numSuccs = numSuccs;
    }

    public void setNumPreds(long numPreds) {
        this.numPreds = numPreds;
    }

    public long[] getEncoding() {
        return blkline.encoding;
    }

    public void setEncoding(long[] lineNos) {
        this.blkline.encoding = lineNos;
    }

    public int getLineNum() {
        return blkline.num;
    }

    public void setNumLine(int numline) {
        this.blkline.num = numline;
    }

    static class BlkLine implements Serializable{
        /**
         *
         */
        private static final long serialVersionUID = 2757557929188979686L;
        public long[] encoding;
        public int num;
    }
}
