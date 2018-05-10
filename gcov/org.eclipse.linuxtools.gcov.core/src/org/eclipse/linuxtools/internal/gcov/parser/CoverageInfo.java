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

public class CoverageInfo implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -6067335353999481231L;

    private int linesInstrumented = 0;
    private int linesExecuted = 0;

    /**
     * Constructor
     */
    public CoverageInfo() {

    }


    /*getters & setters */

    public int getLinesInstrumented() {
        return linesInstrumented;
    }

    public int getLinesExecuted() {
        return linesExecuted;
    }

    public void incLinesInstrumented(){
        this.linesInstrumented++;
    }

    public void incLinesExecuted(){
        this.linesExecuted++;
    }


}
