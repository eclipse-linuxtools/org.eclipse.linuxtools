/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.threadprofiler;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapView;
import org.eclipse.linuxtools.internal.threadprofiler.graphs.GraphModel;
import org.eclipse.linuxtools.internal.threadprofiler.graphs.MultiGraph;
import org.eclipse.linuxtools.internal.threadprofiler.graphs.ThreadGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

public class ThreadProfilerView extends SystemTapView {
	private static final int CPU_BUFFER = 0;
	private static final int MEM_BUFFER = 1;
	private static final int DISK_BUFFER = 2;
	private static final String HIDE = "Hide";
	private static final String SHOW = "Show";
//	private static final int START_WIDTH = 350;
//	private static final int START_HEIGHT = 200;
	private static final int START_X_POS = 30;
	private static final int MAX_REFRESH_RATE = 100;
	private long lastRefresh = 0l;
	private ArrayList<GraphModel> graphs;
	private ArrayList<ThreadGraph> threads;

	private Canvas graphCanvas;
	private GC graphGC;
//	private Canvas threadCanvas;
	private final static int GRAPH_SEPARATION = 40;
	private final static int VBAR_INCREMENT = 10;
	private int segments;
	private int selector = 0;
	private IMenuManager dropMenu;
	private final SystemTapView currentView = this;
	private int oldHeight = 100;
	private static final Integer SHELL_ID_VALUE = new Integer(351);
	private static final String SHELL_ID_PROPERTY = "ThreadShellIDProperty";
	private int firstThread;
	
	private boolean pause = false;
	private ImageDescriptor playImage= ThreadProfilerPlugin.getImageDescriptor("icons/perform.png"); //$NON-NLS-1$
	private ImageDescriptor pauseImage= ThreadProfilerPlugin.getImageDescriptor("icons/pause.gif"); //$NON-NLS-1$
	private Action playPause = new Action("Pause", pauseImage) {
		@Override
		public void run() {
			pause = !pause;
			//Toggle image
			ImageDescriptor tmp = pause ? playImage : pauseImage;
			String txt = pause ? "Play" : "Pause";
			this.setImageDescriptor(tmp);
			this.setText(txt);
		}
	};

	/*
	 * We have two frames of reference: 
	 * 
	 *  1. Pixel frame (absolute frame)
	 *  2. Axis frame (relative frame)
	 *  
	 *  The transformation from 1 to 2 is a coordinate shift and scale
	 *  
	 */
	
	
	@Override
	protected boolean createOpenAction() {
		return false;
	}

	@Override
	protected boolean createOpenDefaultAction() {
		return false;
	}

	@Override
	public IStatus initializeView(Display targetDisplay,
			IProgressMonitor monitor) {
		if (graphs == null)
			graphs = new ArrayList<GraphModel>();
		if (threads == null)
			threads = new ArrayList<ThreadGraph>();
		graphs.clear();
		threads.clear();
		segments = 0;

		//FIXME: these following lines do not work on Juno as they are referencing
		//an internal class that has changed from Indigo to Juno.  Not sure what
		//this code is attempting.  For now, commenting out.
//		IViewReference ref = getSite().getPage().
//			findViewReference("org.eclipse.linuxtools.threadprofiler.threadprofilerview");
//		((WorkbenchPage)getSite().getPage()).getActivePerspective().
//			getPresentation().detachPart(ref);
//		currentView.getViewSite().getShell().setSize(START_WIDTH, START_HEIGHT);
//		currentView.getViewSite().getShell().setData(SHELL_ID_PROPERTY, SHELL_ID_VALUE);
		
		
		//Create dataSet for CPU
		createNewDataSet("CPU", GraphModel.CONSTANT_Y, "%");
		((MultiGraph) graphs.get(0)).addBuffer("Total");
		((MultiGraph) graphs.get(0)).addBuffer("IO Block");
		
		//Create dataSet for memory
		createNewDataSet("Memory", GraphModel.FLEXIBLE_Y, "bytes");
		((MultiGraph) graphs.get(1)).addBuffer("Total");
		((MultiGraph) graphs.get(1)).addBuffer("Data");
		
		//Create dataSet for disk usage
		createNewDataSet("Disk", GraphModel.FLEXIBLE_Y, "bytes");
		((MultiGraph) graphs.get(2)).addBuffer("Total");
		((MultiGraph) graphs.get(2)).addBuffer("Read", MultiGraph.GRAPH_STYLE_LINE);
		((MultiGraph) graphs.get(2)).addBuffer("Write", MultiGraph.GRAPH_STYLE_LINE);
		
		if (dropMenu.getItems().length < graphs.size() + 1) {
			//the +1 is for the 'hide' item
			int counter = 0;
			for (GraphModel g : graphs) {
				final int count = counter;
				counter++;
				dropMenu.add(new Action(g.getTitle()) {
					@Override
					public void run(){
						selector = count;
						if (graphCanvas != null)
							graphCanvas.redraw();
					}
				});
			}
			final int count = counter;
			dropMenu.add(new Action("Threads") {
				@Override
				public void run() {
					selector = count;
					if (graphCanvas != null)
						graphCanvas.redraw();
				}
			});
		}
		

		return Status.OK_STATUS;
	}

