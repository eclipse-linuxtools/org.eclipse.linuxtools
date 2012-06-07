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

package org.eclipse.linuxtools.systemtap.ui.graphing.views;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.graphing.structures.GraphDisplaySet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.ITabListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;



/**
 * This class is designed to hold a number of different running script sets.
 * Each running script is given its own tab that contains all the details for
 * that specific script.  Script sets can be created or disposed of at the will
 * of the user.
 * @author Ryan Morse
 */
public class GraphSelectorView extends ViewPart {
	public GraphSelectorView() {
		super();
		LogManager.logDebug("Start GraphSelectorView:", this); //$NON-NLS-1$
		LogManager.logInfo("Initializing", this); //$NON-NLS-1$
		displaySets = new ArrayList<GraphDisplaySet>();
		tabListeners = new ArrayList<ITabListener>();
		LogManager.logDebug("End GraphSelectorView:", this); //$NON-NLS-1$
	}
	
	/**
	 * This method will create a new script set for the provided dataSet
	 * The new script set will be given a new tab item at the end of 
	 * the list.
	 * @param title The name to be shown on the new tab
	 * @param dataSet The <code>IDataSet</code> for the new script set
	 * @param cmd The running command that this script set represents.
	 */
	public void createScriptSet(String title, IDataSet dataSet) {
		CTabItem item;

		item = new CTabItem(scriptFolder, SWT.CLOSE);
		item.setText(title);
		Composite parent = new Composite(scriptFolder, SWT.NONE);
		parent.setBackground(new Color(parent.getDisplay(), 0, 0, 255));
		GraphDisplaySet gds = new GraphDisplaySet(parent, dataSet);
		displaySets.add(gds);
		item.setControl(parent);

		scriptFolder.setSelection(item);
		fireTabOpenEvent();
	}
	
	/**
	 * This method creates the framework for what will be displayed by this dialog box.
	 * @param parent The composite that will contain all the elements from this dialog
	 */
	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this); //$NON-NLS-1$

		FormData data2 = new FormData();
		data2.left = new FormAttachment(0, 0);
		data2.top = new FormAttachment(0, 0);
		data2.right = new FormAttachment(100, 0);
		data2.bottom = new FormAttachment(100, 0);
		Composite cmpGraph = new Composite(parent, SWT.NONE);
		cmpGraph.setLayoutData(data2);
		
		//This is for the tab view
		cmpGraph.setLayout(new FormLayout());

		//Create the folder for all of the script sets, so it takes up all of the parent composite
		scriptFolder = new CTabFolder(cmpGraph, SWT.NONE);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		scriptFolder.setLayoutData(data);
		scriptFolder.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fireTabChangedEvent();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		

		scriptFolder.addCTabFolder2Listener(new CTabFolder2Listener() {
			public void restore(CTabFolderEvent e) {}
			public void showList(CTabFolderEvent e) {}
			public void minimize(CTabFolderEvent e) {}
			public void maximize(CTabFolderEvent e) {}
			public void close(CTabFolderEvent e) {
				displaySets.remove(scriptFolder.indexOf((CTabItem)e.item));
				fireTabCloseEvent();
			}
		});

		LogManager.logDebug("End createPartControl", this); //$NON-NLS-1$
	}
	
	/**
	 * Finds and returns the active component inside of the active script set
	 * @return The <code>GraphDisplaySet</code> that is currently active
	 */
	public GraphDisplaySet getActiveDisplaySet() {
		int index = scriptFolder.getSelectionIndex();
		if(index >= 0 && index < displaySets.size())
			return (GraphDisplaySet)displaySets.get(index);
		else
			return null;
	}
	
	public void setFocus() {}
	
	public void addTabListener(ITabListener listener) {
		tabListeners.add(listener);
	}
	
	public void removeTabListener(ITabListener listener) {
		tabListeners.remove(listener);
	}
	
	private void fireTabCloseEvent() {
		for(int i=0; i<tabListeners.size(); i++)
			((ITabListener)tabListeners.get(i)).tabClosed();
	}
	
	private void fireTabOpenEvent() {
		for(int i=0; i<tabListeners.size(); i++)
			((ITabListener)tabListeners.get(i)).tabOpened();
	}
	
	private void fireTabChangedEvent() {
		for(int i=0; i<tabListeners.size(); i++)
			((ITabListener)tabListeners.get(i)).tabChanged();
	}

	/**
	 * Removes all internal references in this class.  Nothing should make any references
	 * to anyting in this class after calling the dispose method.
	 */
	public void dispose() {
		LogManager.logDebug("Start dispose:", this); //$NON-NLS-1$
		LogManager.logInfo("Disposing", this); //$NON-NLS-1$
		super.dispose();

		if(null != scriptFolder)
			scriptFolder.dispose();
		scriptFolder = null;
		if(null != tabListeners)
			tabListeners.removeAll(tabListeners);
		tabListeners = null;
		LogManager.logDebug("End dispose:", this); //$NON-NLS-1$
	}
	
	private CTabFolder scriptFolder;
	private ArrayList<GraphDisplaySet> displaySets;
	private ArrayList<ITabListener> tabListeners;
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorView";
}
