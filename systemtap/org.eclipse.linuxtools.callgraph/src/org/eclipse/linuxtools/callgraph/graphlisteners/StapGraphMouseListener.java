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
package org.eclipse.linuxtools.callgraph.graphlisteners;

import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.callgraph.StapGraph;
import org.eclipse.linuxtools.callgraph.StapNode;
import org.eclipse.linuxtools.callgraph.core.FileFinderOpener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.zest.core.widgets.GraphNode;

@SuppressWarnings("unused")
public class StapGraphMouseListener implements MouseListener {
	private int x;
	private int y;
	private StapGraph graph;
	private StapGraphMouseMoveListener listener;
	private StapGraphFocusListener focus;
	private StapGraphMouseExitListener exitListener;

	public StapGraphMouseListener(StapGraph g) {
		this.graph = g;
		listener = new StapGraphMouseMoveListener(graph);
		focus = new StapGraphFocusListener(listener);
		exitListener = new StapGraphMouseExitListener(listener);
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (e.stateMask == SWT.CONTROL) {
			controlDoubleClick();
			return;
		}
		
		
		if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_RADIAL) {
			StapNode node = getNodeFromSelection();
			if (node == null)
				return;
			
			graph.getTreeViewer().collapseToLevel(node.getData(), 0);
			graph.getTreeViewer().expandToLevel(node.getData(), 0);
			graph.getTreeViewer().setSelection(new StructuredSelection(node.getData()));

			int id = node.getData().id;

			graph.scale = 1;
//			graph.setCollapseMode(true);
			// Redraw in the current mode with the new id as the center
			// The x,y parameters to draw() are irrelevant for radial mode
			graph.draw(id);

			// Unhighlight the center node and give it a normal colour
			node = graph.getNode(id);
			node.unhighlight();
			if (graph.getNodeData(id).isMarked())
				node.setBackgroundColor(StapGraph.CONSTANT_MARKED);
			else
				node.setBackgroundColor(graph.DEFAULT_NODE_COLOR);
			return;
		} else {

			StapNode node = getNodeFromSelection();
			if (node == null)
				return;
			
			unhighlightall(node);
			graph.setSelection(null);

			// Draw in current modes with 'id' at the top
			int id = node.getData().id;
			graph.draw(id);
		}

