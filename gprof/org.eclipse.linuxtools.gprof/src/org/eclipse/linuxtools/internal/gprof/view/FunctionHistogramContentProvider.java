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

import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFile;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;


/**
 * Tree content provider on charge of displaying call graph
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class FunctionHistogramContentProvider extends FileHistogramContentProvider {
	
	public static final FunctionHistogramContentProvider sharedInstance = new FunctionHistogramContentProvider();
	
	/**
	 * Constructor
	 */
	FunctionHistogramContentProvider() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.FileHistogramContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof HistRoot) {
			HistRoot root = (HistRoot) parentElement;
			LinkedList<? extends TreeElement> ret = getFunctionChildrenList(root);
			return ret.toArray();
		}
		return super.getChildren(parentElement);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.FileHistogramContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof HistRoot) {
			HistRoot root = (HistRoot) element;
			LinkedList<? extends TreeElement> ret = getFunctionChildrenList(root);
			return !ret.isEmpty();
		}
		return super.hasChildren(element);
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
	 * @see org.eclipse.linuxtools.internal.gprof.view.FileHistogramContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		Object o = super.getParent(element);
		if (o instanceof HistFile) {
			o = super.getParent(o);
		}
		return o;
	}
	
}
