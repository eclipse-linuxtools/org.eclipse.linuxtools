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

package org.eclipse.linuxtools.internal.systemtap.ui.dashboard.structures;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.systemtap.structures.JarArchive;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardPlugin;
import org.eclipse.linuxtools.systemtap.ui.dashboard.preferences.DashboardPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.systemtapgui.SystemTapGUISettings;

/**
 * This class is used to generate the entire Dashboard module list.  It
 * gets the list of directories that the user has specified contain modules
 * and then calls the TreeBuilder on each of them to build up the entire list.
 * @author Ryan Morse
 */
public final class DashboardModuleLocator {
	/**
	 * This is the main method for the class.  It first gathers up all the directories
	 * that contain modules and then goes through each directory and adds the modules
	 * contained in that folder to the list.  Once finished, it will return the entire
	 * list.
	 * @return The entire list of Dashboard Modules organized as a Tree
	 */
	public static TreeNode getModules() {
		TreeNode root = new TreeNode(null, "", false); //$NON-NLS-1$

		String[] locations = getModuleLocations();
		DashboardModuleTreeBuilder dmtb = null;
		if(null != locations) {
			for(int i=0; i<locations.length; i++) {
				dmtb = new DashboardModuleTreeBuilder(root);
				dmtb.generateTree(new File(locations[i]));
			}
			root.sortTree();
		}

		return root;
	}

	/**
	 * This method gets all of the dashboard module directories from the user's
	 * preferences and adds the default directories, then returns the entire list.
	 * @return String array containing all of the directories with modules.
	 */
	public static String[] getModuleLocations() {
		IPreferenceStore store = DashboardPlugin.getDefault().getPreferenceStore();

		String locations = store.getString(DashboardPreferenceConstants.P_MODULE_FOLDERS);

		String[] folders = locations.split(File.pathSeparator);

		String[] allFolders;
		if(locations.length() > 0) {
			allFolders = new String[folders.length + 2];
			System.arraycopy(folders, 0, allFolders, 2, folders.length);
		} else {
			allFolders = new String[2];
		}

		//This locates all the preexisting modules
		if(new File(moduleStore).exists()) {
			File f = new File(moduleLocation);
			if(!f.exists()) {
				f.mkdir();
				JarArchive.unjarFiles(moduleStore, moduleLocation, "modules/"); //$NON-NLS-1$
			}
			allFolders[0] = moduleLocation;
		} else {
			allFolders[0] = System.getProperty("osgi.splashLocation"); //$NON-NLS-1$
			int stapguiLoc = allFolders[0].indexOf("systemtapgui"); //$NON-NLS-1$
			if (stapguiLoc != -1) {
				allFolders[0] = allFolders[0].substring(0, stapguiLoc);
				allFolders[0] += "dashboard/modules/"; //$NON-NLS-1$
			}
		}

		allFolders[1] = SystemTapGUISettings.settingsFolder.getAbsolutePath();

		return allFolders;
	}

	public static final String moduleLocation = SystemTapGUISettings.installDirectory + "/.modules/"; //$NON-NLS-1$
	public static final String moduleStore = SystemTapGUISettings.installDirectory + "/plugins/org.eclipse.linuxtools.systemtap.ui.dashboard_1.0.0.jar"; //$NON-NLS-1$

}
