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
import org.eclipse.ui.ISharedImages;
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

	/**
	 * Provides the icon and text for each entry in the tapset tree.
	 * @author Ryan Morse
	 */
	static class ViewLabelProvider extends LabelProvider {
		@Override
		public String getText(Object obj) {
			return obj.toString();
		}

		@Override
		public Image getImage(Object obj) {
			TreeNode treeObj = (TreeNode)obj;
			Image img;
			String item = treeObj.getData().toString();

			img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			if (treeObj.getChildCount() > 0) {
				img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			}


			//Kernel Source
			if(item.endsWith(".c")) {//$NON-NLS-1$
				img = IDEPlugin.getImageDescriptor("icons/files/file_c.gif").createImage(); //$NON-NLS-1$
			}
			if(item.endsWith(".h")) {//$NON-NLS-1$
				img = IDEPlugin.getImageDescriptor("icons/files/file_h.gif").createImage(); //$NON-NLS-1$
			}

			//Functions
			if(item.endsWith(")") && !item.endsWith("\")")) { //$NON-NLS-1$ //$NON-NLS-2$
				item = item.substring(0, item.indexOf('(')).trim();
				if(item.endsWith(":long")) {//$NON-NLS-1$
					img = IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage(); //$NON-NLS-1$
				} else if(item.endsWith(":string")) {//$NON-NLS-1$
					img = IDEPlugin.getImageDescriptor("icons/vars/var_str.gif").createImage(); //$NON-NLS-1$
				} else {
					img = IDEPlugin.getImageDescriptor("icons/vars/var_void.gif").createImage(); //$NON-NLS-1$
				}
			} else {
				//Probes
				if(item.startsWith("probe")) {//$NON-NLS-1$
					img = IDEPlugin.getImageDescriptor("icons/misc/probe_obj.gif").createImage(); //$NON-NLS-1$
				}

				//Probe variables
				if(item.endsWith(":long")) {//$NON-NLS-1$
					img = IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage(); //$NON-NLS-1$
				} else if(item.endsWith(":string")) {//$NON-NLS-1$
					img = IDEPlugin.getImageDescriptor("icons/vars/var_str.gif").createImage(); //$NON-NLS-1$
				} else if(item.endsWith(":unknown")) {//$NON-NLS-1$
					img = IDEPlugin.getImageDescriptor("icons/vars/var_unk.gif").createImage(); //$NON-NLS-1$
				} else {
					img = IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage(); //$NON-NLS-1$
				}
			}

			return img;
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