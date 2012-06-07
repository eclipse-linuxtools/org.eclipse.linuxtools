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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;

/**
 * This class provides the framework for the ModuleBrowsers.  Since
 * it is abstract, it can't be implemented itself.  It does however
 * provide a number of common methods that will be needed for all of
 * the ModuleBrowsers.
 * @author Ryan Morse
 */
public abstract class GraphsView extends ViewPart {
	public GraphsView() {
		super();
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
	}
	
	/**
	 * This class provides the framework for traversing the view's Tree structure.
	 */
	private class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}
		
		public void dispose() {}
		
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}
		
		public Object getParent(Object child) {
			return null;
		}
		
		public Object[] getChildren(Object par) {
			TreeNode parent = ((TreeNode)par);

			Object[] children = new Object[parent.getChildCount()];

			for(int i=0; i<children.length; i++) {
				children[i] = parent.getChildAt(i);
			}
			
			return children;
		}
		
		public boolean hasChildren(Object parent) {
			return ((TreeNode)parent).getChildCount() > 0;
		}
	}
	
	/**
	 * This class provides functionality for determining what image to
	 * display for each item in the tree.
	 */
	private class ViewLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			TreeNode treeObj = (TreeNode)obj;
			Image img;
			
			img = DashboardPlugin.getImageDescriptor("icons/misc/graph_dis.gif").createImage();
			if (treeObj.getChildCount() > 0)
				img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

			return img;
		}
	}	
	
	/**
	 * This method creates the framework for the view.  It initializes the viewer, which 
	 * contains the TreeNode and handles how to display each entry.
	 */
	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this); //$NON-NLS-1$
		parent.getShell().setCursor(new Cursor(parent.getShell().getDisplay(), SWT.CURSOR_WAIT));
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		generateGraphsTree();
		makeActions();
		LogManager.logDebug("End createPartControl:", this); //$NON-NLS-1$
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

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * This method removes all internal references. Nothing should be called/referenced after
	 * this method is run.
	 */
	public void dispose() {
		LogManager.logInfo("disposing", this); //$NON-NLS-1$
		super.dispose();
		viewer = null;
	}
	
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.dashboard.views.GraphsView";
	protected TreeViewer viewer;
}
