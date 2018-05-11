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
import org.eclipse.linuxtools.internal.gprof.view.histogram.AbstractTreeElement;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;

/**
 * Tree content provider on charge of displaying call graph
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class FileHistogramContentProvider implements ITreeContentProvider {

    public static final FileHistogramContentProvider sharedInstance = new FileHistogramContentProvider();

    /**
     * Constructor
     */
    FileHistogramContentProvider() {
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof AbstractTreeElement) {
            AbstractTreeElement elem = (AbstractTreeElement) parentElement;
            LinkedList<? extends TreeElement> list = elem.getChildren();
            return list.toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof AbstractTreeElement) {
            AbstractTreeElement elem = (AbstractTreeElement) element;
            return elem.getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof AbstractTreeElement) {
            AbstractTreeElement elem = (AbstractTreeElement) element;
            return elem.hasChildren() && !elem.getChildren().isEmpty();
        }
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof GmonDecoder) {
            GmonDecoder obj = (GmonDecoder) inputElement;
            HistRoot root = obj.getRootNode();
            return new Object[] {
                    root
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
