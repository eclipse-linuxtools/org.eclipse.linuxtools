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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.ui.editor.RecentFileMenuManager;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
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

	public BrowserView() {
		super();
	}
	
	/**
	 * Provides an interface for the TreeViewer to interact with the internal TreeNode data structure.
	 * @author Ryan Morse
	 *
	 */
	static class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
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
	 * Provides the icon and text for each entry in the tapset tree.
	 * @author Ryan Morse
	 */
	static class ViewLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			TreeNode treeObj = (TreeNode)obj;
			Image img;
			String item = treeObj.getData().toString();
			
			img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			if (treeObj.getChildCount() > 0)
				img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);


			//Kernel Source
			if(item.endsWith(".c"))
				img = IDEPlugin.getImageDescriptor("icons/files/file_c.gif").createImage();
			if(item.endsWith(".h"))
				img = IDEPlugin.getImageDescriptor("icons/files/file_h.gif").createImage();
			
			//Functions
			if(item.endsWith(")") && !item.endsWith("\")")) {
				item = item.substring(0, item.indexOf("(")).trim();
				if(item.endsWith(":long"))
					img = IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage();
				else if(item.endsWith(":string"))
					img = IDEPlugin.getImageDescriptor("icons/vars/var_str.gif").createImage();
				else //if(item.endsWith(":unknown"))
					img = IDEPlugin.getImageDescriptor("icons/vars/var_void.gif").createImage();
			} else {
				//Probes
				if(item.startsWith("probe"))
					img = IDEPlugin.getImageDescriptor("icons/misc/probe_obj.gif").createImage();
				
				//Probe variables
				if(item.endsWith(":long"))
					img = IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage();
				else if(item.endsWith(":string"))
					img = IDEPlugin.getImageDescriptor("icons/vars/var_str.gif").createImage();
				else if(item.endsWith(":unknown"))
					img = IDEPlugin.getImageDescriptor("icons/vars/var_unk.gif").createImage();
			}

			return img;
		}
	}	
	
	public void createPartControl(Composite parent) {
		parent.getShell().setCursor(new Cursor(parent.getShell().getDisplay(), SWT.CURSOR_WAIT));
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		RecentFileMenuManager.getInstance().registerActionBar(getViewSite().getActionBars());
	}
	
	public TreeViewer getViewer() {
		return viewer;
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public void dispose() {
		super.dispose();
		viewer = null;
	}
	
	abstract void refresh();
	
	protected class ViewUpdater implements IUpdateListener {
		public void handleUpdateEvent() {
			try {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					refresh();
				}
			});
			}catch(Exception e)
			{
				//TO FIX: BUG 315988
			}
		}
	}
}