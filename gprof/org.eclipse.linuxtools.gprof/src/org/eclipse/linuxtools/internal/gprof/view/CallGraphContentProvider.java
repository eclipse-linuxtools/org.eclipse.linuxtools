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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.eclipse.linuxtools.internal.gprof.view.histogram.CGCategory;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFunction;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;

/**
 * Tree content provider on charge of displaying call graph
 *
 * HistRoot => HistFunction => CGCategory (parent/children) => CGArc
 *
 */
public class CallGraphContentProvider implements ITreeContentProvider {

    public static final CallGraphContentProvider sharedInstance = new CallGraphContentProvider();

    /**
     * Constructor
     */
    private CallGraphContentProvider() {
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof HistRoot root) {
            LinkedList<? extends TreeElement> ret = getFunctionChildrenList(root);
            return ret.toArray();
        }
        if (parentElement instanceof HistFunction function) {
            CGCategory parents  = function.getParentsFunctions();
            CGCategory children = function.getChildrenFunctions();
            if (parents == null) {
                if (children == null) return new Object[0];
                return new Object[] {children};
            } else if (children == null) {
                return new Object[] {parents};
            } else {
                return new Object[] {
                    parents,
                    children
                };
            }
        }
        if (parentElement instanceof CGCategory cat) {
            return cat.getChildren().toArray();
        }
        return null;
    }

    protected LinkedList<? extends TreeElement> getFunctionChildrenList(HistRoot root) {
        LinkedList<TreeElement> ret = new LinkedList<>();
        LinkedList<? extends TreeElement> list = root.getChildren();
        for (TreeElement histTreeElem : list) {
            LinkedList<? extends TreeElement> partialList = histTreeElem.getChildren();
            ret.addAll(partialList);
        }
        return ret;
    }

    @Override
    public TreeElement getParent(Object element) {
        if (element instanceof TreeElement) {
            TreeElement cge = (TreeElement) element;
            if (cge instanceof HistFunction) {
                return cge.getParent().getParent();
            }
            return cge.getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object parentElement) {
        if (parentElement instanceof HistRoot) {
            HistRoot root = (HistRoot) parentElement;
            LinkedList<? extends TreeElement> ret = getFunctionChildrenList(root);
            return !ret.isEmpty();
        }
        if (parentElement instanceof HistFunction) {
            HistFunction function = (HistFunction) parentElement;
            CGCategory parents  = function.getParentsFunctions();
            CGCategory children = function.getChildrenFunctions();
            return (parents != null || children != null);
        }
        if (parentElement instanceof CGCategory) {
            CGCategory cat = (CGCategory) parentElement;
            return !cat.getChildren().isEmpty();
        }
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof GmonDecoder) {
            GmonDecoder obj = (GmonDecoder) inputElement;
            HistRoot root   = obj.getRootNode();
            return new Object[] { root };
        }
        return new Object[0];
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
