/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.structures;

import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.DashboardAdapter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;

/**
 * This is a basic structure to contain all the important information for a
 * a Dashboard graph.
 * @author Ryan Morse
 */
public class DashboardGraphData {
	public GraphData graph;
	public String moduleName;
	public IDataSetFilter[] filters;
	public IDataSet data;
	public DashboardAdapter adapter;
}
