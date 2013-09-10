/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ListTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object parentElement) {

		ArrayList<Object> output = new ArrayList<Object>();

		if (parentElement instanceof ICContainer) {
			try {
				Object[] list =((ICContainer) parentElement).getChildren();
				for (Object item : list) {
					if (item instanceof ICContainer) {
						if (checkForValidChildren((ICContainer) item))
							output.add(item);
					} else if (item instanceof ICElement) {
						ICElement el = (ICElement) item;
						if (SystemTapLaunchShortcut.validElement(el))
							output.add(el);
					}


				}

				return output.toArray();
			} catch (CModelException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * A container is valid if any of its children are valid c/cpp elements
	 * or if it contains another container for which the above holds
	 * @param cont
	 * @return
	 */
	private boolean checkForValidChildren(ICContainer cont) {
		try {
			for (ICElement child : cont.getChildren()) {

				if ((child != null)
						&& SystemTapLaunchShortcut.validElement(child))
					return true;
				if ((child instanceof ICContainer)
						&& checkForValidChildren((ICContainer) child)) {
					return true;
				}
			}
		} catch (CModelException e) {
			e.printStackTrace();
		}

		return false;

	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ICElement) {
			return ((ICElement)element).getAncestor(ICElement.C_CCONTAINER);
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ICContainer) {
			try {
				if (((ICContainer) element).getChildren().length > 0)
					return true;
			} catch (CModelException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			for (Object element : (List<?>) inputElement)
				if (element instanceof ICContainer)
					try {
						ICElement[] array = ((ICContainer) element).getChildren();
						ArrayList<ICElement> output = new ArrayList<ICElement>();

						for (ICElement item : array) {
							if ((item instanceof ICContainer)
									&& checkForValidChildren((ICContainer) item)) {
								output.add(item);
							}

							if (SystemTapLaunchShortcut.validElement(item)) {
								output.add(item);
							}
						}
						return output.toArray();
					} catch (CModelException e) {
						e.printStackTrace();
					}
		}
		return null;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}


	public Object[] findElements(Object inputElement) {
		ArrayList<Object> output = new ArrayList<Object>();

		if (inputElement instanceof List) {
			for (Object element : (List<?>) inputElement) {
				Object[] list = (getChildren(element));
				for (Object o : list) {
					output.add(o);
				}
			}
		}
		return output.toArray();
	}
}
