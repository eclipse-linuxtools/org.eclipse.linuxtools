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

package org.eclipse.linuxtools.systemtap.local.callgraph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.linuxtools.systemtap.local.callgraph.graphlisteners.AutoScrollSelectionListener;
import org.eclipse.linuxtools.systemtap.local.core.PluginConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * Initializes and runs a StapGraph and TreeViewer within the SystemTap View
 * 
 * @author chwang
 *
 */
public class GraphUIJob extends UIJob{
	private StapGraph g;
	private StapGraphParser parser;
	private static int treeSize = 200;


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
		
		Display disp = Display.getCurrent();
		if (disp == null)
			disp = Display.getDefault();
		
		
		//-------------Initialize shell, menu
		treeSize = 200;

		
		//OPEN UP THE SYSTEMTAPVIEW IF IT IS NOT ALREADY OPEN
		CallgraphView.forceDisplay();
		
		Composite treeComp = CallgraphView.makeTreeComp(treeSize);
		Composite graphComp = CallgraphView.makeGraphComp();
		graphComp.setBackgroundMode(SWT.INHERIT_FORCE);
		
		
		//Create papa canvas
		Canvas papaCanvas = new Canvas(graphComp, SWT.BORDER);
		GridLayout papaLayout = new GridLayout(1, true);
		papaLayout.horizontalSpacing=0;
		papaLayout.verticalSpacing=0;
		papaLayout.marginHeight=0;
		papaLayout.marginWidth=0;
		papaCanvas.setLayout(papaLayout);
		GridData papaGD = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		papaGD.widthHint=160;
		papaCanvas.setLayoutData(papaGD);
		
		
		//Add first button
		Image image = new Image(disp, PluginConstants.PLUGIN_LOCATION+"icons/up.gif"); //$NON-NLS-1$
		Button up = new Button(papaCanvas, SWT.PUSH);
		GridData buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		buttonData.widthHint = 150;
		buttonData.heightHint = 20;
		up.setData(buttonData);
		up.setImage(image);
		
		
		//Add thumb canvas
		Canvas thumbCanvas = new Canvas(papaCanvas, SWT.NONE);
		
		
		//Add second button
		image = new Image(disp, PluginConstants.PLUGIN_LOCATION+"icons/down.gif"); //$NON-NLS-1$
		Button down = new Button(papaCanvas, SWT.PUSH);
		buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		buttonData.widthHint = 150;
		buttonData.heightHint = 0;
		down.setData(buttonData);
		down.setImage(image);

		
		//Initialize graph
		g = new StapGraph(graphComp, SWT.BORDER, treeComp, papaCanvas);
		g.setLayoutData(new GridData(this.getDisplay().getPrimaryMonitor().getBounds().width - 200,this.getDisplay().getPrimaryMonitor().getBounds().height - 200));

		up.addSelectionListener(new AutoScrollSelectionListener(
				AutoScrollSelectionListener.AutoScroll_up, g));
		down.addSelectionListener(new AutoScrollSelectionListener(
				AutoScrollSelectionListener.AutoScroll_down, g));
		
		
		//Initialize thumbnail
		GridData thumbGD = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		thumbGD.widthHint=160;
		thumbCanvas.setLayoutData(thumbGD);
		LightweightSystem lws = new LightweightSystem(thumbCanvas);
		ScrollableThumbnail thumb = new ScrollableThumbnail(g.getViewport());
		thumb.setSource(g.getContents());
		lws.setContents(thumb);

		//-------------Load graph data
		g.loadData(SWT.NONE, 0, StapGraph.CONSTANT_TOP_NODE_NAME, 1, 1, -1, false, ""); //$NON-NLS-1$
		boolean marked = false;
		String msg = ""; //$NON-NLS-1$
		
		
	    for (int id_parent : parser.serialMap.keySet()) {
	    	if (g.getData(id_parent) == null) {
				if (parser.markedMap.get(id_parent) != null) {
					marked = true;
					msg = parser.markedMap.get(id_parent);
				}
	    		g.loadData(SWT.NONE, id_parent, parser.serialMap.get(id_parent), parser.timeMap.get(id_parent),
	    				1, 0, marked, msg);
	    	}
	    	
			for (int id_child : parser.outNeighbours.get(id_parent)) {
				
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				
				marked = false;
				msg = ""; //$NON-NLS-1$
				if (parser.markedMap.get(id_child) != null) {
					marked = true;
					msg = parser.markedMap.get(id_child);
				}
				if (id_child != -1) {
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

	    //Set total time
	    g.setTotalTime(parser.totalTime);
	    
	    //-------------Finish initializations
	    //Generate data for collapsed nodes
	    g.recursivelyCollapseAllChildrenOfNode(g.getTopNode());
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
	    g.initializeTree();
	    g.setLastFunctionCalled(parser.lastFunctionCalled);
	    

		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
	    g.setFocus();
	    g.setCallOrderList(parser.callOrderList);
	    
	    g.setProject(parser.project);
	    
	    CallgraphView.setValues(graphComp, treeComp, g, parser);
	    CallgraphView.createPartControl();
	    g.draw(StapGraph.CONSTANT_DRAWMODE_RADIAL, StapGraph.CONSTANT_ANIMATION_SLOW,
	    		g.getFirstUsefulNode());
	    
		return Status.OK_STATUS;
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

