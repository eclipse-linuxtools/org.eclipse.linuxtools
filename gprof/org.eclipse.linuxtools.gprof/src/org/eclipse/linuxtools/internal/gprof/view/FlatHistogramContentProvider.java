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
package org.eclipse.linuxtools.internal.gprof.view;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFunction;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;


/**
 * Tree content provider on charge of displaying call graph
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public final class FlatHistogramContentProvider extends FunctionHistogramContentProvider {

    public static final FlatHistogramContentProvider sharedInstance = new FlatHistogramContentProvider();

    /**
     * Constructor
     */
    private FlatHistogramContentProvider() {
    }

    @Override
    protected LinkedList<? extends TreeElement> getFunctionChildrenList(HistRoot root) {
        LinkedList<? extends TreeElement> list = super.getFunctionChildrenList(root);
        LinkedList<TreeElement> ret = new LinkedList<>();
        for (TreeElement histTreeElem : list) {
            LinkedList<? extends TreeElement> partialList = histTreeElem.getChildren();
            ret.addAll(partialList);
        }
        return ret;
    }

    @Override
    public Object getParent(Object element) {
        Object o = super.getParent(element);
        if (o instanceof HistFunction) {
            o = super.getParent(o);
        }
        return o;
    }

}
