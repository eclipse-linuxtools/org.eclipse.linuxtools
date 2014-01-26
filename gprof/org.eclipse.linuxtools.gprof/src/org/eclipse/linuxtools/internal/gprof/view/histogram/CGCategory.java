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
package org.eclipse.linuxtools.internal.gprof.view.histogram;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gprof.symbolManager.CallGraphArc;

/**
 * Tree node displaying "parents" or "children". Used to distinguish input arcs from output arcs in viewer
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CGCategory extends AbstractTreeElement {

    public final static String PARENTS = "parents"; //$NON-NLS-1$
    public final static String CHILDREN = "children"; //$NON-NLS-1$

    public final String category;
    private final LinkedList<TreeElement> children = new LinkedList<>();

    /**
     * Constructor
     *
     * @param parent
     *            the parent of this tree node
     * @param category
     *            the category (one of {@link #PARENTS}, {@link #CHILDREN} )
     * @param list
     *            the children (or parents) of the function
     */
    public CGCategory(HistFunction parent, String category, LinkedList<CallGraphArc> list) {
        super(parent);
        this.category = category;
        for (CallGraphArc arc : list) {
            children.addFirst(new CGArc(this, arc));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement#getChildren()
     */
    @Override
    public LinkedList<? extends TreeElement> getChildren() {
        return children;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement#getName()
     */
    @Override
    public String getName() {
        return category;
    }
}
