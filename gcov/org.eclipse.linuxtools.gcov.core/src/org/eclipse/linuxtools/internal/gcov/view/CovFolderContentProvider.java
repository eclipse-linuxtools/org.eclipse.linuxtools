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
package org.eclipse.linuxtools.internal.gcov.view;


import java.util.LinkedList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;
import org.eclipse.linuxtools.internal.gcov.parser.CovManager;

public class CovFolderContentProvider implements ITreeContentProvider {

    /** Shared instance: this class is implemented as a Singleton */
    public static final CovFolderContentProvider sharedInstance = new CovFolderContentProvider();

    protected CovFolderContentProvider(){
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof TreeElement) {
            TreeElement elem = (TreeElement) parentElement;
            LinkedList<? extends TreeElement> list = elem.getChildren();
            if (list != null)
                return list.toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof TreeElement) {
            TreeElement elem = (TreeElement) element;
            return elem.getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof TreeElement) {
            TreeElement elem = (TreeElement) element;
            return elem.hasChildren();
        }
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof CovManager) {
            CovManager cvrgMnger = (CovManager)inputElement;
            return new Object[] {
                    cvrgMnger.getRootNode()
            };
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
