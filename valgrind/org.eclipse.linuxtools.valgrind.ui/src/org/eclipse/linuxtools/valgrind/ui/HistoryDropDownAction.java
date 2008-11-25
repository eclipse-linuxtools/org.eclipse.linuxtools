/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.linuxtools.valgrind.core.HistoryFile;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class HistoryDropDownAction extends Action implements IMenuCreator {
	protected Menu menu;
	
	public HistoryDropDownAction(String text, int style) {
		super(text, style);		
		setImageDescriptor(ValgrindUIPlugin.imageDescriptorFromPlugin(ValgrindUIPlugin.PLUGIN_ID, "icons/history_list.gif")); //$NON-NLS-1$
		setMenuCreator(this);
	}
	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}				
		menu = new Menu(parent);
		
		String[] executables = HistoryFile.getInstance().getExecutables();
		String[] tools = HistoryFile.getInstance().getTools();
		String[] datadirs = HistoryFile.getInstance().getDatadirs();
	
		// fill starting from most recent
		for (int i = executables.length - 1; i >= 0; i--) {
			ActionContributionItem item = new ActionContributionItem(new HistoryAction(executables[i], tools[i], datadirs[i]));
			item.fill(menu, -1);
		}
		
		return menu;
	}
	
	protected void createMenu(Control parent) {
		
	}

	public Menu getMenu(Menu parent) {
		return null;
	}
	
	public Menu getControl() {
		return menu;
	}

}
