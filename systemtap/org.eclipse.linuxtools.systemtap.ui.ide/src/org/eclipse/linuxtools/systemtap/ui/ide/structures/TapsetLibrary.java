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

package org.eclipse.linuxtools.systemtap.ui.ide.structures;

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.FunctionParser;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.ProbeParser;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetParser;
import org.eclipse.linuxtools.man.parser.ManPage;
import org.eclipse.linuxtools.systemtap.ui.ide.IDESessionSettings;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IUpdateListener;
import org.eclipse.linuxtools.systemtap.ui.systemtapgui.preferences.PreferenceConstants;
import org.eclipse.ui.PlatformUI;



/**
 * This class is used for obtaining all probes and functions from the tapsets.
 * It will initially try to obtain the list from the TreeSettings.xml file, but
 * if there is a problem doing that it will run the TapsetParser in order to
 * obtain everything that way.
 * @author Ryan Morse
 */
public final class TapsetLibrary {

	private static TreeNode functionTree = null;
	private static TreeNode probeTree = null;

	private static FunctionParser functionParser = null;
	private static ProbeParser probeParser = null;

	public static TreeNode getProbes() {
		return probeTree;
	}

	public static TreeNode getFunctions() {
		return functionTree;
	}

	private static HashMap<String, String> pages = new HashMap<String, String>();

	/**
	 * Returns the documentation for the given probe, function, or tapset.
	 * @since 2.0
	 */
	public static synchronized String getDocumentation(String element) {
		String documentation = pages.get(element);
		if (documentation == null) {
			documentation = (new ManPage(element)).getStrippedPage().toString();
			pages.put(element, documentation);
		}
		return documentation;
	}

	/**
	 * This method will attempt to get the most up-to-date information.
	 * However, if the TapsetParser is running already it will quit,
	 * assuming that new information will be available soon.  By registering
	 * a listener at that point the class can be notified when an update is
	 * available.
	 */
	public static void init() {
		if (null != functionParser && null != probeParser) {
			return;
		}

		IPreferenceStore preferenceStore = IDEPlugin.getDefault().getPreferenceStore();

		if (preferenceStore.contains(IDEPreferenceConstants.P_STORED_TREE)
				&& preferenceStore.getBoolean(IDEPreferenceConstants.P_STORED_TREE)
				&& isTreeFileCurrent()) {
			readTreeFile();
		} else {
			runStapParser();
		}
	}

	/**
	 * This method will create a new instance of the TapsetParser in order
	 * to get the information directly from the files.
	 */
	private static void runStapParser() {
		functionParser = FunctionParser.getInstance();
		functionParser.addListener(functionCompletionListener);
		functionParser.schedule();

		probeParser = ProbeParser.getInstance();
		probeParser.addListener(probeCompletionListener);
		probeParser.schedule();
	}

	/**
	 * @since 2.0
	 */
	public static boolean isFinishSuccessful(){
		return functionParser.isFinishSuccessful() && probeParser.isFinishSuccessful();
	}
	/**
	 * This method will get all of the tree information from
	 * the TreeSettings xml file.
	 */
	private static void readTreeFile() {
		functionTree = TreeSettings.getFunctionTree();
		probeTree = TreeSettings.getProbeTree();
	}

