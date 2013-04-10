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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetFactory;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.filter.AvailableFilterTypes;
import org.eclipse.linuxtools.systemtap.ui.systemtapgui.SystemTapGUISettings;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * This class handles retrieving information from the metadata contained in a Dashboard module.
 * It will read through the metadata and create a new DashboardModule data set to contain
 * all of the information contained in the XML Memento file.
 * @author Ryan Morse
 */
public class DashboardMetaData {

	public DashboardMetaData(String file) {
		module = null;
		metaFile = new File(file);
		if(metaFile.exists()) {
			readData();
		}
	}

	public DashboardModule getModule() {
		return module;
	}

	/**
	 * This method tries to retrieve the module for the provided kernel version.
	 * @param kernelVersion String for the specific kernel to try to get a module for
	 * @return File that represents the module for the requested kernel version.
	 */
	public File getKernelModule(String kernelVersion) {
		for(int i=0; i<module.kernelVersions.length; i++) {
			if(module.kernelVersions[i].equals(kernelVersion)) {
				return module.kernelModules[i];
			}
		}
		return null;
	}

	/**
	 * This is the main method for this class. It reads the contents of the meta file
	 * and populates the DashboardModule structure from it.
	 * @return boolean representing whether or not the meta data was succesfuly read
	 */
	@SuppressWarnings("unchecked")
	private boolean readData() {
		if(null == metaFile)
			return false;

		try {
			module = new DashboardModule();
			FileReader reader = new FileReader(metaFile);
			if(!reader.ready()) {
				reader.close();
				return false;
			}

			XMLMemento data = XMLMemento.createReadRoot(reader, XMLDashboardItem);

			//Get main module information
			module.display = data.getString(XMLdDisplay);
			module.category = data.getString(XMLdCategory);
			module.description = data.getString(XMLdDescription);
			module.dataSetID = data.getString(XMLdDataset);
			module.location = data.getString(XMLdLocation);
			module.scriptFileName = data.getString(XMLdScriptFileName);
			File temp = null;
			//Get the script
			if ((module.location ==null) || (module.location.equalsIgnoreCase("local"))) //$NON-NLS-1$
			{
			if(!tempScriptFolder.exists()) {
				tempScriptFolder.mkdirs();
			}
			temp = new File(metaFile.getParentFile() + data.getString(XMLdScript));
			module.script = new File(tempScriptFolder.getAbsolutePath() + "/" + module.hashCode() + ".stp"); //$NON-NLS-1$ //$NON-NLS-2$
			temp.renameTo(module.script);
			}
			else
				module.script = new File(module.location + "/" + module.scriptFileName); //$NON-NLS-1$
			//Get the column names
			IMemento[] children = data.getChild(XMLParsingExpressions).getChildren(XMLpColumn);
			module.labels = new String[children.length];
			int i;
			for(i=0; i<children.length; i++) {
				module.labels[i] = children[i].getString(XMLpName);
			}

			//Get the parser
			module.parser = DataSetFactory.createParserXML(module.dataSetID, data.getChild(XMLParsingExpressions).getChild(XMLpParser));

			//Get all graph information
			IMemento[] children2;
			children = data.getChild(XMLGraphDisplays).getChildren(XMLgGraph);
			module.graphs = new GraphData[children.length];
			module.filters = new ArrayList[children.length];
			int j, ys;
			for(i=0; i<children.length; i++) {
				module.graphs[i] = new GraphData();
				module.graphs[i].graphID = children[i].getString(XMLgId);
				module.graphs[i].title = children[i].getString(XMLgTitle);

				//Get all filters for the graph
				children2 = children[i].getChildren(XMLgFilter);
				module.filters[i] = new ArrayList<IDataSetFilter>();
				for(j=0; j<children2.length; j++) {
					module.filters[i].add(AvailableFilterTypes.getDataSetFilter(children2[j]));
				}

				//Get all x & y series for the graph
				children2 = children[i].getChildren(XMLgSeries);
				module.graphs[i].ySeries = new int[children2.length-1];

				for(j=0, ys=0; j<children2.length; j++) {
					if(XMLgAxisX.equals(children2[j].getString(XMLgAxis)))
						module.graphs[i].xSeries = children2[j].getInteger(XMLgColumn).intValue();
					else if(XMLgAxisY.equals(children2[j].getString(XMLgAxis))) {
						module.graphs[i].ySeries[ys] = children2[j].getInteger(XMLgColumn).intValue();
						ys++;
					}
				}
			}

			//Retreive any kernel module data.
			children = data.getChildren(XMLKernelModule);
			module.kernelVersions = new String[children.length];
			module.kernelModules = new File[children.length];
			if(!tempModuleFolder.exists())
				tempModuleFolder.mkdirs();
			for(i=0; i<children.length; i++) {
				module.kernelVersions[i] = children[i].getString(XMLkVersion);

				temp = new File(metaFile.getParentFile() + children[i].getString(XMLkModule));
				module.kernelModules[i] = new File(tempModuleFolder.getAbsolutePath() + module.hashCode() + temp.getName());
				temp.renameTo(module.kernelModules[i]);
			}

			reader.close();
		} catch(FileNotFoundException fnfe) {
			return false;
		} catch(WorkbenchException we) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private static File metaFile;
	private DashboardModule module;
	public static final File tempScriptFolder = new File(SystemTapGUISettings.tempDirectory + "scripts/"); //$NON-NLS-1$
	public static final File tempModuleFolder = new File(SystemTapGUISettings.tempDirectory + "modules/"); //$NON-NLS-1$

	public static final String XMLDashboardItem = "DashboardItem"; //$NON-NLS-1$
	public static final String XMLdDisplay = "display"; //$NON-NLS-1$
	public static final String XMLdCategory = "category"; //$NON-NLS-1$
	public static final String XMLdDescription = "description"; //$NON-NLS-1$
	public static final String XMLdDataset = "dataset"; //$NON-NLS-1$
	public static final String XMLdScript = "script"; //$NON-NLS-1$
	public static final String XMLdScriptFileName = "scriptFileName"; //$NON-NLS-1$
	public static final String XMLdLocation = "location"; //$NON-NLS-1$


	public static final String XMLParsingExpressions = "ParsingExpressions"; //$NON-NLS-1$
	public static final String XMLpColumn = "Column"; //$NON-NLS-1$
	public static final String XMLpName = "name"; //$NON-NLS-1$
	public static final String XMLpParser = "Parser"; //$NON-NLS-1$

	public static final String XMLGraphDisplays = "GraphDisplays"; //$NON-NLS-1$
	public static final String XMLgGraph = "Graph"; //$NON-NLS-1$
	public static final String XMLgId = "id"; //$NON-NLS-1$
	public static final String XMLgTitle = "title"; //$NON-NLS-1$
	public static final String XMLgColumn = "column"; //$NON-NLS-1$
	public static final String XMLgAxis = "axis"; //$NON-NLS-1$
	public static final String XMLgAxisX = "x"; //$NON-NLS-1$
	public static final String XMLgAxisY = "y"; //$NON-NLS-1$
	public static final String XMLgSeries = "Series"; //$NON-NLS-1$
	public static final String XMLgFilter = "Filter"; //$NON-NLS-1$

	public static final String XMLKernelModule = "KernelModule"; //$NON-NLS-1$
	public static final String XMLkVersion = "version"; //$NON-NLS-1$
	public static final String XMLkModule = "module"; //$NON-NLS-1$
}
