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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.graph;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.graphingapi.ui.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.BlockAdapter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.ScrollAdapter;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IBlockDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IHistoricalDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.GraphData;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.AreaChartBuilder;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.BarChartBuilder;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.LineChartBuilder;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.PieChartBuilder;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.ScatterChartBuilder;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.graphs.PieChart;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.swt.graphics.Image;



public final class GraphFactory {
	private static final String[] graphNames = new String[] {
			Localization.getString("GraphFactory.ScatterGraph"),
			Localization.getString("GraphFactory.LineGraph"),
			Localization.getString("GraphFactory.BarGraph"),
			Localization.getString("GraphFactory.AreaGraph"),
			Localization.getString("GraphFactory.PieChart"),
	};

	private static final String[] graphDescriptions = new String[] {
		Localization.getString("GraphFactory.ScatterDescription"),
		Localization.getString("GraphFactory.LineDescription"),
		Localization.getString("GraphFactory.BarDescription"),
		Localization.getString("GraphFactory.AreaDescription"),
		Localization.getString("GraphFactory.PieDescription"),
	};

	private static final Image[] graphImages = new Image[] {
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/ScatterGraph.gif").createImage(),
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/LineGraph.gif").createImage(),
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/BarGraph.gif").createImage(),
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/AreaChart.gif").createImage(),
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/PieChart.gif").createImage(),
	};

	private static final String[] graphIDs = new String[] {
		ScatterChartBuilder.ID,
		LineChartBuilder.ID,
		BarChartBuilder.ID,
		AreaChartBuilder.ID,
		PieChartBuilder.ID,
	};

	public static String[] getAvailableGraphs(IDataSet data) {
		LinkedList<String> ids = new LinkedList<String>();
		if(data instanceof IHistoricalDataSet) {
			ids.add(ScatterChartBuilder.ID);
			ids.add(LineChartBuilder.ID);
			ids.add(AreaChartBuilder.ID);
			ids.add(BarChartBuilder.ID);
			ids.add(PieChartBuilder.ID);
		}
		if(data instanceof IBlockDataSet) {
			if(!ids.contains(BarChartBuilder.ID))
				ids.add(BarChartBuilder.ID);
			ids.add(PieChartBuilder.ID);
		}
		
		String[] id = new String[0];
		return (String[])ids.toArray(id);
	}
	
	public static String getGraphName(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return graphNames[index];
		return null;
	}
	
	public static String getGraphDescription(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return graphDescriptions[index];
		return null;
	}

	public static Image getGraphImage(String id) {
		int index = getIndex(id);
		if(index >= 0)
			return graphImages[index];
		return null;
	}
	
	public static boolean isMultiGraph(String id) {
		if(id.equals(PieChart.ID))
			return false;
		return true;
	}
	
	public static boolean isKeyRequired(String graphID, IDataSet data) {
		switch(getIndex(graphID)) {
			case 0:
			case 1:
				if(data instanceof IBlockDataSet)	//Has to be IHistoricalDataSet
					return true;
			default:
				return false;
		}
	}
	
	public static boolean isKeyOptional(String graphID, IDataSet data) {
		switch(getIndex(graphID)) {
			case 2:
				if(data instanceof IBlockDataSet)	//Has to be IHistoricalDataSet
					return true;
			default:
				return false;
		}
	}

	public static final AbstractChartBuilder createGraph(GraphComposite comp, int style, GraphData gd, IDataSet data) {
		AbstractChartBuilder builder = null;
		
		switch(getIndex(gd.graphID)) {
			case 0:
				builder = new ScatterChartBuilder(comp, style, gd.title, new ScrollAdapter((IHistoricalDataSet)data, gd.xSeries, gd.ySeries, gd.key));
				break;
			case 1:
				builder = new LineChartBuilder(comp, style, gd.title, new ScrollAdapter((IHistoricalDataSet)data, gd.xSeries, gd.ySeries, gd.key));
				break;
			case 2:
			if(!(data instanceof IBlockDataSet) || (null != gd.key))
				{
					builder = new BarChartBuilder(comp, style, gd.title, new ScrollAdapter((IHistoricalDataSet)data, gd.xSeries, gd.ySeries, gd.key));
				
				}
				else
				{
				builder = new BarChartBuilder(comp, style, gd.title, new BlockAdapter((IBlockDataSet)data, gd.xSeries, gd.ySeries));
				
				}
				break;
			case 3:
						builder = new AreaChartBuilder(comp, style, gd.title, new ScrollAdapter((IHistoricalDataSet)data, gd.xSeries, gd.ySeries, gd.key));
						break;
			case 4:
				builder = new PieChartBuilder(comp, style, gd.title, new ScrollAdapter((IHistoricalDataSet)data, gd.xSeries, gd.ySeries, gd.key));
				
				break;
		}
		return builder;
	}

	private static int getIndex(String id) {
		
		for(int i=0; i<graphIDs.length; i++)
			if(id.equals(graphIDs[i]))
				return i;
		return -1;
	}
}
