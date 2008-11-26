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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.linuxtools.valgrind.core.HistoryEntry;
import org.eclipse.linuxtools.valgrind.core.HistoryFile;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class HistoryDropDownAction extends Action implements IMenuCreator {
	protected Menu menu;
	protected List<ActionContributionItem> items;
	
	public HistoryDropDownAction(String text, int style) {
		super(text, style);	
		setImageDescriptor(ValgrindUIPlugin.imageDescriptorFromPlugin(ValgrindUIPlugin.PLUGIN_ID, "icons/history_list.gif")); //$NON-NLS-1$
		setMenuCreator(this);
		items = new ArrayList<ActionContributionItem>();
	}
	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
			items.clear();
		}				
		menu = new Menu(parent);
		
		HistoryEntry[] entries = HistoryFile.getInstance().getEntries();
	
		// fill starting from most recent
		for (int i = entries.length - 1; i >= 0; i--) {
			ActionContributionItem item = new ActionContributionItem(new HistoryAction(entries[i]));
			item.fill(menu, -1);
			items.add(item);
		}
		
		// select most recent if the view has content
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		if (view.getDynamicView() != null) {
			items.get(0).getAction().setChecked(true);
		}
		
		return menu;
	}

	public Menu getMenu(Menu parent) {
		return null;
	}
	
	public ActionContributionItem[] getItems() {
		return items.toArray(new ActionContributionItem[items.size()]);
	}
	
	public Menu getControl() {
		return menu;
	}

}
