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

package org.eclipse.linuxtools.systemtapgui.graphingapi.ui.wizards.graph;

import java.util.LinkedList;

import org.eclipse.linuxtools.systemtapgui.graphingapi.nonui.adapters.BlockAdapter;
import org.eclipse.linuxtools.systemtapgui.graphingapi.nonui.adapters.ScrollAdapter;
import org.eclipse.linuxtools.systemtapgui.graphingapi.nonui.datasets.IBlockDataSet;
import org.eclipse.linuxtools.systemtapgui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtapgui.graphingapi.nonui.datasets.IHistoricalDataSet;
import org.eclipse.linuxtools.systemtapgui.graphingapi.nonui.structures.GraphData;
import org.eclipse.linuxtools.systemtapgui.graphingapi.ui.graphs.BarGraph;
import org.eclipse.linuxtools.systemtapgui.graphingapi.ui.graphs.IGraph;
import org.eclipse.linuxtools.systemtapgui.graphingapi.ui.graphs.LineGraph;
import org.eclipse.linuxtools.systemtapgui.graphingapi.ui.graphs.PieChart;
import org.eclipse.linuxtools.systemtapgui.graphingapi.ui.graphs.ScatterGraph;
import org.eclipse.linuxtools.systemtapgui.graphingapi.ui.internal.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.systemtapgui.graphingapi.ui.internal.Localization;
import org.eclipse.linuxtools.systemtapgui.graphingapi.ui.widgets.GraphComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;



public final class GraphFactory {
	private static final String[] graphNames = new String[] {
			Localization.getString("GraphFactory.ScatterGraph"),
			Localization.getString("GraphFactory.LineGraph"),
			Localization.getString("GraphFactory.BarGraph"),
			Localization.getString("GraphFactory.PieChart")
	};

	private static final String[] graphDescriptions = new String[] {
		Localization.getString("GraphFactory.ScatterDescription"),
		Localization.getString("GraphFactory.LineDescription"),
		Localization.getString("GraphFactory.BarDescription"),
		Localization.getString("GraphFactory.PieDescription")
	};

	private static final Image[] graphImages = new Image[] {
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/ScatterGraph.gif").createImage(),
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/LineGraph.gif").createImage(),
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/BarGraph.gif").createImage(),
		GraphingAPIUIPlugin.getImageDescriptor("icons/graphs/PieChart.gif").createImage()
	};

	private static final String[] graphIDs = new String[] {
		ScatterGraph.ID,
		LineGraph.ID,
		BarGraph.ID,
		PieChart.ID
	};

	public static String[] getAvailableGraphs(IDataSet data) {
		LinkedList<String> ids = new LinkedList<String>();
		if(data instanceof IHistoricalDataSet) {
			ids.add(ScatterGraph.ID);
			ids.add(LineGraph.ID);
			ids.add(BarGraph.ID);
		}
		if(data instanceof IBlockDataSet) {
			if(!ids.contains(BarGraph.ID))
				ids.add(BarGraph.ID);
			ids.add(PieChart.ID);
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

	public static final IGraph createGraph(GraphComposite comp, GraphData gd, IDataSet data) {
		IGraph g = null;
		switch(getIndex(gd.graphID)) {
			case 0:
				g = new ScatterGraph(comp, SWT.NONE, gd.title, new ScrollAdapter((IHistoricalDataSet)data, gd.xSeries, gd.ySeries, gd.key));
				break;
			case 1:
				g = new LineGraph(comp, SWT.NONE, gd.title, new ScrollAdapter((IHistoricalDataSet)data, gd.xSeries, gd.ySeries, gd.key));
				break;
			case 2:
				if(!(data instanceof IBlockDataSet) || (null != gd.key))
					g = new BarGraph(comp, SWT.NONE, gd.title, new ScrollAdapter((IHistoricalDataSet)data, gd.xSeries, gd.ySeries, gd.key));
				else
					g = new BarGraph(comp, SWT.NONE, gd.title, new BlockAdapter((IBlockDataSet)data, gd.xSeries, gd.ySeries));
				break;
			case 3:
				g = new PieChart(comp, SWT.NONE, gd.title, new BlockAdapter((IBlockDataSet)data, gd.xSeries, gd.ySeries));
				break;
		}
		return g;
	}

	private static int getIndex(String id) {
		for(int i=0; i<graphIDs.length; i++)
			if(id.equals(graphIDs[i]))
				return i;
		return -1;
	}
}
