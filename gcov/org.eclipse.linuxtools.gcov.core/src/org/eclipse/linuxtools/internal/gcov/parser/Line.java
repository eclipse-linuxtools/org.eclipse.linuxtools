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


public class Line implements Serializable{

    /**     */
    private static final long serialVersionUID = 8804878976767948267L;
    private boolean exists = false;
    private long count = 0;

    /*Getters & setters*/

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

}
