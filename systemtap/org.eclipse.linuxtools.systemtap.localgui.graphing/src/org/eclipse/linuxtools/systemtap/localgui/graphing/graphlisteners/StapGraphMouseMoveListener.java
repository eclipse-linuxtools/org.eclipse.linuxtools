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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;


public class StapGraphMouseMoveListener implements MouseMoveListener {
	private StapGraph graph;
	private int prevX;
	private int prevY;
	private static final int INIT = -20000;
	private boolean stop;
	
	public StapGraphMouseMoveListener(StapGraph graph) {
		this.graph = graph;
		prevX = INIT;
		prevY = INIT;
	}
	
	public void setPoint(int x, int y) {
		prevX = x;
		prevY = y;
	}
	
	public void setStop(boolean val) {
		stop = val;
	}
	
	@Override
	public void mouseMove(MouseEvent e) {
		//-------------Panning
			
		if (!stop) {	
			int yDiff, xDiff;
			xDiff = prevX - e.x;
			yDiff = prevY - e.y;
			
			graph.scrollSmoothBy(xDiff, yDiff);
	
			prevX = e.x;
			prevY = e.y;
		}
	}
}
