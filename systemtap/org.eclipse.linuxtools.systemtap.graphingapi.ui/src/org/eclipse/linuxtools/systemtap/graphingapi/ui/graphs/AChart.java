/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.ui.graphs;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.IGraphColorConstants;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphCanvas;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphLabel;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphLegend;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Button;



public abstract class AChart extends GraphCanvas implements IGraph {
	@SuppressWarnings("unchecked")
	public AChart(GraphComposite parent, int style, String title, IAdapter adapt) {
		super(parent, style);
		adapter = adapt;

		elementList = new LinkedList[adapt.getSeriesCount()];
		for(int i=0; i<elementList.length; i++) {
			elementList[i] = new LinkedList<>();
		}

		createLegend();
		createTitle(title);

		this.addPaintListener(paintListener);

		parent.addCheckOption(Localization.getString("AChart.Title"), titleListener); //$NON-NLS-1$
		parent.addCheckOption(Localization.getString("AChart.Legend"), legendListener); //$NON-NLS-1$
	}

	protected void createTitle(String title) {
		this.title = new GraphLabel(this, title, this, 0.1f, SWT.BOLD);
	}

	protected void createLegend() {
		String[] labels = adapter.getLabels();
		String[] labels2 = new String[labels.length-1];
		Color[] colors = new Color[labels2.length];

		for(int i=0; i<labels2.length; i++) {
			labels2[i] = labels[i+1];
			colors[i] = new Color(this.getDisplay(), IGraphColorConstants.COLORS[i]);
		}

		legend = new GraphLegend(this, labels2, colors);
	}

	protected void paintAll(GC gc) {
		paintElementList(gc);
		if(showLegend && legend != null) {
			legend.paint(gc);
		}
		if(showTitle && title != null) {
			title.paint(gc);
		}
	}

	@Override
	public void dispose() {
		this.removePaintListener(paintListener);
		parent.removeCheckOption(Localization.getString("AChart.Title")); //$NON-NLS-1$
		parent.removeCheckOption(Localization.getString("AChart.Legend")); //$NON-NLS-1$

		legendListener = null;
		titleListener = null;

		super.dispose();
	}

	/*
	 * Listeners are below:
	 *  paintListener - A PaintListener for making sure everything is drawn
	 *  titleListener - A SelectionListener for the title button
	 *  legendListener - A SelectionListener for the legend checkbox
	 */
	private final PaintListener paintListener = new PaintListener() {
		@Override
		public void paintControl(PaintEvent e) {
			paintAll(e.gc);
		}
	};

	private SelectionListener titleListener = new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}
		@Override
		public void widgetSelected(SelectionEvent e) {
			showTitle = ((Button)e.getSource()).getSelection();
			repaint();
		}
	};

	private SelectionListener legendListener = new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}
		@Override
		public void widgetSelected(SelectionEvent e) {
			showLegend = ((Button)e.getSource()).getSelection();
			repaint();
		}
	};

	public abstract boolean isMultiGraph();
	@Override
	public abstract void handleUpdateEvent();
	public abstract void paintElementList(GC gc);

	protected GraphComposite parent;
	protected GraphLegend legend;
	protected GraphLabel title;
	protected LinkedList<Object>[] elementList;

	public boolean showTitle, showLegend;

	private IAdapter adapter;
}