	/**
	 * This method checks to see if the tapsets have changed
	 * at all since the TreeSettings.xml file was created.
	 * @return boolean indicating whether or not the TreeSettings.xml file has the most up-to-date version
	 */
	private static boolean isTreeFileCurrent() {
		long treesDate = TreeSettings.getTreeFileDate();

		IPreferenceStore p = IDEPlugin.getDefault().getPreferenceStore();
		String[] tapsets = p.getString(IDEPreferenceConstants.P_TAPSETS).split(File.pathSeparator);

		File f = getTapsetLocation(p);

		if (!checkIsCurrentFolder(treesDate, f)) {
			return false;
		}

		if(null != tapsets) {
			for(int i=0; i<tapsets.length; i++) {
				f = new File(tapsets[i]);
				if (f.lastModified() > treesDate) {
					return false;
				}
				if (f.canRead() && !checkIsCurrentFolder(treesDate, f)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * This method attempts to locate the default tapset directory.
	 * @param p Preference store where the tapset location might be stored
	 * @return File representing the default tapset location.
	 */
	public static File getTapsetLocation(IPreferenceStore p) {
		File f;
		String path = p.getString(PreferenceConstants.P_ENV[2][0]);
		if(path.trim().equals("")) { //$NON-NLS-1$
			f = new File("/usr/share/systemtap/tapset"); //$NON-NLS-1$
			if(!f.exists()) {
				f = new File("/usr/local/share/systemtap/tapset"); //$NON-NLS-1$
				if(!f.exists()) {
					InputDialog i = new InputDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							Localization.getString("TapsetBrowserView.TapsetLocation"), Localization.getString("TapsetBrowserView.WhereDefaultTapset"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					i.open();
					p.setValue(PreferenceConstants.P_ENV[2][0], i.getValue());
					f = new File( i.getValue() );
				}
			}
		} else {
			f = new File( p.getString(path) );
		}
		IDESessionSettings.tapsetLocation = f.getAbsolutePath();
		return f;
	}

	/**
	 * This method checks the provided time stap against the folders
	 * time stamp.  This is to see if the folder may have new data in it
	 * @param time The current time stamp
	 * @param folder The folder to check if it is newer the then time stamp
	 * @return boolean indicating whether the time stamp is newer then the folder
	 */
	private static boolean checkIsCurrentFolder(long time, File folder) {
		File[] fs = folder.listFiles();

		for(int i=0; i<fs.length; i++) {
			if (fs[i].lastModified() > time) {
				return false;
			}

			if (fs[i].isDirectory() && fs[i].canRead()
					&& !checkIsCurrentFolder(time, fs[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds a new listener to the TapsetParser
	 * @param listener the listener to be added
	 * @return boolean indicating whether or not the listener was added
	 * @since 2.0
	 */
	public static boolean addFunctionListener(IUpdateListener listener) {
		if(null == functionParser)
			return false;
		functionParser.addListener(listener);
		return true;
	}

	/**
	 * @since 2.0
	 */
	public static boolean addProbeListener(IUpdateListener listener) {
		if(null == probeParser)
			return false;
		probeParser.addListener(listener);
		return true;
	}

	/**
	 * Removes the provided listener from the tapsetParser.
	 * @param listener The listener to be removed from the tapsetParser
	 */
	public static void removeUpdateListener(IUpdateListener listener) {
		functionParser.removeListener(listener);
	}


	private static Job cacheFunctionManpages = new Job(Localization.getString("TapsetLibrary.0")){ //$NON-NLS-1$
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			TreeNode node = functionParser.getFunctions();
			int n = node.getChildCount();
			for (int i = 0; i < n; i++) {
				getDocumentation("function::" + (node.getChildAt(i).toString())); //$NON-NLS-1$
			}

			return new Status(IStatus.OK, IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$;
		}
	};

	private static Job cacheProbeManpages = new Job(Localization.getString("TapsetLibrary.1")){ //$NON-NLS-1$
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			TreeNode node = probeParser.getProbes();
			int n = node.getChildCount();
			for (int i = 0; i < n; i++) {
				getDocumentation("tapset::" + (node.getChildAt(i).toString())); //$NON-NLS-1$
				// No need to pre-cache probes; they can be fetched pretty quickly.
			}

			return new Status(IStatus.OK, IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$;
		}
	};

	private static final IUpdateListener functionCompletionListener = new IUpdateListener() {
		@Override
		public void handleUpdateEvent() {
			functionTree = functionParser.getFunctions();
			cacheFunctionManpages.schedule();
			TreeSettings.setTrees(functionTree, probeTree);
			synchronized (functionParser) {
				functionParser.notifyAll();
			}
		}
	};

	private static final IUpdateListener probeCompletionListener = new IUpdateListener() {
		@Override
		public void handleUpdateEvent() {
			probeTree = probeParser.getProbes();
			cacheProbeManpages.schedule();
			synchronized (probeParser) {
				probeParser.notifyAll();
			}
		}
	};

	/**
	 * Blocks the current thread until the parser has finished
	 * parsing probes and functions.
	 * @since 2.0
	 */
	public static void waitForInitialization() {
		while (!functionParser.isFinishSuccessful()){
			try {
				synchronized (functionParser) {
					functionParser.wait(5000);
				}
			} catch (InterruptedException e) {
				break;
			}
		}
		while (!probeParser.isFinishSuccessful()){
			try {
				synchronized (probeParser) {
					probeParser.wait(5000);
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	/**
	 * This method will stop services started by
	 * {@link TapsetLibrary#init()} such as the {@link TapsetParser}
	 * @since 1.2
	 */
	public static void stop(){
		if(null != functionParser){
			functionParser.cancel();
			try {
				functionParser.join();
			} catch (InterruptedException e) {
				// The current thread was interrupted while waiting
				// for the parser thread to exit. Nothing to do
				// continue stopping.
			}
		}
	}
}
