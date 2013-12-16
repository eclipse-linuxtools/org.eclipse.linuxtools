/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.view;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gcov.model.CovFileTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.CovRootTreeElement;
import org.eclipse.linuxtools.internal.gcov.model.TreeElement;




public class CovFunctionContentProvider extends CovFileContentProvider {

	public static final CovFunctionContentProvider sharedInstance = new CovFunctionContentProvider();
	
	/**
	 * Constructor
	 */
	protected CovFunctionContentProvider() {
	}
	
	@Override
	protected LinkedList<? extends TreeElement> getElementChildrenList(CovRootTreeElement root) {
		LinkedList<? extends TreeElement> list = super.getElementChildrenList(root);
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
		if (o instanceof CovFileTreeElement) {
			o = super.getParent(o);
		}
		return o;
	}

}