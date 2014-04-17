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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof HistRoot) {
			HistRoot root = (HistRoot) parentElement;
			LinkedList<? extends TreeElement> ret = getFunctionChildrenList(root);
			return ret.toArray();
		}
		if (parentElement instanceof HistFunction) {
			HistFunction function = (HistFunction) parentElement;
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
		if (parentElement instanceof CGCategory) {
			CGCategory cat = (CGCategory) parentElement;
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof GmonDecoder) {
		    GmonDecoder obj = (GmonDecoder) inputElement;
	        HistRoot root   = obj.getRootNode();
	        return new Object[] { root };
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
