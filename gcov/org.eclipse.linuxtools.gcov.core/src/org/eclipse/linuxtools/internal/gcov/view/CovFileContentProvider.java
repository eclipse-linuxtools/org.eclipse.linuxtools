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
		if (parentElement instanceof CovRootTreeElement) {
			CovRootTreeElement root = (CovRootTreeElement) parentElement;
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
