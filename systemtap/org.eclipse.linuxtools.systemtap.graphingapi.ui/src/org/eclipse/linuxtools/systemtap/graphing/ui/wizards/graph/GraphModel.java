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

package org.eclipse.linuxtools.systemtap.graphing.ui.wizards.graph;

import java.util.Arrays;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.GraphData;

public class GraphModel {
	public GraphModel(IDataSet d) {
		graph = new GraphData();
		graph.graphID = ""; //$NON-NLS-1$
		graph.title = ""; //$NON-NLS-1$
		graph.xSeries = -1;
		graph.ySeries = null;
		data = d;
	}

	public void destroy() {
		graph = null;
	}

	public GraphData getGraphData() {
		return graph;
	}

	public IDataSet getDataSet() {
		return data;
	}

	public String[] getSeries() {
		return data.getTitles();
	}

	public String getGraphID() {
		return graph.graphID;
	}

	public int getXSeries() {
		return graph.xSeries;
	}

	public int[] getYSeries() {
		return graph.ySeries;
	}

	public void setTitle(String title) {
		graph.title = title;
	}

	public void setKey(String key) {
		graph.key = key;
	}

	public void setGraph(String g) {
		graph.graphID = g;
	}

	public void setXSeries(int x) {
		graph.xSeries = x;
	}

	public void setYSeries(int[] y) {
		graph.ySeries = Arrays.copyOf(y, y.length);
	}

	public boolean isGraphSet() {
		return !graph.graphID.isEmpty();
	}

	public boolean isSeriesSet() {
		return ((-1 <= graph.xSeries) && (null != graph.ySeries) && graph.ySeries.length > 0);
	}

	public boolean isTitleSet() {
		return !graph.title.isEmpty();
	}

	private GraphData graph;
	private IDataSet data;
}
