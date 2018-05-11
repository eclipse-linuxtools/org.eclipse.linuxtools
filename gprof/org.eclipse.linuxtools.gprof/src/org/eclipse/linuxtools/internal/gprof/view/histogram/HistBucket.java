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
 * Tree Item displaying a bucket.
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class HistBucket extends AbstractTreeElement {

    public final Bucket bucket;

    /**
     * Constructor
     * @param parent the parent of this tree node
     * @param b the object to display in the tree
     */
    public HistBucket(HistLine parent, Bucket b) {
        super(parent);
        this.bucket = b;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public LinkedList<? extends TreeElement> getChildren() {
        return null;
    }

    @Override
    public int getCalls() {
        return -1;
    }

    @Override
    public String getName() {
        return "0x" + Long.toHexString(bucket.startAddr); //$NON-NLS-1$
    }

    @Override
    public int getSamples() {
        return bucket.time;
    }

}
