/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.graphing.ui.charts.listeners;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;

/**
 * An abstract listener class for displaying data values of a chart as it
 * is hovered over by the mouse.
 *
 * Clients must override this class to specify how the listener should react
 * to different kinds of mouse behaviour.
 * @since 3.0
 */
public abstract class AbstractChartMouseMoveListener implements MouseMoveListener {
    protected final Chart chart;
    protected MouseEvent lastMouseEvent = null;

    /**
     * Create a listener to react to mouse movements made in the provided chart region.
     * @param chart The chart that this listener is watching.
     * @param hoverArea The plot area of the chart in which this listener will react
     * to mouse movement.
     */
    public AbstractChartMouseMoveListener(Chart chart, final Composite hoverArea) {
        this.chart = chart;
        final MouseMoveListener thisListener = this;
        hoverArea.addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseExit(MouseEvent e) {
                hoverArea.removeMouseMoveListener(thisListener);
                exit();
            }

            @Override
            public void mouseEnter(MouseEvent e) {
                hoverArea.addMouseMoveListener(thisListener);
            }
        });
    }

    /**
     * This method is called whenever the mouse exits the plot area of the chart.
     * It may be overridden to add extra functionality (but always include a super call).
     */
    public void exit() {
        lastMouseEvent = null;
    }

    /**
     * Call this method whenever the chart gets updated, so that another mouse event can be
     * fired with the new chart contents without having to explicitly move the mouse.
     */
    public final void update() {
        if (lastMouseEvent != null) {
            mouseMove(lastMouseEvent);
        }
    }

    /**
     * Clients must override this method to perform appropriate actions whenever the
     * mouse is moved while inside the chart's plot area (but always include a super call).
     */
    @Override
    public void mouseMove(MouseEvent e) {
        lastMouseEvent = e;
    }
}