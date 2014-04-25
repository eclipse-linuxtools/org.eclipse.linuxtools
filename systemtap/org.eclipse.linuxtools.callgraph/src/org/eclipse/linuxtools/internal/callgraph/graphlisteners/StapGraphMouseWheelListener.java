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

import org.eclipse.linuxtools.internal.callgraph.StapGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

/**
 * Allows the user to zoom when CTRL + mouse wheel is used
 *
 */
public class StapGraphMouseWheelListener implements MouseWheelListener {
    private StapGraph graph;

    public StapGraphMouseWheelListener(StapGraph g) {
        this.graph = g;
    }


    @Override
    public void mouseScrolled(MouseEvent e) {
        if (e.stateMask != SWT.CTRL) {
            // Scrolling
            if (e.count > 0) {
                AutoScrollHelper.scrollUp(graph);
            } else {
                AutoScrollHelper.scrollDown(graph);
            }
            return;
        }

        if (graph.getDrawMode() != StapGraph.CONSTANT_DRAWMODE_LEVEL &&
                graph.getDrawMode() != StapGraph.CONSTANT_DRAWMODE_TREE) {
            return;
        }

        if (e.count <= 0) {
            if (graph.scale < 2){
                graph.scale /= (10.0 / 11.0);
            }else{
                graph.scale = (int)graph.scale + 1;
            }

        }else {
            if (graph.scale <= 2){
                graph.scale *= (10.0 / 11.0);
            } else {
                graph.scale = (int) graph.scale - 1;
            }
        }

        int currentAnimationMode = graph.getAnimationMode();
        graph.draw(graph.getDrawMode(), StapGraph.CONSTANT_ANIMATION_FASTEST,
                graph.getRootVisibleNodeNumber());
        graph.setAnimationMode(currentAnimationMode);

        int realeX = 3 * (int)(e.x / graph.scale);
        int realeY = 3 * (int)(e.y / graph.scale);
        int xDiff = (realeX - graph.getSize().x);
        int yDiff = (realeY - graph.getSize().y);

        graph.scrollTo(realeX + xDiff, realeY - yDiff);
    }

}

