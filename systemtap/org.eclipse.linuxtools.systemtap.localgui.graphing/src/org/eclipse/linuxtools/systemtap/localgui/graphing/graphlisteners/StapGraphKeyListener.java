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

import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraph;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapNode;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Shell;

/**
 * StapGraph key listener
 */
public class StapGraphKeyListener implements KeyListener {
	private StapGraph graph;
	
	public StapGraphKeyListener(StapGraph g) {
		graph = g;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void keyReleased(KeyEvent e) {

		//TODO: Use accelerator in menu actions instead of this hard-coded stuff
		if (e.character == 'r') {
			graph.reset();
		}else if (e.character == '1') {
			graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_SLOW);
		}else if (e.character == '2') {
			graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTER);
		}else if (e.character == '3') {
			graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTEST);
		}else if (e.character == 'k') {
			Shell sh = graph.getShell();
			graph.dispose();
			sh.close();
		}else if (e.character == 'n') {
			int id = graph.getNextMarkedNode();
			graph.draw(id, 0, 0);
			graph.getTreeViewer().expandToLevel(graph.getData(id), 0);
		}else if (e.character == 'p') {
			int id = graph.getPreviousMarkedNode();
			graph.draw(id, 0, 0);
			graph.getTreeViewer().expandToLevel(graph.getData(id), 0);
		}else if (e.character == 'd') {
			graph.deleteAll(-1);
		}else if (e.character == 't') {
			graph.deleteAll(graph.getRootVisibleNode());
			graph.draw(StapGraph.CONSTANT_DRAWMODE_TREE, StapGraph.CONSTANT_ANIMATION_SLOW, 
					graph.getRootVisibleNode());
			graph.currentPositionInLevel.clear();
		}else if (e.character == 'c') {
			if (graph.isCollapseMode()) {
				graph.setCollapseMode(false);
			} else {
				graph.setCollapseMode(true);
			}
			
			
			//Redraw
			List<StapNode> stapNodeList = graph.getSelection();
			if (graph.getDrawMode() ==StapGraph.CONSTANT_DRAWMODE_RADIAL) {
				int id;
				// Default to the current center node if more than one node
				// is selected or if no nodes are selected
				if (stapNodeList.size() != 1) {
					graph.setSelection(null);
					id = graph.getRootVisibleNode();
				} else {
					id = stapNodeList.remove(0).id;
				}

				graph.draw(id,0,0);
				graph.getNode(id).unhighlight();
				graph.setSelection(null);
			}
			
			else if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_BOX) {
				//In box mode we can only collapse everything
				graph.draw(StapGraph.CONSTANT_DRAWMODE_BOX, graph.getAnimationMode(), 
						graph.getRootVisibleNode());
			}
			
			else if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_AGGREGATE) {
				//Do nothing
			}
			
			else {
				int id;
				// Default to the current center node if more than one node
				// is selected or if no nodes are selected
				if (stapNodeList.size() != 1) {
					graph.setSelection(null);
					id = graph.getRootVisibleNode();
				} else {
					id = stapNodeList.remove(0).id;
				}
				graph.draw(StapGraph.CONSTANT_DRAWMODE_TREE, 
						graph.getAnimationMode(), id);
			}
			
		}
	}
};
