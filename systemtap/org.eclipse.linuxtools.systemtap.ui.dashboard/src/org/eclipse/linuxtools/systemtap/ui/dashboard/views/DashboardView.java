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

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.GraphData;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.UpdateManager;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.ITabListener;
import org.eclipse.linuxtools.systemtap.ui.dashboard.DashboardAdapter;
import org.eclipse.linuxtools.systemtap.ui.dashboard.DashboardComposite;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.preferences.DashboardPreferenceConstants;


/**
 * This is the main view of the dashboard.  It is where all of the graphs are displayed 
 * when they are run.  It utalizes <code>DashboardComposite</code> to handle all of the 
 * layout data for the view.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class DashboardView extends ViewPart {

	public DashboardView() {
	super();
	LogManager.logDebug("Start DashboardView:", this); //$NON-NLS-1$
	LogManager.logInfo("Initializing", this); //$NON-NLS-1$
	composites = new ArrayList<DashboardComposite>();
	tabListeners = new ArrayList<ITabListener>();
	moduleNames = new ArrayList<String>();
	LogManager.logDebug("End DashboardView:", this); //$NON-NLS-1$

}

	
	/**
	 * This method will create a new composite for the module
	 * The composite will be given a new tab item at the end of 
	 * the list.
	 * @param title The name to be shown on the new tab
	 */
	public void createComposite(String title) {
		CTabItem item;

		item = new CTabItem(scriptFolder, SWT.CLOSE);
		item.setText(title);
		
		Composite parent = new Composite(scriptFolder, SWT.NONE);
		parent.setBackground(new Color(parent.getDisplay(), 0, 0, 255));
		DashboardComposite comp = new DashboardComposite(parent, SWT.BORDER);
		IPreferenceStore p = DashboardPlugin.getDefault().getPreferenceStore();
		int delay = p.getInt(DashboardPreferenceConstants.P_DASHBOARD_UPDATE_DELAY);
		updater = new UpdateManager(delay);
		composites.add(comp);
		moduleNames.add(title);
		item.setControl(parent);

		scriptFolder.setSelection(item);
		fireTabOpenEvent();
	}

 /*
	 * This method creates the layout for the view.  It makes use of 
	 * <code>DashboardComposite</code> to handle positioning of all of the graphs.
	 * @param parent The composite that contains all elements added by this view.
	 */
/*	public void createPartControl(Composite parent) {
		composite[CompositeNo] = new DashboardComposite(parent, SWT.BORDER);
		IPreferenceStore p = DashboardPlugin.getDefault().getPreferenceStore();
		int delay = p.getInt(DashboardPreferenceConstants.P_DASHBOARD_UPDATE_DELAY);
		updater = new UpdateManager(delay);
	}*/
	
	public void createPartControl(Composite parent) {
		LogManager.logDebug("Start createPartControl: parent-" + parent, this); //$NON-NLS-1$
	     //bar.pack();
		FormData data2 = new FormData();
		data2.left = new FormAttachment(0, 0);
		data2.top = new FormAttachment(0, 0);
		data2.right = new FormAttachment(100, 0);
		data2.bottom = new FormAttachment(100, 0);
		Composite cmpGraph = new Composite(parent, SWT.NONE);
		cmpGraph.setLayoutData(data2);
		
		//This is for the tab view
		cmpGraph.setLayout(new FormLayout());
		FormData data3 = new FormData();
		data3.left = new FormAttachment(0, 0);
		data3.top = new FormAttachment(0, 0);
		data3.right = new FormAttachment(100, 0);
		data3.bottom = new FormAttachment(6, 0);
	//	while (DashboardModuleLocator.moduleNames == null) {}
	//	allModuleNames = new ArrayList<String>(DashboardModuleBrowserView.getmoduleNames());
	
		
	/*	Composite toolbar = new Composite(cmpGraph, SWT.NONE);
		Button modbutton = new Button(toolbar,SWT.PUSH);
	//	modbutton.setBackground(new Color(Display.getCurrent(), IGraphColorConstants.COLORS[0]));
		modbutton.setText(allModuleNames.get(1));
		//modbutton.setText("text0");
		modbutton = new Button(toolbar,SWT.PUSH);
		//modbutton.setBackground(new Color(Display.getCurrent(), IGraphColorConstants.COLORS[1]));
		modbutton.setText(allModuleNames.get(0));
		for (int i = 2; i < 5; i++) {
		      modbutton = new Button(toolbar, SWT.PUSH);
		      modbutton.setText(allModuleNames.get(i));
		     // modbutton.setBackground(new Color(Display.getCurrent(), IGraphColorConstants.COLORS[i]));
		    }
		toolbar.setLayout(new RowLayout());
		toolbar.setLayoutData(data3);
		toolbar.pack();*/
		
	  /* ToolBar bar = new ToolBar(cmpGraph, SWT.BORDER);
	      ToolItem item = new ToolItem(bar, SWT.PUSH);
	      item.setText(allModuleNames.get(1));
	      item = new ToolItem(bar, SWT.PUSH);
	      item.setText(allModuleNames.get(0));
	
		    for (int i = 2; i < allModuleNames.size(); i++) {
		      item = new ToolItem(bar, SWT.PUSH);
		      item.setText(allModuleNames.get(i));
		    }
		 bar.setLayoutData(data3);
		 bar.pack(); */
	

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
				composites.remove(scriptFolder.indexOf((CTabItem)e.item));
		    	moduleNames.remove(scriptFolder.indexOf((CTabItem)e.item));
				fireTabCloseEvent();
			}
		});
		
		Display display = parent.getShell().getDisplay();
		
		/*scriptFolder.setSelectionBackground(new Color[] {
		        display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW),
		        display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
		        display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW)}, new int[] { 50,
		        100});*/
		
         scriptFolder.setSelectionBackground(display.getSystemColor(SWT.COLOR_GRAY));
         //scriptFolder.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
         scriptFolder.setMaximizeVisible(true);
         scriptFolder.setSelectionForeground(display.getSystemColor(SWT.COLOR_BLACK));
         
		LogManager.logDebug("End createPartControl", this); //$NON-NLS-1$
	}


	public void setFocus() {}

	/*public Composite getComposite() {
		return composite.deadComposite;
	}*/
	
	/**
	 * This method creates a new graph in the view.  It creates a wrapper for the graph
	 * and then adds an update listener to the graph, and finally registers it with
	 * the composite.
	 * @param gd The data needed to create a new graph
	 * @param ds The dataset used to store all script output data
	 * @return The newly create graph component.
	 */
