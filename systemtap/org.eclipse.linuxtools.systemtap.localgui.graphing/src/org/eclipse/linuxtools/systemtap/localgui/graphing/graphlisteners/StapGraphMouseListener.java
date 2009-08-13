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
package org.eclipse.linuxtools.systemtap.localgui.graphing.graphlisteners;

import java.util.List;

import org.eclipse.linuxtools.systemtap.localgui.core.MP;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraph;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.zest.core.widgets.GraphNode;

@SuppressWarnings("unused")
public class StapGraphMouseListener implements MouseListener {
	private int x;
	private int y;
	private StapGraph graph;
	private StapGraphMouseMoveListener listener;
	private StapGraphFocusListener focus;

	public StapGraphMouseListener(StapGraph g) {
		this.graph = g;
		listener = new StapGraphMouseMoveListener(graph);
		focus = new StapGraphFocusListener(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_RADIAL) {
			List<GraphNode> stapNodeList = graph.getSelection();
			if (stapNodeList.isEmpty() || stapNodeList.size() != 1) {
				graph.setSelection(null);
				return;
			}

			/*
			 * Check if the button is a StapButton (if not it must a StapNode).
			 * Then perform button action
			 */
			// StapButton tempButton = new StapButton (graph, SWT.NONE, -100,
			// -100, -100);
			//
			// if (tempButton.getClass() == stapNodeList.get(0).getClass()) {
			// StapButton button = (StapButton) stapNodeList.remove(0);
			// System.out.println("StapButton detected");
			// if (button.actionID == StapButton.CYCLE_RIGHT) {
			// if (graph.getNode(button.targetID) == null) {
			// button.unhighlight();
			// graph.setSelection(null);
			// return;
			// }
			//					
			// graph.deleteAll(button.targetID);
			// int x = graph.getNode(button.targetID).getLocation().x;
			// int y = graph.getNode(button.targetID).getLocation().y;
			// graph.drawRadial(button.targetID);
			// }
			//				
			// }
			//			
			// tempButton.dispose();
			StapNode node = null;
			if (stapNodeList.get(0) instanceof StapNode) {
				node = (StapNode) stapNodeList.remove(0);
			} else {
				graph.setSelection(null);
				return;
			}
			graph.getTreeViewer().expandToLevel(node.getData(), 0);
			// TODO: Scroll

			int id = node.getData().id;

			graph.scale = 1;
			// Redraw in the current mode with the new id as the center
			// The x,y parameters to draw() are irrelevant for radial mode
			graph.draw(id, 0, 0);

			// Unhighlight the center node and give it a normal colour
			node = graph.getNode(id);
			node.unhighlight();
			if (graph.getData(id).isMarked())
				node.setBackgroundColor(StapGraph.CONSTANT_MARKED);
			else
				node.setBackgroundColor(graph.DEFAULT_NODE_COLOR);
			return;
		} else {

			List<StapNode> stapNodeList = graph.getSelection();
			if (stapNodeList.isEmpty() || stapNodeList.size() != 1) {
				graph.setSelection(null);
				return;
			}

			StapNode node = stapNodeList.remove(0);
			unhighlightall(node);
			graph.setSelection(null);
			graph.getTreeViewer().expandToLevel(node.getData(), 0);

			// Draw in current modes with 'id' at the top
			int id = node.getData().id;
			graph.draw(id, graph.getBounds().x / 2, 20);
		}

		graph.setSelection(null);
	}

	@Override
	public void mouseDown(MouseEvent e) {
		MP.println("You clicked: " + e.x + ", " + e.y); //$NON-NLS-1$ //$NON-NLS-2$
		MP.println("Convert to control: " + graph.toControl(e.x, e.y).x + ", " //$NON-NLS-1$ //$NON-NLS-2$
				+ graph.toControl(e.x, e.y).y);
		MP.println("Convert to display: " + graph.toDisplay(e.x, e.y).x + ", " //$NON-NLS-1$ //$NON-NLS-2$
				+ graph.toDisplay(e.x, e.y).y);
		MP.println("Bounds: " + graph.getBounds().width + ", " //$NON-NLS-1$ //$NON-NLS-2$
				+ graph.getBounds().height);

		listener.setPoint(e.x, e.y);
		listener.setStop(false);
		graph.addMouseMoveListener(listener);
		graph.addFocusListener(focus);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mouseUp(MouseEvent e) {
		listener.setStop(true);
		graph.removeMouseMoveListener(listener);
		graph.removeFocusListener(focus);


		List<StapNode> list = graph.getSelection();

		// ------------Debug information
		if (list.size() == 1) {
			int id = list.get(0).id;
			MP.println("Clicked node " + graph.getData(id).name + " with id " //$NON-NLS-1$ //$NON-NLS-2$
					+ id);
			MP.println("    level: " + graph.getData(id).levelOfRecursion); //$NON-NLS-1$
			MP.println("    called: " + graph.getData(id).called); //$NON-NLS-1$
			MP.println("    caller: " + graph.getData(id).caller); //$NON-NLS-1$
			MP.println("    copllapsedCaller: " //$NON-NLS-1$
					+ graph.getData(id).collapsedCaller);
			MP.println("    callees: " + graph.getData(id).callees.size()); //$NON-NLS-1$
			MP.println("    position: " + graph.getNode(id).getLocation().x //$NON-NLS-1$
					+ ", " + graph.getNode(id).getLocation().y); //$NON-NLS-1$

			// ------------Highlighting
			if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_TREE) {
				for (StapNode n : (List<StapNode>) graph.getNodes()) {
					unhighlightall(n);
				}

				List<Integer> callees = null;

				if (graph.isCollapseMode())
					callees = graph.getData(id).collapsedCallees;
				else
					callees = graph.getData(id).callees;

				for (int subID : callees) {
					if (graph.getNode(subID) != null)
						graph.getNode(subID).highlight();
				}

				if (graph.getParentNode(id) != null) {
					graph.getParentNode(id).highlight();
				}
				graph.setSelection(null);
				graph.getNode(id).highlight();

				return;
			}

		}

		else if (list.size() == 0) {
			for (StapNode n : (List<StapNode>) graph.getNodes()) {
				unhighlightall(n);
			}

		}

		else {
			for (StapNode n : list) {
				unhighlightall(n);
			}
		}

		graph.setSelection(null);
	}

	private void unhighlightall(StapNode n) {
		int id = n.id;
		List<Integer> callees = null;

		if (graph.isCollapseMode())
			callees = graph.getData(id).collapsedCallees;
		else
			callees = graph.getData(id).callees;
		for (int subID : callees) {
			if (graph.getNode(subID) != null)
				graph.getNode(subID).unhighlight();
		}

		if (graph.getParentNode(id) != null) {
			graph.getParentNode(id).unhighlight();
		}
		n.unhighlight();
	}
};
