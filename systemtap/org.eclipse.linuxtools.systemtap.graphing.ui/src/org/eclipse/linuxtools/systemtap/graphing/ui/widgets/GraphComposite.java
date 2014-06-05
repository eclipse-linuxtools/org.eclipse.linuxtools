/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *     Red Hat - Ongoing maintenance
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.graphing.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A Composite type to contain a Graph object.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class GraphComposite extends Composite {

    private final Composite xControl;
    private final Composite yControl;

    private boolean sidebarVisible = false;
    private AbstractChartBuilder builder;
    private List<Button> checkOptions;
    private Composite checkOptionComp;

    /**
     * The default constructor: creates an internal composite for the Graph to render on, asks GraphFactory
     * to create the graph from the given GraphData and DataSet, then initializes all buttons and listeners.
     */
    public GraphComposite(Composite parent, int style, GraphData gd, IDataSet ds) {
        super(parent, style);
        FormLayout layout = new FormLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        this.setLayout(layout);
        checkOptions = new ArrayList<>();

        checkOptionComp = new Composite(this, style);
        checkOptionComp.setLayout(new RowLayout(SWT.VERTICAL));
        FormData data = new FormData();
        data.bottom = new FormAttachment(100, 0);
        data.right = new FormAttachment(100, 0);
        checkOptionComp.setLayoutData(data);

        builder = GraphFactory.createGraph(this, style, gd, ds);
        xControl = GraphFactory.createGraphXControl(this, style);
        yControl = GraphFactory.createGraphYControl(this, style);

        if (xControl instanceof IUpdateListener) {
            builder.addUpdateListener((IUpdateListener) xControl);
        }
        if (yControl instanceof IUpdateListener) {
            builder.addUpdateListener((IUpdateListener) yControl);
        }

        configure(true);
        builder.build();
    }

    /**
     * Toggles sidebar visible or not visible.
     * @param withSidebar Enables or disables the sidebar.
     */
    private void configure(boolean withSidebar) {
        sidebarVisible = withSidebar;

        for (Button b : checkOptions) {
            b.setVisible(withSidebar);
        }

        if (xControl != null) {
            xControl.setVisible(withSidebar);
        }
        if (yControl != null) {
            yControl.setVisible(withSidebar);
        }

        FormData data = new FormData();
        data.top = new FormAttachment(0,0);
        data.right = withSidebar ? new FormAttachment(checkOptionComp, 0) : new FormAttachment(100, 0);
        data.bottom = withSidebar && xControl != null ? new FormAttachment(xControl, 0) : new FormAttachment(100, 0);
        data.left = withSidebar && yControl != null ? new FormAttachment(yControl, 0) : new FormAttachment(0, 0);
        builder.setLayoutData(data);
        layout(true, true);
    }

    public void addCheckOption(final String title, final SelectionListener listener) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Button b = new Button(checkOptionComp, SWT.CHECK);
                b.setText(title);
                b.addSelectionListener(listener);
                checkOptions.add(b);
                b.setSelection(true);
                if (sidebarVisible) {
                    configure(true);
                }
            }
        });
    }

    /**
     * Returns the graph that is rendering to this composite.
     */
    public AbstractChartBuilder getCanvas() {
        return builder;
    }

    /**
     * Set the visibility of the graph's legend.
     * @param visible Set this to <code>true</code> to show the legend,
     * or <code>false</code> to hide it.
     * @since 3.0
     */
    public void setLegendVisible(boolean visible) {
        builder.getChart().getLegend().setVisible(visible);
        builder.handleUpdateEvent();
    }

    /**
     * Set the visibility of the graph's title.
     * @param visible Set this to <code>true</code> to show the title,
     * or <code>false</code> to hide it.
     * @since 3.1
     */
    public void setTitleVisible(boolean visible) {
        builder.getChart().getTitle().setVisible(visible);
        builder.handleUpdateEvent();
    }
}
