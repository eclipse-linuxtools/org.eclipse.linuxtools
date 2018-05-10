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
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.parser;

import java.io.Serializable;
import java.util.ArrayList;

public class Folder implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5155033391199109661L;
    private final String path;
    private final ArrayList<SourceFile> srcFiles = new ArrayList<>();
    private int numLines = 0;
    private int linesInstrumented = 0;
    private int linesExecuted = 0;

    /**
     * Constructor
     */
    public Folder(String path) {
        this.path = path;
    }


    public void accumulateSourcesCounts(){
        for (SourceFile srcFile: srcFiles) {
            numLines += srcFile.getNumLines();
            linesInstrumented += srcFile.getLinesInstrumented();
            linesExecuted += srcFile.getLinesExecuted();
        }
    }

    public String getPath() {
        return path;
    }

    public ArrayList<SourceFile> getSrcFiles() {
        return srcFiles;
    }

    public void addSrcFiles(SourceFile srcFile) {
        this.srcFiles.add(srcFile);
    }

    public int getNumLines() {
        return numLines;
    }
    public int getLinesExecuted() {
        return linesExecuted;
    }
    public int getLinesInstrumented() {
        return linesInstrumented;
    }
}
