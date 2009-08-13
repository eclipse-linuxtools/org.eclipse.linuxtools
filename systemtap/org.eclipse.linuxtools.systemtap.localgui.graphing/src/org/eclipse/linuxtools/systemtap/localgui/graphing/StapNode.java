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
package org.eclipse.linuxtools.systemtap.localgui.graphing;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.draw2d.Label;
import org.eclipse.linuxtools.systemtap.localgui.core.MP;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;

public class StapNode extends GraphNode{
	public int id;
	public GraphConnection connection;		//Each node should have only one connection (to its caller)
	private boolean hasButtons;				//Has buttosn already attached
	public List<Integer> buttons;
	private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.CANADA);

	public StapNode(StapGraph graphModel, int style, StapData data) {
		super(graphModel, style, Messages.getString("StapNode.0")); //$NON-NLS-1$
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setMinimumFractionDigits(2);
		
		
		
		if (data.name == StapGraph.CONSTANT_TOP_NODE_NAME)
			this.setText(StapGraph.CONSTANT_TOP_NODE_NAME);
		else  {
				this.setText(data.name + ": " +  //$NON-NLS-1$
					numberFormat.format((float) data.time/graphModel.getTotalTime()) 
					+ "%"); //$NON-NLS-1$
		}
		
		if (data.markedMessage.length() != 0) {
			Label tooltip = new Label(this.getText() + "\n  " + data.markedMessage); //$NON-NLS-1$
			this.setTooltip(tooltip);
		}
		
		
		this.id = data.id;
		this.connection = null;
		hasButtons = false;
		buttons = new ArrayList<Integer>();
		
//		if (graphModel.isCollapseMode()) {
//		if (!data.noCaller) {
			if (graphModel.getNode(data.caller) != null) {
				this.connection = new GraphConnection( graphModel, style, 
						this, graphModel.getNode(data.caller));
				if (graphModel.isCollapseMode())
					connection.setText("" + data.called); //$NON-NLS-1$
			}
			
			if (graphModel.getNode(data.collapsedCaller) != null) {
				this.connection = new GraphConnection( graphModel, style, 
						this, graphModel.getNode(data.collapsedCaller));
				if (graphModel.isCollapseMode())
					connection.setText("" + data.called); //$NON-NLS-1$
			}
//		}
		
//		}

	}

	
	public void setHasButtons(boolean value) {
		hasButtons = value;
	}
	
	public boolean getHasButtons() {
		return hasButtons;
	}
	
	/**
	 * Returns the StapData object associated with this node.
	 */
	public StapData getData() {
		return ((StapGraph) this.getGraphModel()).getNodeData(id);
	}
	
	/**
	 * Creates a connection between this node and the
	 * specified node. The connection will have the int called as its text.
	 * 
	 * @param graphModel
	 * @param style
	 * @param n
	 * @param called
	 */
	public void makeConnection(int style, StapNode n, int called) {
		if (n == null) {
			MP.println("Error! Attempting to connect null node to " + this.getText()); //$NON-NLS-1$
		}
		this.connection = new GraphConnection(this.getGraphModel(), style, this, n);
		if (((StapGraph)this.getGraphModel()).isCollapseMode())
			connection.setText("" + called); //$NON-NLS-1$
	}
	
	/**
	 * Returns this node's connection, or null if none exists.
	 * @return
	 */
	public GraphConnection getConnection() {
		return connection;
	}




}
