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
import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.structures.DashboardModuleLocator;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * This is a basic browser view for the dashboard perspective.  It contains a list
 * of all of the available dashboard modules.
 * @author Ryan Morse
 */
public class DashboardModuleBrowserView extends ModuleView {

	/**
	 * This method sends requests to get all of the modules that are
	 * avialable on the system.  Once then are found, it will
	 * set the viewer's content to the tree of modules that were found.
	 */
	@Override
	protected void generateModuleTree() {
		TreeNode modules = DashboardModuleLocator.getModules();

		if(null != modules) {
			viewer.setInput(modules);
		} else {
			viewer.setInput(new TreeNode("", false)); //$NON-NLS-1$
		}

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

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView"; //$NON-NLS-1$

}

