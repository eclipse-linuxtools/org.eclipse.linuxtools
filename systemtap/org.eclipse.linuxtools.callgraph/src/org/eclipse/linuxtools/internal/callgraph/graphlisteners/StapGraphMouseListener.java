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
package org.eclipse.linuxtools.internal.callgraph.graphlisteners;

import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.callgraph.StapGraph;
import org.eclipse.linuxtools.internal.callgraph.StapNode;
import org.eclipse.linuxtools.internal.callgraph.core.FileFinderOpener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.zest.core.widgets.GraphNode;

public class StapGraphMouseListener implements MouseListener {
    private StapGraph graph;
    private StapGraphMouseMoveListener listener;
    private StapGraphMouseExitListener exitListener;

    public StapGraphMouseListener(StapGraph g) {
        this.graph = g;
        listener = new StapGraphMouseMoveListener(graph);
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
            if (node == null) {
                return;
            }

            graph.getTreeViewer().collapseToLevel(node.getData(), 0);
            graph.getTreeViewer().expandToLevel(node.getData(), 0);
            graph.getTreeViewer().setSelection(new StructuredSelection(node.getData()));

            int id = node.getData().id;

            graph.scale = 1;
            // Redraw in the current mode with the new id as the center
            // The x,y parameters to draw() are irrelevant for radial mode
            graph.draw(id);

            // Unhighlight the center node and give it a normal colour
            node = graph.getNode(id);
            node.unhighlight();
            if (graph.getNodeData(id).isMarked()) {
                node.setBackgroundColor(StapGraph.CONSTANT_MARKED);
            } else {
                node.setBackgroundColor(graph.DEFAULT_NODE_COLOR);
            }
            return;
        } else {
            StapNode node = getNodeFromSelection();
            if (node == null) {
                return;
            }

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
        mouseDownEvent(e.x, e.y);
    }

    @Override
    public void mouseUp(MouseEvent e) {
        listener.setStop(true);
        graph.removeMouseMoveListener(listener);
        graph.removeListener(SWT.MouseExit, exitListener);

        List<StapNode> list = graph.getSelection();

        if (list.size() == 1) {
            int id;
            if (list.get(0) != null) {
                id = list.get(0).id;
            } else {
                graph.setSelection(null);
                return;
            }
            graph.setSelection(null);

            // ------------Highlighting
            if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_TREE
                    || graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_LEVEL) {
                for (StapNode n : (List<StapNode>) graph.getNodes()) {
                    unhighlightall(n);
                }

                List<Integer> callees = null;

                if (graph.isCollapseMode()) {
                    callees = graph.getNodeData(id).collapsedChildren;
                } else {
                    callees = graph.getNodeData(id).children;
                }

                for (int subID : callees) {
                    if (graph.getNode(subID) != null) {
                        graph.getNode(subID).highlight();
                    }
                }

                if (graph.getParentNode(id) != null) {
                    graph.getParentNode(id).highlight();
                }
                graph.getNode(id).highlight();
                return;
            }

        } else if (list.size() == 0 && ! (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_AGGREGATE)) {
            for (StapNode n : (List<StapNode>) graph.getNodes()) {
                unhighlightall(n);
            }

        }
    }

    private void unhighlightall(StapNode n) {
        int id = n.id;
        List<Integer> callees = null;

        if (graph.isCollapseMode()) {
            callees = graph.getNodeData(id).collapsedChildren;
        } else {
            callees = graph.getNodeData(id).children;
        }
        if (callees == null) {
            return;
        }

        for (int subID : callees) {
            if (graph.getNode(subID) != null) {
                graph.getNode(subID).unhighlight();
            }
        }

        if (graph.getParentNode(id) != null) {
            graph.getParentNode(id).unhighlight();
        }
        n.unhighlight();
    }


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

    private GraphNode getAggregateNodeFromSelection() {
        List<GraphNode> graphNodeList = graph.getSelection();
        if (graphNodeList.isEmpty() || graphNodeList.size() != 1) {
            graph.setSelection(null);
            return null;
        }

        GraphNode node = null;
        if (graphNodeList.get(0) != null) {
            node = graphNodeList.remove(0);
        } else {
            graph.setSelection(null);
            return null;
        }
        return node;
    }

    private void controlDoubleClick() {
        if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_AGGREGATE) {
            GraphNode node = getAggregateNodeFromSelection();

            if (node == null) {
                return;
            }

            String functionName = (String) node.getData("AGGREGATE_NAME"); //$NON-NLS-1$
            FileFinderOpener.findAndOpen(graph.getProject(), functionName);
            node.unhighlight();
        } else {
            StapNode node = getNodeFromSelection();

            if (node == null) {
                return;
            }

            int caller = node.getData().id;

            if (caller < graph.getFirstUsefulNode()) {
                // The only node that satisfies this condition should be main
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

}
