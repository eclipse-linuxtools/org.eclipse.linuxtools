/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * This class provides the framework for the ModuleBrowsers.  Since
 * it is abstract, it can't be implemented itself.  It does however
 * provide a number of common methods that will be needed for all of
 * the ModuleBrowsers.
 * @author Ryan Morse
 */
public abstract class GraphsView extends ViewPart {

	/**
	 * This class provides the framework for traversing the view's Tree structure.
	 */
	private static class ViewContentProvider implements ITreeContentProvider {
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}

		@Override
		public void dispose() {}

		@Override
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		@Override
		public Object getParent(Object child) {
			return null;
		}

		@Override
		public Object[] getChildren(Object par) {
			TreeNode parent = ((TreeNode)par);

			Object[] children = new Object[parent.getChildCount()];

			for(int i=0; i<children.length; i++) {
				children[i] = parent.getChildAt(i);
			}

			return children;
		}

		@Override
		public boolean hasChildren(Object parent) {
			return ((TreeNode)parent).getChildCount() > 0;
		}
	}

	/**
	 * This class provides functionality for determining what image to
	 * display for each item in the tree.
	 */
	private static class ViewLabelProvider extends LabelProvider {
		@Override
		public String getText(Object obj) {
			return obj.toString();
		}

		@Override
		public Image getImage(Object obj) {
			TreeNode treeObj = (TreeNode)obj;
			Image img;

			img = DashboardPlugin.getImageDescriptor("icons/misc/graph_dis.gif").createImage(); //$NON-NLS-1$
			if (treeObj.getChildCount() > 0) {
				img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			}

			return img;
		}
	}

	/**
	 * This method creates the framework for the view.  It initializes the viewer, which
	 * contains the TreeNode and handles how to display each entry.
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.getShell().setCursor(new Cursor(parent.getShell().getDisplay(), SWT.CURSOR_WAIT));
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		generateGraphsTree();
		makeActions();
	}

	/**
	 * This method is intented to provided the necessary information for generating everthing
	 * that needs to be displayed in the tree viewer.
	 */
	protected abstract void generateGraphsTree();

	/**
	 * This method is intented to add new Actions to the view.
	 */
	protected void makeActions() {}

	/**
	 * This method is intented to handle updating the view when items are added or removed.
	 */
	public void refresh() {
		generateGraphsTree();
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * This method removes all internal references. Nothing should be called/referenced after
	 * this method is run.
	 */
	@Override
	public void dispose() {
		super.dispose();
		viewer = null;
	}

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.dashboard.views.GraphsView"; //$NON-NLS-1$
	protected TreeViewer viewer;
}
