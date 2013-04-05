/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *    Thavidu Ranatunga (IBM) - Derived and modified code from
 *        org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.perf;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.perf.model.TreeParent;
import org.eclipse.linuxtools.internal.perf.ui.PerfProfileView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PerfPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.perf";

	// View ID
	public static final String VIEW_ID = "org.eclipse.linuxtools.perf.ui.ProfileView";
	public static final String SOURCE_DISASSEMBLY_VIEW_ID = "org.eclipse.linuxtools.perf.ui.SourceDisassemblyView";
	public static final String STAT_VIEW_ID = "org.eclipse.linuxtools.perf.ui.StatView";
	public static final String STAT_DIFF_VIEW_ID = "org.eclipse.linuxtools.perf.ui.StatViewDiff";

	// Launch Config ID
	public static final String LAUNCHCONF_ID = "org.eclipse.linuxtools.perf.launch.profile";
	
	//Perf Options tab attribs.
	public static final String ATTR_Kernel_Location = "org.eclipse.linuxtools.internal.perf.attr.Kernel.Location";
	public static final String ATTR_Kernel_Location_default = "";
	public static final String ATTR_Record_Realtime = "org.eclipse.linuxtools.internal.perf.attr.Record.Realtime";
	public static final boolean ATTR_Record_Realtime_default = false;
	public static final String ATTR_Record_Verbose = "org.eclipse.linuxtools.internal.perf.attr.Record.Verbose";
	public static final boolean ATTR_Record_Verbose_default = false;
	public static final String ATTR_SourceLineNumbers = "org.eclipse.linuxtools.internal.perf.attr.SourceLineNumbers";
	public static final boolean ATTR_SourceLineNumbers_default = true;
	public static final String ATTR_Kernel_SourceLineNumbers = "org.eclipse.linuxtools.internal.perf.attr.Kernel.SourceLineNumbers";
	public static final boolean ATTR_Kernel_SourceLineNumbers_default = false;
	public static final String ATTR_Multiplex = "org.eclipse.linuxtools.internal.perf.attr.Multiplex";
	public static final boolean ATTR_Multiplex_default = false;
	public static final String ATTR_ModuleSymbols = "org.eclipse.linuxtools.internal.perf.attr.ModuleSymbols";
	public static final boolean ATTR_ModuleSymbols_default = false;
	public static final String ATTR_HideUnresolvedSymbols = "org.eclipse.linuxtools.internal.perf.attr.HideUnresolvedSymbols";
	public static final boolean ATTR_HideUnresolvedSymbols_default = true;
	public static final String ATTR_ShowSourceDisassembly = "org.eclipse.linuxtools.internal.perf.attr.ShowSourceDisassembly";
	public static final boolean ATTR_ShowSourceDisassembly_default = false;
	public static final String ATTR_ShowStat = "org.eclipse.linuxtools.internal.perf.attr.ShowStat";
	public static final boolean ATTR_ShowStat_default = false;
	public static final String ATTR_StatRunCount = "org.eclipse.linuxtools.internal.perf.attr.StatRunCount";
	public static final int ATTR_StatRunCount_default = 1;
	
	//Perf Events tab attribs.
	public static final String ATTR_DefaultEvent = "org.eclipse.linuxtools.internal.perf.attr.DefaultEvent";
	public static final boolean ATTR_DefaultEvent_default = true;
	public static final String ATTR_MultipleEvents = "org.eclipse.linuxtools.internal.perf.attr.MultipleEvents";
	public static final boolean ATTR_MultipleEvents_default = false;
	public static final String ATTR_SelectedEvents = "org.eclipse.linuxtools.internal.perf.attr.SelectedEvents";
	public static final List<String> ATTR_SelectedEvents_default = null;
	public static final String ATTR_RawHwEvents = "org.eclipse.linuxtools.internal.perf.attr.RawHwEvents";
	public static final List<String> ATTR_RawHwEvents_default = null;
	public static final String ATTR_HwBreakpointEvents = "org.eclipse.linuxtools.internal.perf.attr.HwBreakpointEvents";
	public static final List<String> ATTR_HwBreakpointEvents_default = null;
	
	//Strings
	public static final String STRINGS_Kernel_Location = "Location of kernel image file (optional): ";
	public static final String STRINGS_Record_Realtime = "Record with realtime priority (RT SCHED_FIFO)";
	public static final String STRINGS_Record_Verbose = "Record with verbose output";
	public static final String STRINGS_ModuleSymbols = "Load Module Symbols";
	public static final String STRINGS_HideUnresolvedSymbols = "Hide Unresolved Symbols";
	public static final String STRINGS_SourceLineNumbers = "Obtain source line numbers from profile data";
	public static final String STRINGS_Kernel_SourceLineNumbers = "Obtain kernel source line numbers from profile data (Warning: May be very slow)";
	public static final String STRINGS_Multiplex = "Multiplex counter output in a single channel";
	public static final String STRINGS_RAWHWEvents = "Raw hardware event descriptor";
	public static final String STRINGS_HWBREAKPOINTS = "Hardware breakpoint";
	public static final String STRINGS_UnfiledSymbols = "Unfiled Symbols";
	public static final String STRINGS_MultipleFilesForSymbol = "Symbols conflicting in multiple files";
	public static final String STRINGS_ShowSourceDisassembly = "Show Source Disassembly View";
	public static final String STRINGS_ShowStat = "Show Stat View";
	public static final String STRINGS_SearchSourceDisassembly = "Search Source Disassembly";
	
	public static final String PERF_COMMAND = "perf";
	public static final String PERF_DEFAULT_DATA = "perf.data";
	public static final String PERF_DEFAULT_STAT= "perf.stat";
	public static final String PERF_DEAFULT_OLD_STAT = "perf.old.stat";
	public static final boolean DEBUG_ON = false; //Spew debug messages or not.

	
	// The shared instance
	private static PerfPlugin plugin;
	
	// Model Root
	private TreeParent _modelRoot;

	// Source Disassembly Data
	private SourceDisassemblyData sourceDisassemblyData;

	// Stat Data
	private StatData statData;

	// Profile view
	private PerfProfileView _ProfileView = null;

	// Current profile data
	private IPath curProfileData;

	// Current working directory
	private IPath curWorkingDir;

	// Current stat comparison data
	private StatComparisonData statDiffData;

	public TreeParent getModelRoot() {
		return _modelRoot;
	}

	public SourceDisassemblyData getSourceDisassemblyData () {
		return sourceDisassemblyData;
	}

	public StatData getStatData () {
		return statData;
	}

	public IPath getPerfProfileData() {
		return curProfileData;
	}

	public StatComparisonData getStatDiffData() {
		return statDiffData;
	}

	public IPath getWorkingDir(){
		return curWorkingDir;
	}

	/**
	 * Get perf file with specified name under the current profiled project.
	 *
	 * @param fileName file name.
	 * @return File corresponding to given file or null if no working directory
	 *         has been set.
	 */
	public File getPerfFile(String fileName) {
		if (curWorkingDir != null) {
			IPath curStatPath = curWorkingDir.append(fileName);
			return curStatPath.toFile();
		}
		return null;
	}

	/**
	 * Return cleared model root.
	 * @return TreeParent cleared model root.
	 */
	public TreeParent clearModelRoot(){
		if (_modelRoot == null) {
			_modelRoot = new TreeParent("");
		} else {
			_modelRoot.clear();
		}
		return _modelRoot;
	}

	public void setModelRoot(TreeParent rootnode) {
		this._modelRoot = rootnode;
	}

	public void setSourceDisassemblyData (SourceDisassemblyData sourceDisassemblyData) {
		this.sourceDisassemblyData = sourceDisassemblyData;
	}

	public void setStatData (StatData statData) {
		this.statData = statData;
	}

	public void setPerfProfileData(IPath perfProfileData) {
		this.curProfileData = perfProfileData;
	}

	public void setStatDiffData(StatComparisonData diffData){
		this.statDiffData = diffData;
	}

	public void setWorkingDir(IPath workingDir){
		curWorkingDir = workingDir;
	}

	public PerfProfileView getProfileView() {
		if (_ProfileView == null) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PerfPlugin.VIEW_ID);
			} catch (PartInitException e2) {
				e2.printStackTrace();
			}
		}
		return _ProfileView;
	}

	public void setProfileView(PerfProfileView profileView) {
		this._ProfileView = profileView;
	}
	
	/**
	 * The constructor
	 */
	public PerfPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PerfPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Log the given exception and display the message/reason in an error
	 * message box. (From org.eclipse.linuxtools.packagekit.ui.Activator)
	 *
	 * @param ex the given exception to display
	 * @since 2.0
	 */
	public void openError(Exception ex, final String title) {
		StringWriter writer = new StringWriter();
		ex.printStackTrace(new PrintWriter(writer));

		final String message = ex.getMessage();
		final String formattedMessage = PLUGIN_ID + " : " + message; //$NON-NLS-1$
		final Status status = new Status(IStatus.ERROR, PLUGIN_ID, formattedMessage, new Throwable(writer.toString()));

		getLog().log(status);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				ErrorDialog.openError(Display.getDefault().getActiveShell(),
						title, message, status);
			}
		});
	}

}
