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
package org.eclipse.linuxtools.internal.callgraph;

import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.draw2d.Label;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;

public class StapNode extends GraphNode{

    private static int nodeSize = 20;
    public int id;
    public GraphConnection connection;        //Each node should have only one connection (to its caller)
    static NumberFormat numberFormat = NumberFormat.getInstance(Locale.CANADA);

    public StapNode(StapGraph graphModel, int style, StapData data) {
        super(graphModel, style, Messages.getString("StapNode.0")); //$NON-NLS-1$
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        if (Display.getCurrent().getPrimaryMonitor().getBounds().width < 1000) {
            nodeSize = 10;
        }

        if (data.name == StapGraph.CONSTANT_TOP_NODE_NAME) {
            this.setText(StapGraph.CONSTANT_TOP_NODE_NAME);
        } else {
            String shortName = data.name;
            if (data.name.length() > nodeSize) {
                 shortName = data.name.substring(0, nodeSize - 3) + "...";   //$NON-NLS-1$
            }
            this.setText(shortName + ": " +  //$NON-NLS-1$
                numberFormat.format((float) data.getTime()/graphModel.getTotalTime() * 100)
                + "%"); //$NON-NLS-1$
        }

        if (data.markedMessage != null && data.markedMessage.length() != 0) {
            Label tooltip = new Label(data.name + ": " +  //$NON-NLS-1$
                    numberFormat.format((float) data.getTime()/graphModel.getTotalTime() * 100)
                    + "%" + "\n  " + data.markedMessage); //$NON-NLS-1$ //$NON-NLS-2$
            this.setTooltip(tooltip);
        } else if (data.name.length() > nodeSize) {
            Label tooltip = new Label(data.name + ": " +  //$NON-NLS-1$
                    numberFormat.format((float) data.getTime()/graphModel.getTotalTime() * 100)
                    + "%"); //$NON-NLS-1$
            this.setTooltip(tooltip);
        }


        this.id = data.id;
        this.connection = null;


        if (graphModel.getNode(data.parent) != null) {
            this.connection = new GraphConnection( graphModel, style,
                    this, graphModel.getNode(data.parent));
            if (graphModel.isCollapseMode()) {
                connection.setText("" + data.timesCalled); //$NON-NLS-1$
            }
        } else if (graphModel.getNode(data.collapsedParent) != null) {
            this.connection = new GraphConnection( graphModel, style,
                    this, graphModel.getNode(data.collapsedParent));
            if (graphModel.isCollapseMode()) {
                connection.setText("" + data.timesCalled); //$NON-NLS-1$
            }
        } //else do not create any connections (this should usually never happen)
    }

    /**
     * Returns the StapData object associated with this node.
     */
    @Override
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
        this.connection = new GraphConnection(this.getGraphModel(), style, this, n);
        if (((StapGraph)this.getGraphModel()).isCollapseMode()) {
            connection.setText("" + called); //$NON-NLS-1$
        }
    }
}
