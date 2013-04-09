/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.structures;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;

/**
 * This is a basic structure to contain all the important information for a 
 * module in the dashboard.
 * @author Ryan Morse
 */
public class DashboardModule {
	public String display;
	public String category;
	public String description;
	public File script;
	public File archiveFile;

	public String dataSetID;
	public String[] labels;
	public IDataSetParser parser;
	public GraphData[] graphs;
	public ArrayList<IDataSetFilter>[] filters;
	public String location;
	
	public String[] kernelVersions;
	public File[] kernelModules;

	public String scriptFileName;
	public static final String metaFileName = "/metaData"; //$NON-NLS-1$
	
	public String getcategory()
	{
		return category;
	
	}
	public String getdisplay()
	{
		return display;
	}
	
	public String getlocation()
	{
		return location;
	}
}
