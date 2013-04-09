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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.IGraphColorConstants;
import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.DataPoint;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphAxis;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphAxis2;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;



public abstract class AGraph extends AChart {
	public AGraph(GraphComposite parent, int style, String title, IAdapter adapt) {
		super(parent, style, title, adapt);
		adapter = adapt;
		axes = new LinkedList<GraphAxis>();

		IPreferenceStore store = GraphingAPIUIPlugin.getDefault().getPreferenceStore();
		xSeriesTicks = store.getInt(GraphingAPIPreferenceConstants.P_X_SERIES_TICKS);
		ySeriesTicks = store.getInt(GraphingAPIPreferenceConstants.P_Y_SERIES_TICKS);
		maxItems = store.getInt(GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS);
		viewableItems = store.getInt(GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS);
		
		createAxis(Localization.getString("AGraph.xAxis"), xSeriesTicks, GraphAxis.HORIZONTAL); //$NON-NLS-1$
		createAxis(Localization.getString("AGraph.yAxis"), ySeriesTicks, GraphAxis.VERTICAL); //$NON-NLS-1$
		
		GraphingAPIUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
		
		parent.addCheckOption(Localization.getString("AGraph.GridLines"), gridListener); //$NON-NLS-1$
		if(adapter.getSeriesCount() > 1)
			parent.addCheckOption(Localization.getString("AGraph.Normalize"), normalizeListener); //$NON-NLS-1$
	}
	
	protected void createAxis(String title, int tickCount, int style) {
		axes.add(new GraphAxis(this, title, tickCount, style));
	}
	
	protected void createAxis2(String title, int tickCount, int style) {
		axes.add(new GraphAxis2(this, title, tickCount, style, this.axisColor));
	}

	//******************************************************Please remove
	public Rectangle getArea(int items) {
		int uBound = elementList[0].size();
		int lBound = (uBound > items) ? (uBound-items) : 0;

		int maxX = Integer.MIN_VALUE;
		int minX = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;

		DataPoint p;
		Object o;
		
		for(int j=0; j<elementList.length; j++) {
			for(int i=lBound; i<uBound; i++) {
				o = elementList[j].get(i);
				p = (DataPoint)o;
				if(p.x < minX) minX = (int)p.x;
				if(p.x > maxX) maxX = (int)p.x;
				if(p.y < minY) minY = (int)p.y;
				if(p.y > maxY) maxY = (int)p.y;
			}
		}
		
		return new Rectangle(minX, minY, maxX-minX, maxY-minY);
	}
	
	private synchronized void rebound() {
		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				setGlobalArea(getArea(maxItems));
				setLocalArea(getArea(viewableItems));
			}
		});
	}
	
	@Override
	public synchronized void repaint() {
		rebound();
		super.repaint();
	}
	//*******************************************************End remove
	

	/**
	 * Sets the category axis that is displayed to the axis belonging to the input series index.
	 * @param series Series to display the axis for.
	 */
	public void addSeriesAxis(int series) {
		if(selectedSeries != (series+1)) {
			removeSeriesAxis();
			seriesAxis = new GraphAxis2(this, Localization.getString("AGraph.SeriesAxis"), ySeriesTicks, //$NON-NLS-1$
					GraphAxis2.ALIGN_RIGHT | 
					GraphAxis2.HIDE_GRID_LINES | 
					GraphAxis2.UNNORMALIZED | 
					GraphAxis2.HIDE_TITLE, 
					new Color(this.getDisplay(), IGraphColorConstants.COLORS[series]));
			selectedSeries = series+1;
			axes.add(seriesAxis);
			this.repaint();
		}
	}
	
	/**
	 * Removes the series axis from the graph.
	 */
	public void removeSeriesAxis() {
		if(null != seriesAxis) {
			axes.remove(seriesAxis);
			seriesAxis = null;
			selectedSeries = -1;
			this.repaint();
		}
	}
	
	@Override
	protected void paintAll(GC gc) {
		for(GraphAxis axis: axes) {
			axis.paint(gc);
		}
		super.paintAll(gc);
	}

	@Override
	public void dispose() {
		GraphingAPIUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);

		parent.removeCheckOption(Localization.getString("AGraph.Normalize")); //$NON-NLS-1$
		parent.removeCheckOption(Localization.getString("AGraph.GridLines")); //$NON-NLS-1$

		normalizeListener = null;
		gridListener = null;
		
		parent = null;
		
		super.dispose();
	}
	
	/*
	 * Listeners are below:
	 *  gridListener - A SelectionListener for the Grid checkbox
	 *  normalizeListener - A SelectionListener for the normalization checkbox
	 *  propertyChangeListener - Detects changes in user preferences and applies them
	 */
	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			IPreferenceStore store = GraphingAPIUIPlugin.getDefault().getPreferenceStore();
			if(event.getProperty().equals(GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS))
				maxItems = store.getInt(GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS);
			else if(event.getProperty().equals(GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS))
				viewableItems = store.getInt(GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS);
			else if(event.getProperty().equals(GraphingAPIPreferenceConstants.P_X_SERIES_TICKS)) {
				xSeriesTicks = store.getInt(GraphingAPIPreferenceConstants.P_X_SERIES_TICKS);
				for(GraphAxis a: axes) {
					if(GraphAxis.HORIZONTAL == a.getType()) {
						a.setTickCount(xSeriesTicks);
					}
				}
			} else if(event.getProperty().equals(GraphingAPIPreferenceConstants.P_Y_SERIES_TICKS)) {
				ySeriesTicks = store.getInt(GraphingAPIPreferenceConstants.P_Y_SERIES_TICKS);
				for(GraphAxis a:axes) {
					if(GraphAxis.VERTICAL == a.getType()) {
						a.setTickCount(ySeriesTicks);
					}
				}
			} else if(event.getProperty().equals(GraphingAPIPreferenceConstants.P_SHOW_X_GRID_LINES)) {
				showXGrid = store.getBoolean(GraphingAPIPreferenceConstants.P_SHOW_X_GRID_LINES);
				showGrid = showXGrid || showYGrid;
			} else if(event.getProperty().equals(GraphingAPIPreferenceConstants.P_SHOW_X_GRID_LINES)) {
				showYGrid = store.getBoolean(GraphingAPIPreferenceConstants.P_SHOW_X_GRID_LINES);
				showGrid = showXGrid || showYGrid;
			}

			repaint();
		}
	};
	
	private SelectionListener gridListener = new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}
		@Override
		public void widgetSelected(SelectionEvent e) {
			showGrid = ((Button)e.getSource()).getSelection();
			repaint();
		}
	};
	
	private SelectionListener normalizeListener = new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}
		@Override
		public void widgetSelected(SelectionEvent e) {
			normalize = ((Button)e.getSource()).getSelection();
			if(!normalize) removeSeriesAxis();
			handleUpdateEvent();
		}
	};
	
	protected LinkedList<GraphAxis> axes;
	
	private IAdapter adapter;
	private boolean showYGrid, showXGrid;
	
	protected static int xSeriesTicks;
	protected static int ySeriesTicks;
	protected static int maxItems;
	protected static int viewableItems;
	protected int removedItems;
	
	public boolean showGrid, normalize;

	protected int selectedSeries;
	protected GraphAxis seriesAxis;
}
