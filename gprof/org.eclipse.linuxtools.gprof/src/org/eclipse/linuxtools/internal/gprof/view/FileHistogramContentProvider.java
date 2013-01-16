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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
	    if (parentElement instanceof AbstractTreeElement) {
	        AbstractTreeElement elem = (AbstractTreeElement) parentElement;
	        LinkedList<? extends TreeElement> list = elem.getChildren();
	        return list.toArray();
	    }
	    return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof AbstractTreeElement) {
			AbstractTreeElement elem = (AbstractTreeElement) element;
			return elem.getParent();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
	    if (element instanceof AbstractTreeElement) {
	        AbstractTreeElement elem = (AbstractTreeElement) element;
	        return elem.hasChildren() && !elem.getChildren().isEmpty();
	    }
	    return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
