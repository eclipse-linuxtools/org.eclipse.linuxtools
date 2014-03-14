/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;



/**
 * The parent class of the Kernel Source browser, and grandparent of the Tapset browsers.
 * Contains code common to all three of those classes.
 * @author Ryan Morse
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.KernelBrowserView
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView
 */
public abstract class BrowserView extends ViewPart {
	protected TreeViewer viewer;

	private CollapseAllHandler collapseHandler;

	public BrowserView() {
		super();
	}

	/**
	 * Provides an interface for the TreeViewer to interact with the internal TreeNode data structure.
	 * @author Ryan Morse
	 *
	 */
	static class ViewContentProvider implements ITreeContentProvider {
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

	protected abstract Image getEntryImage(TreeNode treeObj);

	protected Image getGenericImage(TreeNode treeObj) {
		if (treeObj.getChildCount() == 0) {
			return IDEPlugin.getImageDescriptor("icons/vars/var_unk.gif").createImage(); //$NON-NLS-1$
		} else {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
	}

	/**
	 * Provides the icon and text for each entry in the tapset tree.
	 * @author Ryan Morse
	 */
	protected class ViewLabelProvider extends LabelProvider {
		@Override
		public String getText(Object obj) {
			return obj.toString();
		}

		@Override
		public Image getImage(Object obj) {
			return getEntryImage((TreeNode) obj);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.getShell().setCursor(parent.getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		PatternFilter filter = new PatternFilter();
		FilteredTree filteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
		viewer = filteredTree.getViewer();
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		collapseHandler = new CollapseAllHandler(getViewer());
		handlerService.activateHandler(CollapseAllHandler.COMMAND_ID, collapseHandler);
	}

	protected void registerContextMenu(String menuName) {
		Control control = this.viewer.getControl();
		MenuManager manager = new MenuManager(menuName);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = manager.createContextMenu(control);
		viewer.getControl().setMenu(menu);

		IWorkbenchPartSite partSite = getSite();
		partSite.registerContextMenu(manager, viewer);
		partSite.setSelectionProvider(viewer);
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		viewer = null;
		if(collapseHandler != null) {
			collapseHandler.dispose();
		}
	}

	abstract void refresh();

	protected class ViewUpdater implements IUpdateListener {
		@Override
		public void handleUpdateEvent() {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					refresh();
				}
			});
		}
	}
}