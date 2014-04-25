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
import java.util.SortedSet;
import java.util.TreeSet;

public class SourceFile implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -9182882194956475711L;
    private final String name;
    private final int index;
    private final ArrayList<Line> lines = new ArrayList<>();
    private final TreeSet<GcnoFunction> fnctns = new TreeSet<>();
    private int numLines = 1;
    private final CoverageInfo cvrge = new CoverageInfo();
    private long maxCount = -1;

    /**
     * Constructor
     */
    public SourceFile(String name, int index) {
        this.name = name;
        this.index = index;
    }


    public void accumulateLineCounts() {
        for (Line line : lines) {
            if (line.exists()) {
                cvrge.incLinesInstrumented();
                if (line.getCount() != 0) {
                    cvrge.incLinesExecuted();
                }
            }
        }
    }

    public long getmaxLineCount() {
        if (maxCount < 0) {
            for (Line line : lines) {
                if (line.getCount() > maxCount) {
                    maxCount = line.getCount();
                }
            }
        }
        return maxCount;
    }

    /* getters & setters */



    public int getLinesExecuted() {
        return cvrge.getLinesExecuted();
    }

    public int getLinesInstrumented() {
        return cvrge.getLinesInstrumented();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public SortedSet<GcnoFunction> getFnctns() {
        return fnctns;
    }

    public void addFnctn(GcnoFunction fnctn) {
        this.fnctns.add(fnctn);
    }

    public int getNumLines() {
        return numLines;
    }

    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }

    public int getIndex() {
        return index;
    }

    public void createLines() {
        int n = getNumLines();
        lines.ensureCapacity(n);
        for (int j = 0; j < n; j++) {
            lines.add(new Line());
        }
    }


}
