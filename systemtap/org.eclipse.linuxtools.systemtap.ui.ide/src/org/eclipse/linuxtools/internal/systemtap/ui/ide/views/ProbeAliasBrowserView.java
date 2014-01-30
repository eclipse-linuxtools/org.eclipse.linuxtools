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
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.ProbeAliasAction;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;


/**
 * The Probe Alias Browser module of the SystemTap GUI. This class provides a list of all probe aliases
 * defined in the tapset (both the standard, and user-specified tapsets), and allows the user to insert
 * template probes into an editor.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class ProbeAliasBrowserView extends BrowserView {
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView"; //$NON-NLS-1$
	private ProbeAliasAction doubleClickAction;

	/**
	 * Creates the UI on the given <code>Composite</code>
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		TapsetLibrary.init();
		TapsetLibrary.addProbeListener(new ViewUpdater());
		refresh();
		makeActions();
	}

	/**
	 * Refreshes the list of probe aliases in the viewer.
	 */
	@Override
	public void refresh() {
		TreeNode probes = TapsetLibrary.getProbes();
		if (probes != null){
			super.viewer.setInput(TapsetLibrary.getProbes());
		}
	}

	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	private void makeActions() {
		doubleClickAction = new ProbeAliasAction(getSite().getWorkbenchWindow(), this);
		viewer.addDoubleClickListener(doubleClickAction);
		Control control = this.viewer.getControl();
		MenuManager manager = new MenuManager("probePopup"); //$NON-NLS-1$

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
	}
}
