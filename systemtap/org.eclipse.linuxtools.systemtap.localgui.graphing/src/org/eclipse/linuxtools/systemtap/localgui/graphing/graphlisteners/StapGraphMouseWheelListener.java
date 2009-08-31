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

import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

/**
 * Allows the user to zoom when CTRL + mouse wheel is used
 *
 */
public class StapGraphMouseWheelListener implements MouseWheelListener {
//	private long snapshot;
	private StapGraph graph;
	
	public StapGraphMouseWheelListener(StapGraph g) {
		this.graph = g;
	}
	
	

	@Override
	public void mouseScrolled(MouseEvent e) {
		
		if (e.stateMask != SWT.CTRL)
			return;
		
		if (graph.getDrawMode() != StapGraph.CONSTANT_DRAWMODE_BOX && 
				graph.getDrawMode() != StapGraph.CONSTANT_DRAWMODE_TREE)
			return;
				

		if (e.count <= 0) {
			if (graph.scale < 2){
				graph.scale /= (10.0 / 11.0);
			}else{				
				graph.scale = (int)graph.scale + 1;
			}

		}else {
			if (graph.scale <= 2){
				graph.scale *= (10.0 / 11.0);
			}else{
				graph.scale = (int) graph.scale - 1;
			}
		}
		
		int currentAnimationMode = graph.getAnimationMode();
		graph.draw(graph.getDrawMode(), StapGraph.CONSTANT_ANIMATION_FASTEST,
				graph.getRootVisibleNode());
		graph.setAnimationMode(currentAnimationMode);
//		graph.moveAllNodesBy(graph.getBounds().width/2 - e.x, graph.getBounds().height/2 - e.y);
		
//		// Don't scroll for circle mode
//		if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_CIRCLE)
//			return;
//
//		// Scrolling
//		if (e.count > 0) {
//
//			long tempSnapshot = System.currentTimeMillis();
//			if (tempSnapshot - snapshot < 100)
//				return;
//			snapshot = tempSnapshot;
//
//			// Scrolling up
//			if (graph.getBottomLevelToDraw() < graph.getLowestLevelOfNodesAdded()
//					- StapGraph.CONSTANT_LEVEL_BUFFER / 2)
//				graph.addLevelToTop();
//
//			graph.decreaseTopLevelOnScreen();
//			if (graph.getTopLevelOnScreen() < 0) {
//				graph.setTopLevelOnScreen(0);
//			}
//
//			graph
//					.scrollToY(graph.getNode(
//							graph.getLevel(graph.getTopLevelOnScreen()).get(0))
//							.getLocation().y);
//			graph.update();
//
//		}
//
//		else {
//
//			long tempSnapshot = System.currentTimeMillis();
//
//			if (tempSnapshot - snapshot < 100)
//				return;
//			snapshot = tempSnapshot;
//
//			// Don't delete levels yet
//			if (graph.getTopLevelOnScreen() > StapGraph.CONSTANT_LEVEL_BUFFER / 4)
//				graph.addLevelToBottom();
//
//			graph.incrementTopLevelOnScreen();
//			if (graph.getTopLevelOnScreen() < 0) {
//				graph.setTopLevelOnScreen(0);
//			}
//
//			graph
//			.scrollToY(graph.getNode(
//							graph.getLevel(graph.getTopLevelOnScreen()).get(0))
//							.getLocation().y);
//			graph.update();
//
//		}

	}

};

