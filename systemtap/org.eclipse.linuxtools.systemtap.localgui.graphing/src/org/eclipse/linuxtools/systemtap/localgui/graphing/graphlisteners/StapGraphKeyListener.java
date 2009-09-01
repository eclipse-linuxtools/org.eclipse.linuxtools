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
import org.eclipse.linuxtools.systemtap.localgui.graphing.SystemTapView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

/**
 * StapGraph key listener
 */
public class StapGraphKeyListener implements KeyListener {
//	private StapGraph graph;
	
	public StapGraphKeyListener(StapGraph g) {
//		graph = g;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.stateMask != SWT.SHIFT) {
			return;
		}
		
		//TODO: Use accelerator in menu actions instead of this hard-coded stuff
		if (e.character == 'R') {
			SystemTapView.getView_refresh().run();
//		}else if (e.character == '1') {
//			graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_SLOW);
//		}else if (e.character == '2') {
//			graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTER);
//		}else if (e.character == '3') {
//			graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTEST);
//		}else if (e.character == 'k') {
//			Shell sh = graph.getShell();
//			graph.dispose();
//			sh.close();
//		}else if (e.character == 'n') {
//			int id = graph.getNextMarkedNode();
//			graph.draw(id, 0, 0);
//			graph.getTreeViewer().expandToLevel(graph.getData(id), 0);
//		}else if (e.character == 'p') {
//			int id = graph.getPreviousMarkedNode();
//			graph.draw(id, 0, 0);
//			graph.getTreeViewer().expandToLevel(graph.getData(id), 0);
//		}else if (e.character == 'd') {
//			graph.deleteAll(-1);
//		}else if (e.character == 'T') {
//			graph.deleteAll(graph.getRootVisibleNode());
//			graph.draw(StapGraph.CONSTANT_DRAWMODE_TREE, graph.getAnimationMode(), 
//					graph.getRootVisibleNode());
//			graph.currentPositionInLevel.clear();
		}else if (e.character == 'C') {
			SystemTapView.getMode_collapsednodes().run();
		} else if (e.character == 'N') {
			SystemTapView.getGoto_next().run();
		} else if (e.character == 'P') {
			SystemTapView.getGoto_previous().run();
		}
	}
};