	private void createNewDataSet(String name, int type, String units) {
		GraphModel graph = new MultiGraph(name, units, START_X_POS, 0, type);
		graphs.add(graph);
	}

	@Override
	public void setViewID() {
		viewID = "org.eclipse.linuxtools.threadprofiler.ThreadProfilerView";
	}

	@Override
	public void updateMethod() {
		if (graphCanvas != null && ! graphCanvas.isDisposed())
			graphCanvas.redraw();
//		threadCanvas.redraw();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		if (masterComposite != null)
			masterComposite.dispose();
		masterComposite = parent;
		
		FillLayout gl = new FillLayout(SWT.NONE);
		parent.setLayout(gl);
		
		addMenus(parent);
    	
    	Canvas graphComp =new Canvas(parent, SWT.BORDER | SWT.V_SCROLL);
    	graphComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    	
    	final ScrollBar vBar = graphComp.getVerticalBar ();
    	vBar.setIncrement(VBAR_INCREMENT);
    	vBar.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent(Event event) {
//				int size = vBar.getMaximum();
//				int sel = vBar.getSelection() - vBar.getMinimum();
//				System.out.println("Sel: " + sel);
//				System.out.println("Size: " + size);
//				firstThread = (int) (sel/size * threads.size() + 0.5);
				firstThread = (vBar.getSelection() - vBar.getMinimum())/VBAR_INCREMENT;
			}
    	});
    	
		graphCanvas = graphComp;
//    	graphCanvas.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
//    	gd = new GridData(GridData.FILL_BOTH);
//    	gd.grabExcessHorizontalSpace = true;
//    	graphCanvas.setLayoutData(gd);
    	graphGC = new GC(graphCanvas);

		graphCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				updateMethod();
			}
		});
		
	    graphCanvas.addPaintListener(new PaintListener() {
	        @Override
			public void paintControl(PaintEvent e) {
	        	long diff = System.currentTimeMillis() - lastRefresh;
	        	if (pause || (diff < MAX_REFRESH_RATE && diff > 0))
					try {
						Thread.sleep(diff);
					} catch (InterruptedException e1) {
						return;
					}
					
					
					Rectangle rect = graphCanvas.getBounds();
					Image buf = new Image(Display.getDefault(), graphCanvas.getBounds());
					GC gc = new GC(buf);
					gc.setClipping(rect);
	        	if (graphs != null && selector < graphs.size()) {
		        	//Double buffer!
		        	gc.setLineWidth(2);
	        		GraphModel graph = graphs.get(selector);
	        		graph.setHeight(rect.height - GraphModel.MAX_MARGIN - GraphModel.MAX_MARGIN/2);
	        		graph.setXOffset(rect.width/20);
	        		graph.setYOffset(rect.height - GraphModel.MAX_MARGIN);
	        		graph.draw(gc);
		        	graphGC.drawImage(buf, 0, 0);
		        	buf.dispose();
	        	} else if (threads != null) {
	        		if (threads == null)
						return;
	        		vBar.setMaximum(VBAR_INCREMENT*(threads.size() - 1));
		        	gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
	
	        		ArrayList<ThreadGraph> toRemove = new ArrayList<ThreadGraph>();
	        		int maxThreads = (int) (rect.height/GRAPH_SEPARATION + 0.5);
	        		
//	        		int counter = 0;
//					for (ThreadGraph tg : threads) {
//						counter++;
	        		if (firstThread >= threads.size() ) {
	        			firstThread = threads.size() - maxThreads;
	        		}
	        		
	        		if (firstThread < 0) {
	        			firstThread = 0;
	        		}
	        		
	        		for (int i = 0; i < maxThreads; i++) {
	        			if (i + firstThread >= threads.size())
	        				break;
	        			ThreadGraph tg = threads.get(i + firstThread);
	        			tg.setHeight(rect.height);
						tg.setYOffset(i*GRAPH_SEPARATION);
						gc.setLineWidth(2);
						tg.draw(gc);
						gc.setLineWidth(0);
						gc.drawText(tg.getTitle(), START_X_POS, i*GRAPH_SEPARATION + 10, true);
						if (tg.isEmpty())
							toRemove.add(tg);
	        		}
//					}
					//TODO: removeAll is an O(N^2) operation
					threads.removeAll(toRemove);
					graphGC.drawImage(buf, 0, 0);
					buf.dispose();
					System.out.println("-----------");
	        	}
	        }
	    }); 
    	
