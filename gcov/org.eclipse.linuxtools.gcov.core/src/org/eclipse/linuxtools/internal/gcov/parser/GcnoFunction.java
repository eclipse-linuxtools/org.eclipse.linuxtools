/*******************************************************************************
 * Copyright (c) 2009, 2021 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GcnoFunction implements Serializable, Comparable<GcnoFunction> {

    private static final long serialVersionUID = -4159055012321132651L;

    private final long ident;
    private final long cheksum;
    private final long firstLineNmbr;
    private final String name;
    private final String srcFile;
    private ArrayList<Block> functionBlocks = new ArrayList<>();
    private int numCounts = 0, numBlocks = 0;
    private final CoverageInfo cvrge = new CoverageInfo();
    private boolean hasCatch = false;

    /*
     * Array of basic blocks.  Like in GCC, the entry block is
     * at functionBlocks[0] and the exit block is at functionBlocks[1].
     */
    private static final int entryBlockIndice = 0;
    private static final int exitBlockIndice = 1;

    public GcnoFunction(long fnctnIdent, long fnctnChksm, String fnctnName, String fnctnSrcFle, long fnctnFrstLnNmbr) {
        this.ident = fnctnIdent;
        this.cheksum = fnctnChksm;
        this.name = fnctnName;
        this.srcFile = fnctnSrcFle;
        this.firstLineNmbr = fnctnFrstLnNmbr;
    }

    @Override
    public int compareTo(GcnoFunction o) {
        if (getFirstLineNmbr() > o.getFirstLineNmbr()) {
            return 1;
        } else if (getFirstLineNmbr() < o.getFirstLineNmbr()) {
            return -1;
        }
        return 0;
    }

    public void addLineCounts(ArrayList<SourceFile> srcs) {
		Set<Line> linesToCalculate = new HashSet<>();
        for (int i = 0; i != numBlocks; i++) {
            Block blk = functionBlocks.get(i);
            SourceFile fileSrc = null;

            long[] enc = blk.getEncoding();
            for (int j = 0, k = 0; j != blk.getLineNum(); j++, k++) {
                if (enc[k] == 0) {
                    int srcn = (int) enc[++k];
                    for (SourceFile sf : srcs) {
                        if (sf.getIndex() == srcn) {
                            fileSrc = sf;
                            break;
                        }
                    }
                    j++;
                } else if ((fileSrc != null) && enc[k] < fileSrc.getLines().size()) {
                    Line line = fileSrc.getLines().get((int) enc[k]);
                    if (!line.exists()) {
                        cvrge.incLinesInstrumented();
                    }
                    if ((line.getCount() == 0) && (blk.getCount() != 0)) {
                        cvrge.incLinesExecuted();
                    }
                    line.setExists(true);
					if (line.getBlocks().size() > 1) {
						// we can't count on the blk count to be accurate (multiple blocks have same
						// line
						linesToCalculate.add(line);
						line.setCount(1); // to avoid counting it twice in execution total
					} else {
						line.setCount(line.getCount() + blk.getCount());
					}
                }
            }
        }
		for (Line line : linesToCalculate) {
			long count = 0;
			for (Block b : line.getBlocks()) {
				for (Arc arc : b.getEntryArcs()) {
					if (!line.hasBlock(arc.getSrcBlock())) {
						count += arc.getCount();
					}
				}
			}
			line.setCount(count);
		}
    }

    public void solveGraphFnctn() {
        ArrayList<Block> fnctnBlcks = this.functionBlocks;
        ArrayList<Block> validBlocks = new ArrayList<>();
        ArrayList<Block> invalidBlocks = new ArrayList<>();

        // Function should contain at least one block
        if (fnctnBlcks.size() >= 2) {
            if (fnctnBlcks.get(entryBlockIndice).getNumPreds() == 0) {
                fnctnBlcks.get(entryBlockIndice).setNumPreds(50000);
            }
            if (fnctnBlcks.get(exitBlockIndice).getNumSuccs() == 0) {
                fnctnBlcks.get(exitBlockIndice).setNumSuccs(50000);
            }
        }

        for (Block b: fnctnBlcks) {
            b.setInvalidChain(true);
            invalidBlocks.add(b);
        }

        while (!validBlocks.isEmpty() || !invalidBlocks.isEmpty()) {

            if (!invalidBlocks.isEmpty()) {
                for (int i = invalidBlocks.size() - 1; i >= 0; i--) {
                    Block invb = invalidBlocks.get(i);
                    long total = 0;
                    invalidBlocks.remove(i);
                    invb.setInvalidChain(false);

                    if (invb.getNumPreds() != 0 && invb.getNumSuccs() != 0)
                    	continue;

                    if (invb.getNumSuccs() == 0) {
                        ArrayList<Arc> extArcs = invb.getExitArcs();
                        for (Arc arc : extArcs) {
                            total += arc.getCount();
                        }
                    }
                    // On Windows, we can end up with both numpreds and numsuccs 0 for
                    // a closing brace of a function so we need to check the entry arcs
                    // as well if we don't have a total > 0.
                    if (invb.getNumPreds() == 0 && total == 0) {
                        ArrayList<Arc> entrArcs = invb.getEntryArcs();
                        for (Arc arc : entrArcs) {
                            total += arc.getCount();
                        }
                    }

                    invb.setCount(total);
                    invb.setCountValid(true);
                    invb.setValidChain(true);
                    validBlocks.add(invb);
                }
            }
            while (!validBlocks.isEmpty()) {
                int last = validBlocks.size() - 1;
                Block vb = validBlocks.get(last);
                Arc invarc = null;
                int total = 0;
                validBlocks.remove(last);

                vb.setValidChain(false);

                if (vb.getNumSuccs() == 1) {
                    Block blcksdst;
                    total = (int) vb.getCount();

                    for (Arc extAr : vb.getExitArcs()) {
                        total -= extAr.getCount();
                        if (extAr.isCountValid() == false) {
                            invarc = extAr;
                        }
                    }
                    blcksdst = invarc.getDstnatnBlock();
                    invarc.setCountValid(true);
                    invarc.setCount(total);
                    vb.decNumSuccs();
                    blcksdst.decNumPreds();

                    if (blcksdst.isCountValid()) {
                        if (blcksdst.getNumPreds() == 1 && !blcksdst.isValidChain()) {
                            blcksdst.setValidChain(true);
                            validBlocks.add(blcksdst);
                        }
                    } else {
                        if (blcksdst.getNumPreds() == 0 && !blcksdst.isInvalidChain()) {
                            blcksdst.setInvalidChain(true);
                            invalidBlocks.add(blcksdst);
                        }
                    }
                }

                if (vb.getNumPreds() == 1) {
                    Block blcksrc;
                    total = (int) vb.getCount();
                    invarc = null;

                    for (Arc entrAr : vb.getEntryArcs()) {
						total -= entrAr.getCount(); /* total can end up negative here ?? */
                        if (!entrAr.isCountValid()) {
                            invarc = entrAr;
                        }
                    }

                    blcksrc = invarc.getSrcBlock();
                    invarc.setCountValid(true);
					invarc.setCount(total); /* temporary kludge */
                    vb.decNumPreds();
                    blcksrc.decNumSuccs();

                    if (blcksrc.isCountValid()) {
                        if (blcksrc.getNumSuccs() == 1 && !blcksrc.isValidChain()) {
                            blcksrc.setValidChain(true);
                            validBlocks.add(blcksrc);
                        }
                    } else if (blcksrc.getNumSuccs() == 0 && !blcksrc.isInvalidChain()) {
                        blcksrc.setInvalidChain(true);
                        invalidBlocks.add(blcksrc);
                    }
                }
            }
        }
    }

    /* getters & setters */

    public long getIdent() {
        return ident;
    }

    public long getCheksum() {
        return cheksum;
    }

    public String getName() {
        return name;
    }

    public String getSrcFile() {
        return srcFile;
    }

    public long getFirstLineNmbr() {
        return firstLineNmbr;
    }

    public ArrayList<Block> getFunctionBlocks() {
        return functionBlocks;
    }

    public Block getFunctionBlock(int i) {
        return functionBlocks.get(i);
    }

    public void setFunctionBlocks(ArrayList<Block> functionBlocks) {
        this.functionBlocks = functionBlocks;
    }

	public boolean hasCatch() {
		return hasCatch;
	}

    public void incNumCounts() {
        this.numCounts++;
    }

    public int getNumCounts() {
        return numCounts;
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }

    public CoverageInfo getCvrge() {
        return cvrge;
    }

	public void setHasCatch(boolean hasCatch) {
		this.hasCatch = hasCatch;
	}

}