/*	public DashboardAdapter createGraph(GraphData gd, IDataSet ds) {
		DashboardAdapter adapter = new DashboardAdapter(composite, gd, ds);
		updater.addUpdateListener(adapter.getGraph());
		composite.add(adapter);
		
		return adapter;
	}*/
	/**
	 * This method creates a new graph in the view.  It creates a wrapper for the graph
	 * and then adds an update listener to the graph, and finally registers it with
	 * the composite.
	 * @param gd The data needed to create a new graph
	 * @param ds The dataset used to store all script output data
	 * @return The newly create graph component.
	 */
	public DashboardAdapter createGraph(GraphData gd, IDataSet ds, String moduleName) {
		 Display disp = PlatformUI.getWorkbench().getDisplay();
		  Cursor cursor = new Cursor(disp, SWT.CURSOR_WAIT);
		  PlatformUI.getWorkbench().getDisplay().getActiveShell().setCursor(cursor);
   //    while (ds.getRowCount() <= 0 ) {}
		int index = searchModuleNames(moduleName);
	
		DashboardAdapter adapter = null;
		if (index == -1)
		{
				createComposite(moduleName);
			index = searchModuleNames(moduleName);
		}
		adapter = new DashboardAdapter(composites.get(index), gd, ds, moduleName);
		updater.addUpdateListener(adapter.getGraph());
		composites.get(index).add(adapter);
		  PlatformUI.getWorkbench().getDisplay().getActiveShell().setCursor(null);
		  cursor.dispose();
		return adapter;
	}
	
	private int searchModuleNames(String moduleName) {
		for (int i = 0; i<moduleNames.size();i++)
		{
			
			if (moduleName.equals(moduleNames.get(i))) return i;
		}
		return -1;
	}

	public UpdateManager getUpdater() {
		return updater;
	}

	public void removeGraph(DashboardAdapter graph, String moduleName) {
		int index = searchModuleNames(moduleName);
		composites.get(index).remove(graph);
		graph.dispose();
		/*if (composites.get(index).getusedSlots() == 0)
		{
			composites.remove(index);
			moduleNames.remove(index);
			tabListeners.remove(index);
			fireTabCloseEvent();
		}*/
	}
	
	public void closeComposite(String moduleName)
	{
		int index = searchModuleNames(moduleName);
		if (composites.get(index).getusedSlots() <= 0)
		{
		scriptFolder.getItem(index).dispose();
		moduleNames.remove(index);
		composites.remove(index);
		}
	}
	
	public void addTabListener(ITabListener listener) {
		tabListeners.add(listener);
	}

	public void removeTabListener(ITabListener listener) {
		tabListeners.remove(listener);
	}

	private void fireTabCloseEvent() {
		for(int i=0; i<tabListeners.size(); i++)
			tabListeners.get(i).tabClosed();
	}
	
	
	private void fireTabOpenEvent() {
		for(int i=0; i<tabListeners.size(); i++)
			tabListeners.get(i).tabOpened();
	}

	private void fireTabChangedEvent() {
		for(int i=0; i<tabListeners.size(); i++)
			tabListeners.get(i).tabChanged();
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
		if(null != composites)
			composites.removeAll(composites);
		composites = null;
		scriptFolder = null;
		if(null != tabListeners)
			tabListeners.removeAll(tabListeners);
		tabListeners = null;
		LogManager.logDebug("End dispose:", this); //$NON-NLS-1$
		
	}
	
	public static void addmodulename(String str)
	{
		if (allModuleNames == null) allModuleNames = new ArrayList<String>();
		
		allModuleNames.add(str);
	}
	
	private ArrayList<DashboardComposite> composites;
	private ArrayList<String> moduleNames;
	public static ArrayList<String> allModuleNames = null;
	private ArrayList<ITabListener> tabListeners;
	private UpdateManager updater;
	private CTabFolder scriptFolder;
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardView";
	
}



