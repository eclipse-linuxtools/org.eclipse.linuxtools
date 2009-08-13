/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.localgui.graphing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.systemtap.localgui.graphing.treeviewer.StapTreeContentProvider;
import org.eclipse.linuxtools.systemtap.localgui.graphing.treeviewer.StapTreeDoubleClickListener;
import org.eclipse.linuxtools.systemtap.localgui.graphing.treeviewer.StapTreeLabelProvider;
import org.eclipse.linuxtools.systemtap.localgui.graphing.treeviewer.StapTreeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * Initializes and runs a StapGraph and TreeViewer
 * 
 * @author chwang
 *
 */
public class GraphUIJob extends UIJob{
	private StapGraph g;
	private StapGraphParser parser;
	private TreeViewer treeview;
	private static int treeSize = 200;
	private static int textSize = 300;


	public StapGraph getGraph() {
		return g;
	}
	
	public GraphUIJob(String name, StapGraphParser parser) {
		super(name);
		//CREATE THE SHELL
		this.parser = parser;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		
		//-------------Initialize shell, menu
//		System.out.println("Running in UI Thread");
		Display d = Display.getCurrent();
		int screenWidth = d.getPrimaryMonitor().getBounds().width;
		int screenHeight = d.getPrimaryMonitor().getBounds().height - 280;
		treeSize = screenWidth/6;
		textSize = screenWidth/4;

		
		//OPEN UP THE SYSTEMTAPVIEW IF IT IS NOT ALREADY OPEN
		//GIVE IT THE FOCUS
		SystemTapView.forceDisplay();
		
		Composite treeComp = new Composite(SystemTapView.masterComposite, SWT.NONE);
		
		GridData gd = new GridData(treeSize, screenHeight);
		treeComp.setLayout(new FillLayout());
		treeComp.setLayoutData(gd); 
		treeview = new TreeViewer(treeComp);
		StapTreeListener stl = new StapTreeListener(treeview.getTree().getHorizontalBar());
		treeview.addTreeListener(stl);
		
		
		Composite graphComp = new Composite(SystemTapView.masterComposite, SWT.NONE);
		GridData graphGridData = new GridData(screenWidth - treeSize - textSize, screenHeight);
		graphComp.setLayout(new FillLayout());
		graphComp.setLayoutData(graphGridData);

		
		g = new StapGraph(graphComp, SWT.NONE, treeview);

		
		//-------------Load graph data
		g.loadData(SWT.NONE, 0, StapGraph.CONSTANT_TOP_NODE_NAME, parser.timeMap.get(0), 1, -1, false, ""); //$NON-NLS-1$
		boolean marked = false;
		String msg;
		
		
	    for (int id_parent : parser.serialMap.keySet()) {
	    	
			for (int id_child : parser.outNeighbours.get(id_parent)) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				marked = false;
				msg = ""; //$NON-NLS-1$
				if (id_child != -1) {
					
					if (parser.markedMap.keySet() != null) {
						if (parser.markedMap.keySet().contains(id_child)) {
							msg = parser.markedMap.get(id_child);							
							parser.markedMap.keySet().remove((Integer)id_child);
							marked = true;
						}
					}
					
					if (parser.timeMap.get(id_child) == null){						
						g.loadData(SWT.NONE, id_child, parser.serialMap
								.get(id_child), parser.timeMap.get(0),
								1, id_parent, marked,msg);
					}else{
						g.loadData(SWT.NONE, id_child, parser.serialMap
								.get(id_child), parser.timeMap.get(id_child),
								1, id_parent, marked,msg);
					}
				}
			}
		}

	    
	    g.aggregateCount = parser.countMap;
	    g.aggregateTime = parser.cumulativeTimeMap;

	    //Set total time (automatically seeks highest useful node)
	    g.setTotalTime();
	    
	    //-------------Finish initializations
	    //Generate data for collapsed nodes
	    g.recursivelyCollapseAllChildrenOfNode(g.getTopNode());
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
	    initializeTree();
	    

	    g.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, StapGraph.CONSTANT_ANIMATION_SLOW, g.getFirstUsefulNode(),
	    		g.getBounds().width/2, 1);
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
	    g.setFocus();
	    
	    SystemTapView.setValues(graphComp, treeComp, g, parser);
	    SystemTapView.createPartControl();
	    
		return Status.OK_STATUS;
	}
	
	
	/**
	 * Initialize the treeviewer with data from the graph
	 */
	public void initializeTree() {
		StapTreeContentProvider scp = new StapTreeContentProvider();
		scp.setGraph(g);
		treeview.setContentProvider(scp);
		StapTreeLabelProvider prov = new StapTreeLabelProvider();
	    treeview.setLabelProvider(prov);
	    treeview.setInput(g.getData(g.getTopNode()));
	    treeview.addDoubleClickListener(new StapTreeDoubleClickListener(treeview, g));
	}
	
	/**
	 * Returns number of StapData objects created 
	 * @return
	 */
	public int getNumberOfDataNodes() {
		return g.getDataMapSize();
	}
	
	
	/**
	 * For easier JUnit testing only. Allows public access to run method without scheduling an extra job.
	 *  
	 * @param m
	 * @return
	 */
	public IStatus testRun(IProgressMonitor m) {
		return runInUIThread(m);
	}
	
	
}
