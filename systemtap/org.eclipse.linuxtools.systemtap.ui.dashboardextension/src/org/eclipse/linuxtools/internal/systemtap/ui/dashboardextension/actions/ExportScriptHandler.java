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

package org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.dialogs.ExportScriptDialog;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetWizard;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.structures.ZipArchive;
import org.eclipse.linuxtools.systemtap.ui.dashboard.DashboardPerspective;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardMetaData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModule;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModuleFileFilter;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingConstants;
import org.eclipse.linuxtools.systemtap.ui.ide.IDEPerspective;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.RunScriptHandler;
import org.eclipse.linuxtools.systemtap.ui.systemtapgui.SystemTapGUISettings;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * This class brings up a dialog box for the user to select what they want the
 * new module to contain.  If a new module is build, it will be exported to the
 * dashboard for use at any time.
 * @author Ryan Morse
 */
public class ExportScriptHandler extends RunScriptHandler {
	/**
	 * This method will bring up the export script dialog window for the user
	 * to select what they want to new module to contain.  If the user enters
	 * module information and clicks ok the module will be built and added to
	 * the dashboard.
	 */

	private static String scriptFileName = "/script.stp"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) {
		String script = getFilePath();
		if(null == script || script.length() <= 0) {
			String msg = MessageFormat.format(Localization.getString("ExportScriptAction.NoFileToExport"), (Object[])null); //$NON-NLS-1$
			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Localization.getString("ExportScriptAction.Error"), msg); //$NON-NLS-1$
		} else {
			DataSetWizard wizard = new DataSetWizard(GraphingConstants.DataSetMetaData, script);
			IWorkbench workbench = PlatformUI.getWorkbench();
			wizard.init(workbench, null);
			WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
			dialog.create();
			dialog.open();

			IDataSetParser parser = wizard.getParser();
			IDataSet dataSet = wizard.getDataSet();

			wizard.dispose();

			if(null == parser || null == dataSet)
				return null;

			ExportScriptDialog exportDialog = new ExportScriptDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), dataSet);
			exportDialog.create();

			if(exportDialog.open() == Window.OK) {
				String category = exportDialog.getCategory();
				String display = exportDialog.getDisplay();
				String description = exportDialog.getDescription();
				GraphData[] gd = exportDialog.getGraphs();
				TreeNode filters = exportDialog.getGraphFilters();

				validateDirectory();
				File meta = saveMetaData(display, category, description, dataSet, parser, gd, filters,"local"); //$NON-NLS-1$
				String archiveName = getSaveDirectory() + "/" + category.replace(' ', '_') + "." + display.replace(' ', '_'); //$NON-NLS-1$ //$NON-NLS-2$
				buildArchive(archiveName, new File(script), meta);
				cleanupFiles(new String[] {archiveName, meta.getAbsolutePath()});
				updateDashboard();
			}
		}
		return null;
	}

	/**
	 * This method will check to make sure the exported module directory is valid.
	 * If it isn't then the folders will be created in order to make the directory
	 * valid.
	 */
	private void validateDirectory() {
		File folder = new File(getSaveDirectory());

		if(!folder.exists())
			folder.mkdir();
	}

	/**
	 * This method will create a new XML Memento used to store all of the meta data
	 * for the module.  This data is all based on what the user selected from the
	 * dialog box.
	 * @param disp The string to display that represents this module.
	 * @param cat The category string representing where this module will be placed
	 * @param desc The description string for this module
	 * @param dataSet The IDataSet that holds the data for this module
	 * @param parser The parer that can obtain the information from the raw output
	 * @param gd GraphData Array specifying all of the information needed to create the selected graphs
	 * @param filters TreeNode containing all of the selected filters for each graph.
	 */
	private File saveMetaData(String disp, String cat, String desc, IDataSet dataSet, IDataSetParser parser, GraphData[] gd, TreeNode filters, String location) {
		File meta = null;
		XMLMemento data = XMLMemento.createWriteRoot(DashboardMetaData.XMLDashboardItem);

		try {
			IMemento child, child2, child3;

			data.putString(DashboardMetaData.XMLdDisplay, disp);
			data.putString(DashboardMetaData.XMLdCategory, cat);
			data.putString(DashboardMetaData.XMLdDescription, desc);
			data.putString(DashboardMetaData.XMLdDataset, dataSet.getID());
			data.putString(DashboardMetaData.XMLdScript, scriptFileName);
			data.putString(DashboardMetaData.XMLdLocation, location);
			data.putString(DashboardMetaData.XMLdScriptFileName, scriptFileName);


			child = data.createChild(DashboardMetaData.XMLParsingExpressions);
			String[] cols = dataSet.getTitles();
			for(int i=0; i<cols.length; i++) {
				child2 = child.createChild(DashboardMetaData.XMLpColumn);
				child2.putString(DashboardMetaData.XMLpName, cols[i]);
			}
			parser.saveXML(child.createChild(DashboardMetaData.XMLpParser));

			child = data.createChild(DashboardMetaData.XMLGraphDisplays);
			for(int j,i=0; i<gd.length; i++) {
				child2 = child.createChild(DashboardMetaData.XMLgGraph);
				child2.putString(DashboardMetaData.XMLgId, gd[i].graphID);
				child2.putString(DashboardMetaData.XMLgTitle, gd[i].title);

				TreeNode treeChild = filters.getChildAt(i);
				for(j=0; j<treeChild.getChildCount(); j++) {
					((IDataSetFilter)(treeChild.getChildAt(j).getData())).writeXML(child2);
				}

				child3 = child2.createChild(DashboardMetaData.XMLgSeries);
				child3.putString(DashboardMetaData.XMLgAxis, DashboardMetaData.XMLgAxisX);
				child3.putInteger(DashboardMetaData.XMLgColumn, gd[i].xSeries);
				for(j=0; j<gd[i].ySeries.length; j++) {
					child3 = child2.createChild(DashboardMetaData.XMLgSeries);
					child3.putString(DashboardMetaData.XMLgAxis, DashboardMetaData.XMLgAxisY);
					child3.putInteger(DashboardMetaData.XMLgColumn, gd[i].ySeries[j]);
				}
			}

			meta = new File(getSaveDirectory() + DashboardModule.metaFileName);
			FileWriter writer = new FileWriter(meta);
			data.save(writer);
			writer.close();
		} catch(FileNotFoundException fnfe) {
			return meta;
		} catch (IOException e) {
			return meta;
		}
		return meta;
	}

	/**
	 * This method will create the module archive by first zipping the .stp file and the meta data
	 * together.  Then it will compress the .zip file into a .gz file.  The .gz file's extension is
	 * set to .dash to discurage users from trying to modify it and to make it sepecific to the
	 * SystemTapGUI dashboard.
	 * @param archiveName The name to use for the file containing the new module data.
	 * @param script The file representing the .stp script file to use for the module
	 * @param meta The XML Memento file representing the module details.
	 */
	private void buildArchive(String archiveName, File script, File meta) {
		String[] files = new String[] {script.getAbsolutePath(), meta.getAbsolutePath()};
		String[] names = new String[] {scriptFileName, DashboardModule.metaFileName};

		ZipArchive.zipFiles(archiveName, files, names);
		ZipArchive.compressFile(archiveName + DashboardModuleFileFilter.DashboardModuleExtension, archiveName);
	}

	/**
	 * This method will delete any extra files that were generated as a result of building
	 * the Dashboard module.
	 * @param files A list of all of the file paths that should be removed
	 */
	private void cleanupFiles(String[] files) {
		if(null == files) {
			return;
		}

		File f;
		for(String fileName: files) {
			f = new File(fileName);
			if(f.exists()) {
				f.delete();
			}
		}
	}

	/**
	 * This method will get the directory name that should be used for saving the dashboard modules.
	 */
	private String getSaveDirectory() {
		return SystemTapGUISettings.settingsFolder + "/dashboard"; //$NON-NLS-1$
	}

	/**
	 * This method forces the Dashboard's DashboardModuleBrowserView to refresh itself to ensure
	 * that is contains the most up-to-date module list.
	 */
	private void updateDashboard() {
		try {
			IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(DashboardPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			IViewPart ivp = p.findView(DashboardModuleBrowserView.ID);
			((DashboardModuleBrowserView)ivp).refresh();

			p = PlatformUI.getWorkbench().showPerspective(IDEPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		} catch(WorkbenchException we) {}
	}

}