		graph.setSelection(null);
	}


	@Override
	public void mouseDown(MouseEvent e) {
		if (graph.getProjectionist() != null) {
			graph.getProjectionist().pause();
		}
//		MP.println("You clicked: " + e.x + ", " + e.y); //$NON-NLS-1$ //$NON-NLS-2$
//		MP.println("Convert to control: " + graph.toControl(e.x, e.y).x + ", " //$NON-NLS-1$ //$NON-NLS-2$
//				+ graph.toControl(e.x, e.y).y);
//		MP.println("Convert to display: " + graph.toDisplay(e.x, e.y).x + ", " //$NON-NLS-1$ //$NON-NLS-2$
//				+ graph.toDisplay(e.x, e.y).y);
//		MP.println("Bounds: " + graph.getBounds().width + ", " //$NON-NLS-1$ //$NON-NLS-2$
//				+ graph.getBounds().height);
		mouseDownEvent(e.x, e.y);
	}

	@Override
	public void mouseUp(MouseEvent e) {
			mouseUpEvent();
	}

	private void unhighlightall(StapNode n) {
		int id = n.id;
		List<Integer> callees = null;

		if (graph.isCollapseMode())
			callees = graph.getNodeData(id).collapsedChildren;
		else
			callees = graph.getNodeData(id).children;
		
		if (callees == null)
			return;
		
		for (int subID : callees) {
			if (graph.getNode(subID) != null)
				graph.getNode(subID).unhighlight();
		}

		if (graph.getParentNode(id) != null) {
			graph.getParentNode(id).unhighlight();
		}
		n.unhighlight();
	}
	
	
	@SuppressWarnings("unchecked")
	private StapNode getNodeFromSelection() {
		List<GraphNode> stapNodeList = graph.getSelection();
		if (stapNodeList.isEmpty() || stapNodeList.size() != 1) {
			graph.setSelection(null);
			return null;
		}

		StapNode node = null;
		if (stapNodeList.get(0) instanceof StapNode) {
			node = (StapNode) stapNodeList.remove(0);
		} else {
			graph.setSelection(null);
			return null;
		}
		return node;
	}
	
	@SuppressWarnings("unchecked")
	private GraphNode getAggregateNodeFromSelection() {
		List<GraphNode> graphNodeList = graph.getSelection();
		if (graphNodeList.isEmpty() || graphNodeList.size() != 1) {
			graph.setSelection(null);
			return null;
		}

		GraphNode node = null;
		if (graphNodeList.get(0) instanceof GraphNode) {
			node = (GraphNode) graphNodeList.remove(0);
		} else {
			graph.setSelection(null);
			return null;
		}
		return node;
	}
	
	public void controlDoubleClick() {
		if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_AGGREGATE) {
			GraphNode node = getAggregateNodeFromSelection();
			
			if (node == null)
				return;
			
			String functionName = (String) node.getData("AGGREGATE_NAME"); //$NON-NLS-1$
			FileFinderOpener.findAndOpen(graph.getProject(), functionName);
			node.unhighlight();
		} else {
			StapNode node = getNodeFromSelection();
			
			if (node == null)
				return;

			int caller = node.getData().id;

			if (caller < graph.getFirstUsefulNode()) {
				// The only node that satisfies this condition should be
				// main
				caller = graph.getFirstUsefulNode();
			}
			FileFinderOpener.findAndOpen(graph.getProject(), graph.getNodeData(caller).name);
			node.unhighlight();
		}

		graph.setSelection(null);
	}
	
	public void mouseDownEvent(int x, int y) {
		List<?> list = graph.getSelection();
		if (list.size() < 1) {		
			listener.setPoint(x, y);
			listener.setStop(false);
			graph.addMouseMoveListener(listener);
			graph.addListener(SWT.MouseExit, exitListener);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void mouseUpEvent() {

		listener.setStop(true);
		graph.removeMouseMoveListener(listener);
		graph.removeListener(SWT.MouseExit, exitListener);

		List<StapNode> list = graph.getSelection();

		// ------------Debug information
		if (list.size() == 1) {
			int id;
			if (list.get(0) instanceof StapNode)
				id = list.get(0).id;
			else {
				graph.setSelection(null);
				return;
			}
			graph.setSelection(null);
//			MP.println("Clicked node " + graph.getData(id).name + " with id " //$NON-NLS-1$ //$NON-NLS-2$
//					+ id);
//			MP.println("    level: " + graph.getData(id).levelOfRecursion); //$NON-NLS-1$
//			MP.println("    called: " + graph.getData(id).called); //$NON-NLS-1$
//			MP.println("    caller: " + graph.getData(id).caller); //$NON-NLS-1$
//			MP.println("    copllapsedCaller: " //$NON-NLS-1$
//					+ graph.getData(id).collapsedCaller);
//			MP.println("    callees: " + graph.getData(id).callees.size()); //$NON-NLS-1$
//			MP.println("    position: " + graph.getNode(id).getLocation().x //$NON-NLS-1$
//					+ ", " + graph.getNode(id).getLocation().y); //$NON-NLS-1$

			// ------------Highlighting
			if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_TREE 
					|| graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_LEVEL) {
				for (StapNode n : (List<StapNode>) graph.getNodes()) {
					unhighlightall(n);
				}

				List<Integer> callees = null;

				if (graph.isCollapseMode())
					callees = graph.getNodeData(id).collapsedChildren;
				else
					callees = graph.getNodeData(id).children;

				for (int subID : callees) {
					if (graph.getNode(subID) != null)
						graph.getNode(subID).highlight();
				}

				if (graph.getParentNode(id) != null) {
					graph.getParentNode(id).highlight();
				}
//				graph.setSelection(null);
				graph.getNode(id).highlight();

				return;
			}

		}

		else if (list.size() == 0 && ! (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_AGGREGATE)) {
			for (StapNode n : (List<StapNode>) graph.getNodes()) {
				unhighlightall(n);
			}

		}

//		else {
//			for (StapNode n : list) {
//				unhighlightall(n);
//			}
//		}
//
//		graph.setSelection(null);
	}
};
