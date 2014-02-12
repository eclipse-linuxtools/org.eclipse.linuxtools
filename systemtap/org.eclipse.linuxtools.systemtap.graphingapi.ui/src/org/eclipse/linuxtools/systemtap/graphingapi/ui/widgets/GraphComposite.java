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

package org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * A Composite type to contain a Graph object.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class GraphComposite extends Composite {
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
	public void configure(boolean withSidebar) {
		sidebarVisible = withSidebar;

		for(Button b:checkOptions) {
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

	public void addCheckOption(String title, SelectionListener listener) {
		Button b = new Button(checkOptionComp, SWT.CHECK);
		b.setText(title);
		b.addSelectionListener(listener);
		checkOptions.add(b);
		b.setSelection(true);
		configure(sidebarVisible);
	}

	public void removeCheckOption(String title) {
		for(Button b :checkOptions) {
			if(b.getText().equals(title)) {
				checkOptions.remove(b);
				b.dispose();
				configure(sidebarVisible);
				return;
			}
		}
	}

	/**
	 * Returns the graph that is rendering to this composite.
	 */
	public AbstractChartBuilder getCanvas() {
		return builder;
	}

	/**
	 * Returns the current sidebar visibility state.
	 */
	public boolean isSidebarVisible() {
		return sidebarVisible;
	}

	/**
	 * @since 3.0
	 */
	public void setLegendVisible(boolean visible) {
		builder.getChart().getLegend().setVisible(visible);
		builder.handleUpdateEvent();
	}

	private final Composite xControl;
	private final Composite yControl;

	private boolean sidebarVisible = false;
	private AbstractChartBuilder builder;
	private ArrayList<Button> checkOptions;
	private Composite checkOptionComp;
}
