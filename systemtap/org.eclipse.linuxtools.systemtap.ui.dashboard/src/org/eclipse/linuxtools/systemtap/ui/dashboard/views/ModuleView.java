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

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
public abstract class ModuleView extends ViewPart {
	public ModuleView() {
		super();
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
	}
	
	/**
	 * This class provides the framework for traversing the view's Tree structure.
	 */
	private static class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
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
public class ViewLabelProvider extends LabelProvider 
	implements ILabelProvider, IFontProvider, IColorProvider{

	FontRegistry registry = new FontRegistry();
	int j=0;

	public Image getImage(Object element) {
		TreeNode treeObj = (TreeNode)element;
		Image img = null;
		img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		if (treeObj.getChildCount() == 0)
			img = DashboardPlugin.getImageDescriptor("icons/misc/module_obj.gif").createImage();
		
		return img;
	}

	public String getText(Object obj) {
		return obj.toString();
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public Font getFont(Object element) {
		Font font = Display.getCurrent().getSystemFont();
		//TreeNode parent = ((TreeNode)element);
		/*if (parent.getChildCount()>0) {
			font = registry.getBold(Display.getCurrent().getSystemFont().g);
		}*/
		return font;
	}

	public Color getBackground(Object element) {
	//	TreeNode parent = ((TreeNode)element);
		Color c = null;
	//	if (parent.getChildCount()>0) {
	//	c = new Color(Display.getCurrent(), IGraphColorConstants.COLORS[j]);
	//	j++;
	//	}
		return c;
	}

	public Color getForeground(Object element, int columnIndex) {
		//Globals.debug("Get foreground colour for index " + columnIndex + "!",8);
		Color color = null;
		//if (element instanceof MyModelItem) {
			//color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		//}
		return color;
	}

	public Color getForeground(Object element) {
	//	Globals.debug("Get foreground colour!",8);
	//	Color color = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);;
	//	if (element instanceof MyModelItem) {
		//	MyModelItem mItem = (MyModelItem) element;
			//color = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
		//}
		return null;
	}

	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

}
	/**
	 * This class provides functionality for determining what image to
	 * display for each item in the tree.
	 */
	/*private class ViewLabelProvider extends LabelProvider implements ITableColorProvider{
		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			TreeNode treeObj = (TreeNode)obj;
			Image img;
			
			img = DashboardPlugin.getImageDescriptor("icons/misc/module_obj.gif").createImage();
			if (treeObj.getChildCount() > 0)
				img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

			return img;
		}

		public Color getBackground(Object obj, int columnIndex) {
		//	if (((TreeNode)obj).getChildCount() > 0)
			//{
			Color color = new Color(null, 255, 255, 255);//color is black
			return color;
		//	}
			//else
				//return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			Color color = new Color(null, 255, 255, 255);//color is black
			return color;
		}
	}	*/
	
	/**
	 * This method creates the framework for the view.  It initializes the viewer, which 
	 * contains the TreeNode and handles how to display each entry.
	 */
	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this); //$NON-NLS-1$
		parent.getShell().setCursor(new Cursor(parent.getShell().getDisplay(), SWT.CURSOR_WAIT));
	//	viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer = new TreeViewer(parent);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		generateModuleTree();
		makeActions();
		LogManager.logDebug("End createPartControl:", this); //$NON-NLS-1$
	}
	
	/**
	 * This method is intented to provided the necessary information for generating everthing
	 * that needs to be displayed in the tree viewer.
	 */
	protected abstract void generateModuleTree();

	/**
	 * This method is intented to add new Actions to the view.
	 */
	protected void makeActions() {}
	
	/**
	 * This method is intented to handle updating the view when items are added or removed.
	 */
	public void refresh() {
		generateModuleTree();
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
	
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.dashboard.views.ModuleView";
	protected TreeViewer viewer;
}
