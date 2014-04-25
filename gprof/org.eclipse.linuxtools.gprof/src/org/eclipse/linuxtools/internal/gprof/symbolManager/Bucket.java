/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.symbolManager;


/**
 * Bucket structure.
 * used to display bucket info relative to each symbol.
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class Bucket {

    /** Start address of this bucket */
    public final long startAddr;
    /** End address of this bucket */
    public final long endAddr;
    /** time spent in this bucket */
    public final int  time;

    /**
     * Constructor
     * @param startAddr
     * @param endAddr
     * @param time
     */
    public Bucket(long startAddr, long endAddr, int time) {
        this.startAddr = startAddr;
        this.endAddr   = endAddr;
        this.time       = time;
    }

}
