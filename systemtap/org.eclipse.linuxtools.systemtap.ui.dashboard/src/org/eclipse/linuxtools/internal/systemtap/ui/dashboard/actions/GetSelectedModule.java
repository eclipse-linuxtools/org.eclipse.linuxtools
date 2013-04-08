/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.dashboard.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ActiveModuleData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModule;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.ModuleTreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView;

/**
 * This class is used for determining what module is currently selected. It examins both
 * the DashboardModuleBrowserView and the ActiveModuleBrowserView to determine which is
 * active, and what is selected inside that view.
 * @author Ryan Morse
 */
public final class GetSelectedModule {
	/**
	 * This class examins the provided view and if it is an instance of either
	 * DashboardModuleBrowserView or ActiveModuleBrowserView it retreives the selected
	 * item and returns it.  Else, it returns null.
	 * @param view The view that is currently open.
	 * @return The selected DashboardModule contained in the provided view
	 */
	public static DashboardModule getModule(IViewPart view) {
		if(null == view) {
			view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DashboardModuleBrowserView.ID);
		}

		if(view instanceof DashboardModuleBrowserView) {
			return getModule2((DashboardModuleBrowserView)view);
		} else if(view instanceof ActiveModuleBrowserView) {
			return getModule2((ActiveModuleBrowserView)view);
		}
		return null;
	}


	/**
	 * This class examins the provied view and retreives the selected item from
	 * it.  It then converts the selected item into a DashboardModule and returns it.
	 * @param view The DashboardModuleBrowserView that is currently open
	 * @return The selected DashboardModule from the view
	 */
	private static DashboardModule getModule2(DashboardModuleBrowserView view) {
		TreeViewer viewer = view.getViewer();
		ISelection selected = viewer.getSelection();
		if(null != selected && selected instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)selected).getFirstElement();
			if(element instanceof ModuleTreeNode) {
				ModuleTreeNode mod = (ModuleTreeNode)element;
				return (DashboardModule)mod.getData();
			}
		}
		return null;
	}

	public static TreeNode getNode(IViewPart view) {
		TreeViewer viewer = null;
		if(null == view) {
			view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DashboardModuleBrowserView.ID);
		}
		if(view instanceof DashboardModuleBrowserView) {
			viewer = ((DashboardModuleBrowserView)view).getViewer();
		} else {
			viewer = ((ActiveModuleBrowserView)view).getViewer();
		}
		ISelection selected = viewer.getSelection();
		if(null != selected && selected instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)selected).getFirstElement();
			if(element instanceof TreeNode) {
				return (TreeNode)element;
			}
		}
		return null;
	}

	/**
	 * This class examins the provied view and retreives the selected item from
	 * it.  It then gets the DashboardModule contained within the selected item
	 * and returns it.
	 * @param view The ActiveModuleBrowserView that is currently open
	 * @return The selected DashboardModule from the view
	 */


	private static DashboardModule getModule2(ActiveModuleBrowserView view) {
		TreeViewer viewer = view.getViewer();
		ISelection selected = viewer.getSelection();
		if(null != selected && selected instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)selected).getFirstElement();
			if(element instanceof TreeNode) {
				TreeNode mod = (TreeNode)element;
				return ((ActiveModuleData)mod.getData()).module;
			}
		}
		return null;
	}
}
