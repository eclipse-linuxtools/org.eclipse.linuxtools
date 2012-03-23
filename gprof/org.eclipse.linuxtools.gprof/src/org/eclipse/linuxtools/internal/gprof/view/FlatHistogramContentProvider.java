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

import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFunction;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;


/**
 * Tree content provider on charge of displaying call graph
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class FlatHistogramContentProvider extends FunctionHistogramContentProvider {
	
	public static final FlatHistogramContentProvider sharedInstance = new FlatHistogramContentProvider();
	
	/**
	 * Constructor
	 */
	private FlatHistogramContentProvider() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.FunctionHistogramContentProvider#getFunctionChildrenList(org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot)
	 */
	@Override
	protected LinkedList<? extends TreeElement> getFunctionChildrenList(HistRoot root) {
		LinkedList<? extends TreeElement> list = super.getFunctionChildrenList(root);
		LinkedList<TreeElement> ret = new LinkedList<TreeElement>();
		for (TreeElement histTreeElem : list) {
			LinkedList<? extends TreeElement> partialList = histTreeElem.getChildren();
			ret.addAll(partialList);
		}
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.internal.gprof.view.FunctionHistogramContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		Object o = super.getParent(element);
		if (o instanceof HistFunction) {
			o = super.getParent(o);
		}
		return o;
	}
	
}
