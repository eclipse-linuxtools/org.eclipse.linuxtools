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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;


public class StapGraphMouseMoveListener implements MouseMoveListener {
    private StapGraph graph;
    private int prevX;
    private int prevY;
    private static final int INIT = -20000;
    private boolean stop;
    private boolean showMessage;

    public StapGraphMouseMoveListener(StapGraph graph) {
        this.graph = graph;
        prevX = INIT;
        prevY = INIT;
        showMessage = true;
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
        //TODO: Implement panning at this zoom and mode
        //For some reason getting rid of some of the /scale's in drawTree
        //Will fix panning, but at the cost of making the drawTree zoom look weird

        if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_TREE) {
//            if (graph.scale < 0.63) {
                if (showMessage) {
//                    SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
//                            Messages.StapGraphMouseMoveListener_0, Messages.StapGraphMouseMoveListener_1,
//                            Messages.StapGraphMouseMoveListener_2);
//                    mess.schedule();
                    showMessage = false;
                }
                return;
            }

        //Initialize
        if (prevX == INIT && prevY == INIT) {
            prevX = e.x;
            prevY = e.y;
            return;
        }

        if (!stop) {
            int yDiff, xDiff;
            xDiff = prevX - e.x;
            yDiff = prevY - e.y;
            if (graph.scale > 1) {
                graph.scrollSmoothBy((int) (xDiff/graph.scale), (int) (yDiff/graph.scale));
            } else {
                graph.scrollSmoothBy(xDiff, yDiff);
            }

            prevX = e.x;
            prevY = e.y;
        }
    }
}
