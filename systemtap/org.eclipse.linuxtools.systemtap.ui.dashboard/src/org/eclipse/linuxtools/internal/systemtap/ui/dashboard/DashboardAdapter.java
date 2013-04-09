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

package org.eclipse.linuxtools.internal.systemtap.ui.dashboard;

import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.actions.StopGraphAction;
import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.views.DashboardView;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardGraphData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.GraphTreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * This class is used to contain a graph for the DashboardComposite.  The class
 * provides a windowing framework that allows the user to close the graph at
 * will and to toggle whether or not to display the graph configuration settings.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class DashboardAdapter {
	/**
	 * This constructor sets up all of the basic internal components for class.
	 * @param parent This is the component that will act as the parent container
	 * @param gd The data that will be used to construct the internal graph
	 * @param ds The data used to populate the graph.
	 */
	public DashboardAdapter(DashboardComposite parent, GraphData gd, IDataSet ds, String moduleName) {
		folder = new CTabFolder(parent.deadComposite, SWT.ALL);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 0; layout.marginWidth = 0;
		parent.deadComposite.setLayout(layout);

		FormData data = new FormData();
		data.left = new FormAttachment(0,0);
		data.top = new FormAttachment(0,0);
		data.right = new FormAttachment(100,0);
		data.bottom = new FormAttachment(100,0);

		folder.setLayoutData(folder);
		folder.setLayout(new FormLayout());

		ToolBar toolBar = new ToolBar (folder, SWT.FLAT | SWT.BORDER);
		min = new ToolItem(toolBar, SWT.PUSH);
		min.setImage(expandImage);
		close = new ToolItem(toolBar, SWT.PUSH);
		close.setImage(closeImage);
		max = new ToolItem(toolBar, SWT.PUSH);
		max.setImage(maxImage);
		toolBar.pack ();
		folder.setTopRight(toolBar);
         modulename = moduleName;
		resetDND(null, null);

		createGraph(gd, ds);
		wireButtons(parent);
	}

	/**
	 * This method creates the internal graph that will be displayed in this container.
	 * @param gd The graph data used to specify the graph that will be created.
	 * @param ds The data set that is used to populate the graph.
	 */
	private void createGraph(GraphData gd, IDataSet ds) {
		//Setup canvas
		CTabItem item = new CTabItem(folder, SWT.NONE);
		Composite c = new Composite(folder, SWT.NONE);
		FormData data = new FormData();
		data.left = new FormAttachment(0,0);
		data.top = new FormAttachment(0,0);
		data.left = new FormAttachment(100,0);
		data.right = new FormAttachment(100,0);
		c.setLayoutData(data);
		c.setLayout(new FormLayout());
		item.setControl(c);
		folder.setSelection(item);

		//Create graph
		gc = new GraphComposite(c, SWT.NONE, gd, ds);
		gc.configure(false);
		FormData fd = new FormData();
		fd.left = new FormAttachment(0,0);
		fd.right = new FormAttachment(100,0);
		fd.top = new FormAttachment(0,0);
		fd.bottom = new FormAttachment(100,0);
		gc.setLayoutData(fd);
	}

	/**
	 * This method resets all Drag And Drop information.
	 * @param drag This is the object that is being moved
	 * @param drop This is the object that occupies the desired location of the first object.
	 */
	public void resetDND(DragSourceListener drag, DropTargetListener drop) {
		if(drag != null) {
			dragSource.removeDragListener(drag);
		}
		if(drop != null) {
			dropTarget.removeDropListener(drop);
		}
		if(dragSource != null) {
			dragSource.dispose();
		}
		if(dropTarget != null) {
			dropTarget.dispose();
		}
		dragSource = new DragSource(folder, DND.DROP_MOVE);
		dragSource.setTransfer(types);
		dropTarget = new DropTarget(folder, DND.DROP_MOVE | DND.DROP_DEFAULT);
		dropTarget.setTransfer(types);
	}

	/**
	 * Creates and addes the action listeners for the buttons.
	 * @param comp The composite that is containing this class.
	 */
	private void wireButtons(DashboardComposite comp) {
		SelectionListener listener = new DashboardShimButtonController(this, comp);
		close.addSelectionListener(listener);
		min.addSelectionListener(listener);
		max.addSelectionListener(listener);
	}

	/**
	 * Updates the containers parent with the provided composite.
	 * @param c The new composite that will serve as the parent.
	 */
	public void setParent(Composite c) {
		folder.setParent(c);
	}

	/**
	 * Changes the layout information for this class.
	 * @param o The new layout informatino for the class.
	 */
	public void setLayoutData(Object o) {
		folder.setLayoutData(o);
	}

	/**
	 * Sets whether or not the graph is visible
	 * @param b Visibility flag.
	 */
	public void setVisible(boolean b) {
		folder.setVisible(b);
	}

	/**
	 * Returns the internal graph that is being displayed
	 * @return The graph that is held by this class.
	 */
	public AbstractChartBuilder getGraph() {
		return gc.getCanvas();

	}

	/**
	 * Closes this class and removes it from being displayed in the parent composite.
	 * Also updates its display information in the ActiveModuleBrowserView.
	 */
	private void closeShim() {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ActiveModuleBrowserView.ID);
		ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
		TreeNode root = (TreeNode)ambv.getViewer().getTree().getData();
		TreeNode node;
		GraphTreeNode child;
		for(int j,i=0; i<root.getChildCount(); i++) {
			node = root.getChildAt(i);
			for(j=0; j<node.getChildCount(); j++) {
				child = (GraphTreeNode)node.getChildAt(j);
				if(this == ((DashboardGraphData)child.getData()).adapter) {
					StopGraphAction sga = new StopGraphAction();
					sga.run(child);
					return;
				}
			}
		}
	}

	/**
	 * Removes all internal references in this class.  Nothing should make any references
	 * to anyting in this class after calling the dispose method.
	 */
	public void dispose() {
		if(null != folder) {
			folder.dispose();
		}
		folder = null;
		if(null != close) {
			close.dispose();
		}
		close = null;
		if(null != min) {
			min.dispose();
		}
		min = null;
		if(null != dragSource) {
			dragSource.dispose();
		}
		dragSource = null;
		if(null != dropTarget) {
			dropTarget.dispose();
		}
		dropTarget = null;
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DashboardView.ID);
		((DashboardView)ivp).getUpdater().removeUpdateListener(getGraph());
		if(null != gc && !gc.isDisposed()) {
			gc.dispose();
		}
		gc = null;
		types = null;
	}

	/**
	 * This class handles the button clicks for the three buttons on the window bar.  It
	 * supports closing the window, as well as toggling whether or not to display the
	 * Graph display options.
	 * @author Henry Hughes
	 * @author Ryan Morse
	 */
	private class DashboardShimButtonController extends SelectionAdapter {
		DashboardAdapter shim;
		DashboardComposite composite;
		boolean maximized = false;
		public DashboardShimButtonController(DashboardAdapter shim, DashboardComposite composite) {
			this.shim = shim;
			this.composite = composite;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(e.widget == close) {
				closeShim();
			}

			if(e.widget == min) {
				Composite c = (Composite)folder.getSelection().getControl();
				Control[] children = c.getChildren();
				for(int i = 0; i < children.length; i++) {
					if(children[i] instanceof GraphComposite) {
						GraphComposite gc = (GraphComposite)children[i];
						gc.configure(!gc.isSidebarVisible());
						if(gc.isSidebarVisible()) {
							min.setImage(collapseImage);
						} else {
							min.setImage(expandImage);
						}
					}
				}
			}
			if(e.widget == max) {
				Composite c = (Composite) folder.getSelection().getControl();
				Control[] children = c.getChildren();
				for (int i = 0; i < children.length; i++) {
					if (children[i] instanceof GraphComposite) {
						if (!maximized) {
							composite.maximize(shim);
							max.setImage(restoreImage);
							maximized = true;
						} else {
							composite.restore();
							max.setImage(maxImage);
							maximized = false;
						}
					}
				}
			}

		}

	}

	public String getmodulename() {
		return modulename;
	}

	public CTabFolder folder;
	public DragSource dragSource;
	public DropTarget dropTarget;
	private ToolItem close, min, max;
	private GraphComposite gc;
	private String modulename;
	Transfer[] types = new Transfer[] { TextTransfer.getInstance() };

	private static final Image closeImage = DashboardPlugin.getImageDescriptor("icons/actions/graph/close.gif").createImage(); //$NON-NLS-1$
	private static final Image collapseImage = DashboardPlugin.getImageDescriptor("icons/actions/graph/collapse.gif").createImage(); //$NON-NLS-1$
	private static final Image expandImage = DashboardPlugin.getImageDescriptor("icons/actions/graph/expand.gif").createImage(); //$NON-NLS-1$
	private static final Image maxImage = DashboardPlugin.getImageDescriptor("icons/actions/graph/maximize.gif").createImage(); //$NON-NLS-1$
	private static final Image restoreImage = DashboardPlugin.getImageDescriptor("icons/actions/graph/restore.gif").createImage(); //$NON-NLS-1$

}
