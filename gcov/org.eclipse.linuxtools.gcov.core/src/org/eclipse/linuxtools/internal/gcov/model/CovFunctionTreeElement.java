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
package org.eclipse.linuxtools.internal.gcov.model;

import java.util.LinkedList;


public class CovFunctionTreeElement extends AbstractTreeElement {

    /**
     *
     */
    private static final long serialVersionUID = -2025221943523670378L;
    private final String sourceFilePath;
    private final long firstLnNmbr;

    public CovFunctionTreeElement(TreeElement parent, String name, String sourceFilePath,
            long firstLnNmbr, int executedLines, int instrumentedLines) {
        super(parent, name, -1, executedLines, instrumentedLines);
        this.sourceFilePath = sourceFilePath;
        this.firstLnNmbr = firstLnNmbr;
    }


    /* no children for functions*/

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public LinkedList<? extends TreeElement> getChildren() {
        return null;
    }

    @Override
    public void addChild(TreeElement child){
    }


    /* specific methods for functions*/

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public long getFirstLnNmbr() {
        return firstLnNmbr;
    }

}
