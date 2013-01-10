/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.sleepingthreads;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapParser;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

public class XMLGraphingView extends SystemTapView{
	private Graph graph;
	private ArrayList<GraphNode> nodes;
	GraphNode node1;
	public static final Color MARKED = new Color(Display.getCurrent(), 210, 112, 214);
	protected SleepingThreadsParser parser;

	
	@Override
	protected boolean createOpenAction() {
		return false;
	}

	@Override
	protected boolean createOpenDefaultAction() {
		return false;
	}

	@Override
	public IStatus initializeView(Display targetDisplay, IProgressMonitor monitor) {
		if (nodes != null) {
			for (GraphNode n : nodes)
				if (n != null) n.dispose();
		}
		nodes = new ArrayList<GraphNode>();
		return Status.OK_STATUS;
	}


	@Override
	public void setViewID() {
		viewID = "org.eclipse.linuxtools.sleepingthreads.xmlview";
	}

	@Override
	public boolean setParser(SystemTapParser p) {
		if (p instanceof SleepingThreadsParser) {
			parser = (SleepingThreadsParser) p;
			return true;
		}
		return false;
	}
	
	@Override
	public void updateMethod() {
		if (nodes == null)
			nodes = new ArrayList<GraphNode>();
		if (parser.getData() instanceof List<?>) {
			List<?> list = (List<?>) parser.getData();
			if (list.size() == 0)
				return;
			if (list.get(0) instanceof XMLData) {
				for (Object o : list) {
					if (o instanceof XMLData) {
						XMLData data = (XMLData) o;
						
						if (data.isNode()) {
							
							handleNode(data);

						} else {
							int parent = data.getParent();
							if (parent >= nodes.size())
								continue;
							GraphNode node = nodes.get(parent);
							
							handleData(node, data);
						}
						
					}
				}
			}
		}
		parser.setData(null);
		graph.applyLayout();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		// Graph will hold all other objects
		graph = new Graph(parent, SWT.NONE);
		nodes = new ArrayList<GraphNode>();
		graph.setLayoutAlgorithm(getLayoutAlgorithm(), true);
		addKillButton();
		
	}
	
	/**
	 * Overwrite to change the layout algorithm used. By default uses
	 * SpringLayoutAlgorithm with no resizing.
	 * @return
	 */
	private LayoutAlgorithm getLayoutAlgorithm() {
		return new SpringLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SystemTapParser getParser() {
		return parser;
	}
	
	/**
	 * Overwrite to process data differently. By default, this method will add data.getText()
	 * to the current tooltip for the node. Will be called with the XMLData object being processed
	 * as well as the node to which it belongs.
	 * @param node
	 * @param data
	 */
	private void handleData(GraphNode node, XMLData data) {
		IFigure fig = node.getTooltip();
		String currText =  "";
		if (fig instanceof Label)
			currText = ((Label) fig).getText();
		node.setTooltip(new Label( currText + "\n" + data.getText()));
		node.setBackgroundColor(MARKED);
	}

	
	/**
	 * Overwrite to handle nodes differently. By default will create a node with name
	 * data.getName() and connect it to its parent if its parent exists. Will also
	 * add the node to the list of nodes.
	 * @param data
	 */
	private void handleNode(XMLData data) {
		int parent = data.getParent();
		GraphNode node = new GraphNode(graph, SWT.NONE, data.getName());
		
		if (parent < nodes.size() && parent >= 0)
			new GraphConnection (graph, ZestStyles.CONNECTIONS_DASH, node, nodes.get(parent));
		nodes.add(node);
	}
}
