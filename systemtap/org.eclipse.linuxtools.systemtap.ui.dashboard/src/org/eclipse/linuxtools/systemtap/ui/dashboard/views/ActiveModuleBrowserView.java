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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.actions.ActivateGraphAction;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ActiveModuleData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ActiveModuleTreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardGraphData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModule;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.GraphTreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;

/**
 * This class is used to display a list of all of the Modules that are running
 * as well as the status of each graph associated with the module.  It provides
 * methods for starting, stopping, and pausing the active module.
 * @author Ryan Morse
 */
public class ActiveModuleBrowserView extends ModuleView {

	@Override
	protected void generateModuleTree() {
		viewer.setInput(new TreeNode("root", "", false)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		viewer.setLabelProvider(new ViewLabelProvider());
	}

	/**
	 * This method builds the actions for the items inside this view.  It adds a
	 * double click listener to each of the Items so they will be run if they
	 * are actual modules.  It also sets up the layout for popup menu when users
	 * right click on a module element.
	 */
	@Override
	protected void makeActions() {
		//Gets items from plugin.xml
		MenuManager manager = new MenuManager("modulePopup"); //$NON-NLS-1$
		Control control = this.viewer.getControl();
		manager.add(new Separator("file.ext")); //$NON-NLS-1$
		manager.add(new Separator("build.ext")); //$NON-NLS-1$
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = manager.createContextMenu(control);
		control.setMenu(menu);
		getSite().registerContextMenu(manager, viewer);
		super.makeActions();
	}

	/**
	 * This method handles adding a new module to the list of active modules.
	 * It will then generate each of the graphs that are a part of the module.
	 * Adding them as a new graph in the main display and adding them as children
	 * of the module in the ActiveModuleBrowserView.
	 * @param display The string used as a title for the module
	 * @param data The <code>ActiveModuleData</code> representing the item to add.
	 */
	public void add(String display, ActiveModuleData data) {
		Display disp = PlatformUI.getWorkbench().getDisplay();
		disp.getActiveShell().setCursor(disp.getSystemCursor(SWT.CURSOR_WAIT));

		TreeNode root = (TreeNode)viewer.getInput();
		ActiveModuleTreeNode child = new ActiveModuleTreeNode(data, display, true);
		GraphTreeNode graphNode;
		root.add(child);
		DashboardGraphData graphData;
		for(int i=0; i<data.module.graphs.length; i++) {
			graphData = new DashboardGraphData();
			graphData.data = data.data;
			graphData.filters = new IDataSetFilter[data.module.filters[i].size()];
			data.module.filters[i].toArray(graphData.filters);
			graphData.graph = data.module.graphs[i];
			graphData.moduleName = data.module.category;
			graphData.adapter = null;
			ActivateGraphAction act = new ActivateGraphAction();
			graphNode = new GraphTreeNode(graphData, data.module.graphs[i].title, true);
			act.run(graphNode,data.module.getcategory());
			child.add(graphNode);
		}
		root.sortLevel();
		viewer.refresh();
		disp.getActiveShell().setCursor(null);
	}

	/**
	 * This method handles pausing a module in the ActiveModuleBrowserView.  When
	 * it locates the item to be paused, it will return a reference to that module.
	 * @param mod The <code>DashboardModule</code> that needs to be paused
	 * @return The item being paused
	 */
	public ActiveModuleData pause(DashboardModule mod) {
		TreeNode tree = (TreeNode)viewer.getInput();
		for(int i=0; i<tree.getChildCount(); i++) {
			if(tree.getChildAt(i).toString().equals(mod.category + "." + mod.display)) { //$NON-NLS-1$
				return (ActiveModuleData)(tree.getChildAt(i).getData());
			}
		}

		return null;
	}

	/**
	 * This method handles removing a module in the ActiveModuleBrowserView.  When
	 * it locates the item to be removed, it will return a remove it from the tree
	 * and return a reference to that module.
	 * @param mod The module that needs to be removed
	 * @return The active module that was removed.
	 */
	public ActiveModuleTreeNode remove(DashboardModule mod) {
		TreeNode tree = (TreeNode)viewer.getInput();
		for(int i=0; i<tree.getChildCount(); i++) {
			if(tree.getChildAt(i).toString().equals(mod.category + "." + mod.display)) { //$NON-NLS-1$
				ActiveModuleTreeNode node = (ActiveModuleTreeNode)tree.getChildAt(i);
				tree.remove(i);
				viewer.refresh();
				return node;
			}
		}

		return null;
	}

	/**
	 * This method checks to see if the provided module is running or not.
	 * @param mod The module to check if it is running
	 * @return boolean representing whether the provided module is running
	 */
	public boolean isActive(DashboardModule mod) {
		TreeNode tree = (TreeNode)viewer.getInput();
		for(int i=0; i<tree.getChildCount(); i++) {
			if(tree.getChildAt(i).toString().equals(mod.category + "." + mod.display)) {//$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks to see if the provided module is running, and if so
	 * whether or not it is paused.
	 * @param mod The module to check if it is paused.
	 * @return boolean representing whether the provided module is paused
	 */
	public boolean isPaused(DashboardModule mod) {
		TreeNode tree = (TreeNode)viewer.getInput();
		for(int i=0; i<tree.getChildCount(); i++) {
			if(tree.getChildAt(i).toString().equals(mod.category + "." + mod.display)) {//$NON-NLS-1$
				return ((ActiveModuleData)tree.getChildAt(i).getData()).paused;
			}
		}
		return false;
	}

	/**
	 * This method checks to see if any modules are still running
	 * @return boolean - are any modules are still running?
	 */
	public boolean anyRunning() {
		TreeNode tree = (TreeNode)viewer.getInput();
		return tree.getChildCount() > 0;
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
			Image img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);

			if(treeObj.getChildCount() > 0) {
				img = DashboardPlugin.getImageDescriptor("icons/misc/module_obj.gif").createImage(); //$NON-NLS-1$
			} else if(treeObj instanceof GraphTreeNode) {
				if(null == ((DashboardGraphData)((GraphTreeNode)treeObj).getData()).adapter) {
					img = DashboardPlugin.getImageDescriptor("icons/misc/graph_dis.gif").createImage(); //$NON-NLS-1$
				} else {
					img = DashboardPlugin.getImageDescriptor("icons/misc/graph_act.gif").createImage(); //$NON-NLS-1$
				}
			}

			return img;
		}
	}

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView"; //$NON-NLS-1$
}
