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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden.FunctionBrowserAction;
import org.eclipse.linuxtools.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;



/**
 * This class is the Function Tapset Browser, which provides a list of all of the functions
 * defined in the tapset library for the user to browse through.
 * @author Ryan Morse
 * @author Henry Hughes
 */
public class FunctionBrowserView extends BrowserView {
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView";
	private FunctionBrowserAction doubleClickAction;
	private TreeNode functions;
	private TreeNode localFunctions;
	private Menu menu;

	public FunctionBrowserView() {
		super();
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
	}

	/**
	 * Creates the UI on the given <code>Composite</code>
	 */
	@Override
	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this); //$NON-NLS-1$
		super.createPartControl(parent);
		TapsetLibrary.init();
		TapsetLibrary.addListener(new ViewUpdater());
		refresh();
		makeActions();
		LogManager.logDebug("End createPartControl:", this); //$NON-NLS-1$
	}

	/**
	 * Refreshes the list of functions in the viewer.
	 */
	@Override
	public void refresh() {
		LogManager.logDebug("Start refresh:", this); //$NON-NLS-1$
		functions = TapsetLibrary.getFunctions();
		addLocalFunctions(localFunctions);
		LogManager.logDebug("End refresh:", this); //$NON-NLS-1$
	}

	/**
	 * Adds the local functions specified in the argument to the viewer.
	 * @param localFunctionTree A tree of the local functions.
	 */
	public void addLocalFunctions(TreeNode localFunctionTree) {
		LogManager.logDebug("Start addLocalFunctions: localFunctionTree-" + localFunctionTree, this); //$NON-NLS-1$

		if(functions.getChildCount() > 0) {
			TreeNode localFuncs = functions.getChildAt(0);

			if("<local>".equals(localFuncs.toString()))
				functions.remove(0);

			if(null != localFunctions) {
				localFunctions = localFunctionTree;
				localFunctions.setDisplay("<local>");
				functions.addAt(localFunctions, 0);
			}
		}
		viewer.setInput(functions);
		LogManager.logDebug("End addLocalFunctions:", this); //$NON-NLS-1$
	}

	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	private void makeActions() {
		LogManager.logDebug("Start makeActions:", this); //$NON-NLS-1$
		doubleClickAction = new FunctionBrowserAction(getSite().getWorkbenchWindow(), this);
		viewer.addDoubleClickListener(doubleClickAction);

		//This loads the menu from plugin.xml
		MenuManager manager = new MenuManager("functionPopup");
		Control control = this.viewer.getControl();
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = manager.createContextMenu(control);
		control.setMenu(menu);
		getSite().registerContextMenu(manager, viewer);
		LogManager.logDebug("End makeActions:", this); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		super.dispose();
		if(null != viewer)
			viewer.removeDoubleClickListener(doubleClickAction);
		if(null != doubleClickAction)
			doubleClickAction.dispose();
		doubleClickAction = null;
		if(null != localFunctions)
			localFunctions.dispose();
		localFunctions = null;
		if(null != functions)
			functions.dispose();
		functions = null;
		if(null != menu)
			menu.dispose();
		menu = null;
		LogManager.logDebug("End dispose:", this); //$NON-NLS-1$
		TapsetLibrary.stop();
	}
}
