/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics.
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
package org.eclipse.linuxtools.internal.gcov.view;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gcov.model.CovFolderTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.CovRootTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;



public class CovFileContentProvider extends CovFolderContentProvider {

    public static final CovFileContentProvider sharedInstance = new CovFileContentProvider();

    /**
     * Constructor
     */
    protected CovFileContentProvider() {
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof CovRootTreeElement root) {
            LinkedList<? extends TreeElement> ret = getElementChildrenList(root);
            return ret.toArray();
        }
        return super.getChildren(parentElement);
    }

    protected LinkedList<? extends TreeElement> getElementChildrenList(CovRootTreeElement root) {
        LinkedList<TreeElement> ret = new LinkedList<>();
        LinkedList<? extends TreeElement> list = root.getChildren();
        for (TreeElement folderlist : list) {
            LinkedList<? extends TreeElement> partialList = folderlist.getChildren();
            ret.addAll(partialList);
        }
        return ret;
    }

    @Override
    public Object getParent(Object element) {
        Object o = super.getParent(element);
        if (o instanceof CovFolderTreeElement) {
            o = super.getParent(o);
        }
        return o;
    }

}
