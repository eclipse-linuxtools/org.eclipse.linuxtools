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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.hidden.ProbeAliasAction;
import org.eclipse.linuxtools.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
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
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.ide.views.ProbeAliasBrowserView";
	private ProbeAliasAction doubleClickAction;
	private IDoubleClickListener dblClickListener;
	private Menu menu;

	public ProbeAliasBrowserView() {
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
	 * Refreshes the list of probe aliases in the viewer.
	 */
	@Override
	public void refresh() {
		LogManager.logDebug("Start refresh:", this); //$NON-NLS-1$
		super.viewer.setInput(TapsetLibrary.getProbes());
		LogManager.logDebug("End refresh:", this); //$NON-NLS-1$
	}
	
	/**
	 * Wires up all of the actions for this browser, such as double and right click handlers.
	 */
	private void makeActions() {
		LogManager.logDebug("Start makeActions:", this); //$NON-NLS-1$
		doubleClickAction = new ProbeAliasAction(getSite().getWorkbenchWindow(), this);
		dblClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				LogManager.logDebug("doubleClick fired", this); //$NON-NLS-1$
				doubleClickAction.run();
			}
		};
		viewer.addDoubleClickListener(dblClickListener);
		Control control = this.viewer.getControl();
		MenuManager manager = new MenuManager("probePopup");

		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = manager.createContextMenu(control);
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(manager, viewer);
		LogManager.logDebug("End makeActions:", this); //$NON-NLS-1$
	}
	
	@Override
	public void dispose() {
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		super.dispose();
		if(null != doubleClickAction)
			doubleClickAction.dispose();
		doubleClickAction = null;
		if(null != viewer)
			viewer.removeDoubleClickListener(dblClickListener);
		dblClickListener = null;
		if(null != menu)
			menu.dispose();
		menu = null;
	}
}
