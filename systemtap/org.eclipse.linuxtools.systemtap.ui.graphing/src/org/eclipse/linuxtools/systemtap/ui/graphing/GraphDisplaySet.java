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

package org.eclipse.linuxtools.systemtap.ui.graphing;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.GraphingPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.graphing.preferences.GraphingPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.datadisplay.DataGrid;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetFactory;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.SelectGraphAndSeriesWizard;
import org.eclipse.linuxtools.systemtap.structures.UpdateManager;
import org.eclipse.linuxtools.systemtap.structures.listeners.ITabListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;



/**
 * This class is used to contain all of the graphing components that can
 * be displayed as individual tabs in a single location.
 * @author Ryan Morse
 * @since 2.0
 */
public class GraphDisplaySet {

	public GraphDisplaySet(Composite parent, IDataSet data) {
		IPreferenceStore p = GraphingPlugin.getDefault().getPreferenceStore();
		int delay = p.getInt(GraphingPreferenceConstants.P_GRAPH_UPDATE_DELAY);

		dataSet = data;
		updater = new UpdateManager(delay);
		createPartControl(parent);

		builders = new ArrayList<AbstractChartBuilder>();
		tabListeners = new ArrayList<ITabListener>();
	}

	/**
	 * This method creates the framework for what will be displayed by this dialog box.
	 * @param parent The composite that will contain all the elements from this dialog
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FormLayout());
		FormData data1 = new FormData();
		Composite cmpCoolBar = new Composite(parent, SWT.NONE);
		data1.left = new FormAttachment(0, 0);
		data1.top = new FormAttachment(0, 0);
		data1.right = new FormAttachment(100, 0);
		data1.bottom = new FormAttachment(0, 10);
		cmpCoolBar.setLayoutData(data1);

		FormData data2 = new FormData();
		data2.left = new FormAttachment(0, 0);
		data2.top = new FormAttachment(cmpCoolBar);
		data2.right = new FormAttachment(100, 0);
		data2.bottom = new FormAttachment(100, 0);
		Composite cmpGraph = new Composite(parent, SWT.NONE);
		cmpGraph.setLayoutData(data2);

		//This is for the tab view
		cmpGraph.setLayout(new FormLayout());

		folder = new CTabFolder(cmpGraph, SWT.NONE);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		folder.setLayoutData(data);

		listener = new ButtonClickListener();
		folder.addSelectionListener(listener);

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			public void close(CTabFolderEvent e) {
				int selected = folder.indexOf((CTabItem)e.item)-2;
				if(null != updater) {
					updater.removeUpdateListener(builders.get(selected));
				}
				builders.remove(selected);
				fireTabCloseEvent();
			}
		});

		//This is a tab/button for opening new graphs
		CTabItem newGraph = new CTabItem(folder, SWT.NONE);
		newGraph.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(GraphingPlugin.PLUGIN_ID, "icons/actions/new_wiz.gif").createImage()); //$NON-NLS-1$
		newGraph.setToolTipText(Localization.getString("GraphDisplaySet.CreateGraph")); //$NON-NLS-1$

		//Tab containing the data table
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Localization.getString("GraphDisplaySet.DataView")); //$NON-NLS-1$
		Composite c = new Composite(folder, SWT.NONE);
		GridLayout grid = new GridLayout();
		grid.marginHeight = 0;
		grid.marginWidth = 0;
		c.setLayout(grid);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		DataGrid table = DataSetFactory.getDataGrid(c, dataSet);
		if(null != updater) {
			updater.addUpdateListener(table);
		}
		table.getControl().setLayoutData(gd);
		item.setControl(c);
		folder.setSelection(item);
		lastSelectedTab = 1;
	}

	public IDataSet getDataSet() {
		return dataSet;
	}

	/**
	 * Finds the graph that is open in the current tab
	 * @return The graph that is currently visible on the screen
	 */
	public AbstractChartBuilder getActiveGraph() {
		if(0 == builders.size() || folder.getSelectionIndex() < 2) {
			return null;
		}
		return builders.get(folder.getSelectionIndex()-2);
	}

	public void setFocus() {}

	/**
	 * Removes all internal references in this class.  Nothing should make any references
	 * to anything in this class after calling the dispose method.
	 */
	public void dispose() {
		if(null != updater && updater.isRunning()) {
			updater.dispose();
		}
		updater = null;

		dataSet = null;
		if(null != folder && !folder.isDisposed()) {
			folder.removeSelectionListener(listener);
			folder.dispose();
			folder = null;
		}
		listener = null;

		builders.clear();
	}

	/**
	 * This class handles switching between tabs and creating new graphs.
	 * When the user selects the first tab a new dialog is displayed for
	 * them to select what they want to display for the new graph.
	 */
	public class ButtonClickListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent event) {
			CTabFolder folder = (CTabFolder)event.getSource();

			if(folder.getSelectionIndex() == 0) {
				folder.setSelection(lastSelectedTab);
				SelectGraphAndSeriesWizard wizard = new SelectGraphAndSeriesWizard(dataSet, null);
				IWorkbench workbench = PlatformUI.getWorkbench();
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
				dialog.create();
				dialog.open();

				GraphData gd = wizard.getGraphData();

				if(null != gd) {
					addGraph(gd);
				}
				wizard.dispose();
				fireTabOpenEvent();
			}
			lastSelectedTab = folder.getSelectionIndex();
			fireTabChangedEvent();
		}
	}

	public void addTabListener(ITabListener listener) {
		tabListeners.add(listener);
	}

	private void fireTabCloseEvent() {
		for(int i=0; i<tabListeners.size(); i++) {
			(tabListeners.get(i)).tabClosed();
		}
	}

	private void fireTabOpenEvent() {
		for(int i=0; i<tabListeners.size(); i++) {
			(tabListeners.get(i)).tabOpened();
		}
	}

	private void fireTabChangedEvent() {
		for(int i=0; i<tabListeners.size(); i++) {
			(tabListeners.get(i)).tabChanged();
		}
	}

	public void addGraph(GraphData gd) {
		CTabItem item = new CTabItem(folder, SWT.CLOSE);
		item.setText(GraphFactory.getGraphName(gd.graphID));
		GraphComposite gc = new GraphComposite(folder, SWT.FILL, gd, dataSet);
		gc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		folder.setSelection(item);

		AbstractChartBuilder g = gc.getCanvas();
		item.setControl(gc);

		if(null != g) {
			if(null != updater) {
				updater.addUpdateListener(g);
			}
			builders.add(g);
		}
	}

	private int lastSelectedTab;
	private IDataSet dataSet;
	private CTabFolder folder;
	private ButtonClickListener listener;
	private UpdateManager updater;
	private ArrayList<ITabListener> tabListeners;

	private ArrayList<AbstractChartBuilder> builders;
}
