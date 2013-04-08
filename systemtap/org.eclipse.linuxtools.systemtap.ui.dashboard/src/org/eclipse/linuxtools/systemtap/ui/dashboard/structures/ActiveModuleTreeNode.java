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

package org.eclipse.linuxtools.systemtap.ui.dashboard.structures;

import org.eclipse.linuxtools.systemtap.structures.TreeNode;

/**
 * This is a simple extension of TreeNode to allow for providing a popup menu
 * for specific items in the ActiveModuleBrowserView and DashboardModuleBrowserView
 * @author Ryan Morse
 *
 */
public class ActiveModuleTreeNode extends TreeNode {
	public ActiveModuleTreeNode(ActiveModuleData amd, String disp, boolean c) {
		super(amd, disp, c);
	}
}
