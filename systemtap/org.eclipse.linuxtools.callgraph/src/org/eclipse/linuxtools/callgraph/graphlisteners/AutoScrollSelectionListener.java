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

import org.eclipse.linuxtools.callgraph.StapGraph;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class AutoScrollSelectionListener implements SelectionListener{
	public static final int AutoScroll_up = 0;
	public static final int AutoScroll_down = 1;
	public static final int AutoScroll_bar = 2;
	private final int type;
	private final StapGraph graph;
	
	public AutoScrollSelectionListener(int type, StapGraph g) {
		this.type = type;
		this.graph = g;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (type == AutoScroll_up)
			AutoScrollHelper.scrollUp(graph);
		if (type == AutoScroll_down)
			AutoScrollHelper.scrollDown(graph);
	}

}