//    	ScrolledComposite threadComp =new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL);
//    	threadComp.setLayout (new GridLayout(1, true));
//    	threadComp.setBackground(white);
//    	GridData gd = new GridData(GridData.FILL_BOTH);
//    	gd.grabExcessHorizontalSpace = true;
//    	threadComp.setLayoutData(gd);
    	
//    	threadCanvas = new Canvas(parent, SWT.BORDER );
//    	threadCanvas.setBackground(white);
//    	gd = new GridData(GridData.FILL_BOTH);
//    	gd.grabExcessHorizontalSpace = true;
//    	threadCanvas.setLayoutData(gd);
//    	threadGC = new GC(threadCanvas);
//
//	    threadCanvas.addPaintListener(new PaintListener() {
//			@Override
//			public void paintControl(PaintEvent e) {
//				
//			}
//	    });
	    
	}
	
	private void addMenus(Composite parent) {

		Action hide = new Action(HIDE) {
			private void setHeight(int height) {
				Point size = currentView.getViewSite().getShell().getSize();
				Object o = currentView.getViewSite().getShell().getData(SHELL_ID_PROPERTY); 
				if ( o instanceof Integer && ((Integer) o).equals(SHELL_ID_VALUE)) {
					oldHeight = size.y;
					currentView.getViewSite().getShell().setSize(size.x, height);
				}
			}
			
			@Override
			public void run() {
				if (this.getText().equals(HIDE)) {
					this.setText(SHOW);
					graphCanvas.setVisible(false);
					currentView.layout();
					setHeight(100);
				} else {
					this.setText(HIDE);
					graphCanvas.setVisible(true);
					currentView.layout();
					setHeight(oldHeight);
				}
			}
		};
		
		
		//Icon i.e. toolbar manager
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		
		mgr.add(hide);
		mgr.add(playPause);
		
		//Dropdown manager
		dropMenu = getViewSite().getActionBars().getMenuManager();
		dropMenu.add(hide);

	}

	@Override
	public void setFocus() {
		//Do nothing
	}
	

	
	public void addDataPoints(int counter, String[] blargh) {
		if (segments < GraphModel.BUFFER_SIZE)
			segments++;
		if (blargh.length < 2)
			return;
		this.addPoint(new DataPoint(counter, Integer.parseInt(blargh[1]), 1), CPU_BUFFER, 0);
		
		if (blargh.length < 3)
			return;
		this.addPoint(new DataPoint(counter, Integer.parseInt(blargh[2]), 1), CPU_BUFFER, 1);
		
		if (blargh.length < 4)
			return;
		this.addPoint(new DataPoint(counter, Integer.parseInt(blargh[3]), 1), MEM_BUFFER, 0);
		
		if (blargh.length < 5)
			return;
		this.addPoint(new DataPoint(counter, Integer.parseInt(blargh[4]), 1), MEM_BUFFER, 1);
		
		if (blargh.length < 6)
			return;
		addPoint(new DataPoint(counter, Integer.parseInt(blargh[5]), 1), DISK_BUFFER, 0);
		
		if (blargh.length < 7)
			return;
		addPoint(new DataPoint(counter, Integer.parseInt(blargh[6]), 1), DISK_BUFFER, 1);
		
		if (blargh.length < 8)
			return;
		addPoint(new DataPoint(counter, Integer.parseInt(blargh[7]), 1), DISK_BUFFER, 2);
	}
	
	
	private void addPoint(DataPoint point, int dataSet, int subIndex) {
		if (graphs.size() < 1)
			return;
		graphs.get(dataSet).add(point, subIndex);
	}

	public void addThread(int tid, String line) {
		int i = threadExists(tid);
		if (i < 0) {
			ThreadGraph tg = new ThreadGraph(line, tid);
			tg.setXOffset(START_X_POS);
			for (int j = 0; j < segments; j++) {
				//Fill with empty segments
				tg.add(new DataPoint(0, 0, DataPoint.THREAD_INACTIVE), 0);
			}
			threads.add(tg);
			i = threads.size() - 1;
		}
		threads.get(i).addPoint();
	}
	

	public void tick() {
		// TODO Auto-generated method stub
		for (ThreadGraph tg : threads) {
			tg.tick();
		}
	}		
	
	/**
	 * Returns true if a thread with the given tid exists
	 * 
	 * @param tid
	 * @return
	 */
	private int threadExists(int tid) {
		for (int i = 0; i < threads.size(); i++) {
			if (threads.get(i).getTid() == tid) {
				return i;
			}
		}
		return -1;
	}



 }
