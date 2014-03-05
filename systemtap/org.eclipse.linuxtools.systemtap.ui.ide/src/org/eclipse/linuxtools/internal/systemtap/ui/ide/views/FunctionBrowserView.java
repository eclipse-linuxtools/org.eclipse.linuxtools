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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.FunctionBrowserAction;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.structures.FunctionNodeData;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.graphics.Image;
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
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.FunctionBrowserView"; //$NON-NLS-1$
	private FunctionBrowserAction doubleClickAction;
	private TreeNode functions;
	private TreeNode localFunctions;

	/**
	 * Creates the UI on the given <code>Composite</code>
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		TapsetLibrary.init();
		TapsetLibrary.addFunctionListener(new ViewUpdater());
		refresh();
		makeActions();
	}

	@Override
	protected Image getEntryImage(TreeNode treeObj) {
		if (!(treeObj.getData() instanceof FunctionNodeData)) {
			return null;
		}
		FunctionNodeData d = (FunctionNodeData) treeObj.getData();
		String type = d.getType();
		if (type == null) {
			return IDEPlugin.getImageDescriptor("icons/vars/var_void.gif").createImage(); //$NON-NLS-1$
		} else if(type.equals("long")) {//$NON-NLS-1$
			return IDEPlugin.getImageDescriptor("icons/vars/var_long.gif").createImage(); //$NON-NLS-1$
		} else if(type.equals("string")) {//$NON-NLS-1$
			return IDEPlugin.getImageDescriptor("icons/vars/var_str.gif").createImage(); //$NON-NLS-1$
		} else {
			return IDEPlugin.getImageDescriptor("icons/vars/var_unk.gif").createImage(); //$NON-NLS-1$
		}
	}

	/**
	 * Refreshes the list of functions in the viewer.
	 */
	@Override
	public void refresh() {
		functions = TapsetLibrary.getFunctions();
		if (functions != null){
			addLocalFunctions(localFunctions);
		}
	}

	/**
	 * Adds the local functions specified in the argument to the viewer.
	 * @param localFunctionTree A tree of the local functions.
	 */
	public void addLocalFunctions(TreeNode localFunctionTree) {
		if(functions.getChildCount() > 0) {
			TreeNode localFuncs = functions.getChildAt(0);

			if("<local>".equals(localFuncs.toString())) { //$NON-NLS-1$
				functions.remove(0);
			}

			if(null != localFunctions) {
				localFunctions = localFunctionTree;
				localFunctions.setDisplay("<local>"); //$NON-NLS-1$
				functions.addAt(localFunctions, 0);
			}
		}
		viewer.setInput(functions);
	}

	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	private void makeActions() {
		doubleClickAction = new FunctionBrowserAction(getSite().getWorkbenchWindow(), this);
		viewer.addDoubleClickListener(doubleClickAction);
		Control control = this.viewer.getControl();
		MenuManager manager = new MenuManager("functionPopup"); //$NON-NLS-1$

		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = manager.createContextMenu(control);
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(manager, viewer);
	}

	@Override
	public void dispose() {
		super.dispose();
		if(null != viewer) {
			viewer.removeDoubleClickListener(doubleClickAction);
		}
		if(null != doubleClickAction) {
			doubleClickAction.dispose();
		}
		doubleClickAction = null;
		if(null != localFunctions) {
			localFunctions.dispose();
		}
		localFunctions = null;
		if(null != functions) {
			functions.dispose();
		}
		functions = null;
		TapsetLibrary.stop();
	}
}
