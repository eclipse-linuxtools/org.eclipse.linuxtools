/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.oprofile.core.opxml.CheckEventsProcessor;


/**
 * Common class wrapper for all things Oprofile.
 */
public class Oprofile
{
	// Ugh. Need to know whether the module is loaded without running oprofile commands...
	private static final String[] _OPROFILE_CPU_TYPE_FILES = 
	{
		"/dev/oprofile/cpu_type", //$NON-NLS-1$
		"/proc/sys/dev/oprofile/cpu_type"  //$NON-NLS-1$
	};
	
	// Oprofile information
	private static OpInfo _info;
	
	// Make sure that oprofile is ready to go
	static {
		initializeOprofileModule();
	}
	
	/**
	 * Initialize the oprofile module
	 * 
	 * This function will check if the kernel module is
	 * loaded. If it is not, it will attempt to load it
	 * (which will cause the system to prompt the user for
	 * root access).
	 */
	static private void initializeOprofileModule() {
		// Check if kernel module is loaded, if not, try to load it
		if (!isKernelModuleLoaded())
			_initializeOprofile();
		
		//it still may not have loaded, if not, critical error
		if (!isKernelModuleLoaded()) {
		
			String smsg = OprofileProperties.getString("oprofile.init.error.status.message"); //$NON-NLS-1$
			Status status = new Status(IStatus.ERROR, OprofileCorePlugin.getId(), IStatus.OK, smsg, null);
			String title = OprofileProperties.getString("oprofile.init.error.dialog.title"); //$NON-NLS-1$
			String msg = OprofileProperties.getString("oprofile.init.error.dialog.message"); //$NON-NLS-1$
			ErrorDialog.openError(null, title, msg, status);
			
		} else {
			_initializeOprofileCore();
		}
	}
	
	// This requires more inside knowledge about Oprofile than one would like,
	// but it is the only way of knowing whether the module is loaded (and we can
	// succesfully call into the oprofile wrapper library without causing it to print out
	// a lot of warnings).
	private static boolean isKernelModuleLoaded() {
		for (int i = 0; i < _OPROFILE_CPU_TYPE_FILES.length; ++i) {
			File f = new File(_OPROFILE_CPU_TYPE_FILES[i]);
			if (f.exists())
				return true;
		}
		
		return false;
	}
	
	// initialize oprofile module by calling `opcontrol --init`
	private static void _initializeOprofile() {
		try {
			OprofileCorePlugin.getDefault().getOpcontrolProvider().initModule();
		} catch (OpcontrolException e) {
			e.getStatus().getException();
		} 
	}

	
	// Initializes static data for oprofile.	
	private static void _initializeOprofileCore () {
		_info = OpInfo.getInfo();
	}
	
	/**
	 * Queries oprofile for the number of counters on the current CPU.
	 * Used only in launch config tabs.
	 * @return the number of counters
	 */
	public static int getNumberOfCounters() {
		return _info.getNrCounters();
	}
	
	/**
	 * Returns the CPU speed of the current configuration.
	 * @return the cpu speed in MHz
	 */
	public static double getCpuFrequency() {
		return _info.getCPUSpeed();
	}

	/**
	 * Finds the event with the given name
	 * @param name the event's name (i.e., CPU_CLK_UNHALTED)
	 * @return the event or <code>null</code> if not found
	 */
	public static OpEvent findEvent(String name) {
		return _info.findEvent(name);
	}

	/**
	 * Get all the events that may be collected on the given counter.
	 * (-1 for all counters)
	 * @param num the counter number
	 * @return an array of all valid events -- NEVER RETURNS NULL!
	 */
	public static OpEvent[] getEvents(int num) {
		return _info.getEvents(num);
	}

	/**
	 * Guess what the kernel image file in use might be. This is used by
	 * the launcher interface to present some sort of reasonable default.
	 * @return a possible kernel image filename
	 */
	public static String getKernelImageFile()
	{
		return "/boot/vmlinux-" + _uname(); //$NON-NLS-1$
	}
	
	 // Returns the release string from the system call uname
	private static String _uname() {
		try {
			Process p = Runtime.getRuntime().exec("uname -r"); //$NON-NLS-1$
			p.waitFor();
			if (p.exitValue() != 0) {
				return OprofileProperties.getString("unkown-kernel"); //$NON-NLS-1$
			}
		
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			return br.readLine();
		} catch (Exception e) { }
		
		return OprofileProperties.getString("unknown-kernel"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the default location of the oprofile samples directory.
	 * @return the default samples directory
	 */
	public static String getDefaultSamplesDirectory() {
		return _info.getDefault(OpInfo.DEFAULT_SAMPLE_DIR);
	}
	
	/**
	 * Returns the oprofile daemon log file.
	 * @return the log file (absolute pathname)
	 */
	public static String getLogFile() {
		return _info.getDefault(OpInfo.DEFAULT_LOG_FILE);
	}
	
	/**
	 * Checks the requested counter, event, and unit mask for vailidity.
	 * @param ctr	the counter
	 * @param event	the event number
	 * @param um	the unit mask
	 * @return whether the requested event is valid
	 */
	public static boolean checkEvent(int ctr, int event, int um) {
		int[] validResult = new int[1];
		try {
			IRunnableWithProgress opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().checkEvents(ctr, event, um, validResult);
			opxml.run(null);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		} catch (OpxmlException e) {
			_showErrorDialog("opxmlProvider", e);
		}
		
		return (validResult[0] == CheckEventsProcessor.EVENT_OK);
	}
	
	/**
	 * Returns a list of all the events collected on the system, as well as
	 * the sessions under each of them.
	 * @returns a list of all collected events
	 */
	public static OpModelEvent[] getEvents()
	{
		OpModelEvent[] events = new OpModelEvent[0];
		
		ArrayList<OpModelEvent> sessionList = new ArrayList<OpModelEvent>();
		try {
			IRunnableWithProgress opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().sessions(sessionList);
			opxml.run(null);
			events = new OpModelEvent[sessionList.size()];
			sessionList.toArray(events);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		} catch (OpxmlException e) {
			_showErrorDialog("opxmlProvider", e); //$NON-NLS-1$
		}
		return events;
	}

	//Helper function
	private static void _showErrorDialog(String key, CoreException except) {
		String title = OprofileProperties.getString(key + ".error.dialog.title"); //$NON-NLS-1$
		String msg = OprofileProperties.getString(key + ".error.dialog.message"); //$NON-NLS-1$
		ErrorDialog.openError(null, title, msg, except.getStatus());
	}

	/**
	 * Return a list of all the Samples in the given session.
	 * @param session the session for which to get samples
	 * @param shell the composite shell to use for the progress dialog
	 */
	public static OpModelImage getModelData(String eventName, String sessionName) {		
		OpModelImage image = new OpModelImage();
		
		final IRunnableWithProgress opxml;
		try {
			opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().modelData(eventName, sessionName, image);
			opxml.run(null);
		} catch (InvocationTargetException e) { 
		} catch (InterruptedException e) { 
		} catch (OpxmlException e) {
			_showErrorDialog("opxmlProvider", e); //$NON-NLS-1$
			return null;
		}

		return image;
	}
}
