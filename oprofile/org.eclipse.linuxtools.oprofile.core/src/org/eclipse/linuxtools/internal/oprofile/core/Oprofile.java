/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelEvent;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelImage;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.checkevent.CheckEventsProcessor;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;


/**
 * Common class wrapper for all things Oprofile.
 */
public class Oprofile
{
	// Ugh. Need to know whether the module is loaded without running oprofile commands...
	private static final String[] OPROFILE_CPU_TYPE_FILES = {
		"/dev/oprofile/cpu_type", //$NON-NLS-1$
		"/proc/sys/dev/oprofile/cpu_type"  //$NON-NLS-1$
	};

	/**
	 *  Oprofile information
	 */
	private static OpInfo info;

	/**
	 *  Make sure that oprofile is ready to go
	 */
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
			initializeOprofile();

		//it still may not have loaded, if not, critical error
		if (!isKernelModuleLoaded()) {
			OprofileCorePlugin.showErrorDialog("oprofileInit", null); //$NON-NLS-1$
			//			throw new ExceptionInInitializerError(OprofileProperties.getString("fatal.kernelModuleNotLoaded")); //$NON-NLS-1$
		} else {
			initializeOprofileCore();
		}
	}

	// This requires more inside knowledge about Oprofile than one would like,
	// but it is the only way of knowing whether the module is loaded (and we can
	// succesfully call into the oprofile wrapper library without causing it to print out
	// a lot of warnings).
	/**
	 * Check whether oprofile kernel module is loaded
	 * @return true if the module is loaded, otherwise false
	 */
	private static boolean isKernelModuleLoaded() {
		if (OprofileProject.getProfilingBinary().equals(OprofileProject.OPCONTROL_BINARY)) {
			IRemoteFileProxy proxy = null;
			try {
				proxy = RemoteProxyManager.getInstance().getFileProxy(Oprofile.OprofileProject.getProject());
			} catch (CoreException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < OPROFILE_CPU_TYPE_FILES.length; ++i) {
				IFileStore f = proxy.getResource(OPROFILE_CPU_TYPE_FILES[i]);
				if (f.fetchInfo().exists())
					return true;
			}
			return false;
		} else {
			return true;
		}
	}
	/**
	 *  Initialize oprofile module by calling <code>`opcontrol --init`</code>
	 */
	private static void initializeOprofile() {
		try {
			OprofileCorePlugin.getDefault().getOpcontrolProvider().initModule();
		} catch (OpcontrolException e) {
			OprofileCorePlugin.showErrorDialog("opcontrolProvider", e); //$NON-NLS-1$
		}
	}


	/**
	 *  Initializes static data for oprofile.
	 */
	private static void initializeOprofileCore () {
		if (isKernelModuleLoaded()){
			info = OpInfo.getInfo();

			if (info == null) {
				throw new ExceptionInInitializerError(OprofileProperties.getString("fatal.opinfoNotParsed")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Queries oprofile for the number of counters on the current CPU.
	 * Used only in launch config tabs.
	 * @return the number of counters
	 */
	public static int getNumberOfCounters() {
		if (!isKernelModuleLoaded()){
			return 0;
		}
		return info.getNrCounters();
	}

	/**
	 * Returns the CPU speed of the current configuration.
	 * @return the cpu speed in MHz
	 */
	public static double getCpuFrequency() {
		return info.getCPUSpeed();
	}

	/**
	 * Finds the event with the given name
	 * @param name the event's name (i.e., CPU_CLK_UNHALTED)
	 * @return the event or <code>null</code> if not found
	 */
	public static OpEvent findEvent(String name) {
		return info.findEvent(name);
	}

	/**
	 * Get all the events that may be collected on the given counter.
	 * @param num the counter number
	 * @return an array of all valid events -- NEVER RETURNS NULL!
	 */
	public static OpEvent[] getEvents(int num) {
		return info.getEvents(num);
	}

	/**
	 * Returns the default location of the opcontrol samples directory
	 * or the project directory if the profiler is operf.
	 * @return the default samples directory
	 */
	public static String getDefaultSamplesDirectory() {
		return info.getDefault(OpInfo.DEFAULT_SAMPLE_DIR);
	}

	/**
	 * Returns the oprofile daemon log file.
	 * @return the log file (absolute pathname)
	 */
	public static String getLogFile() {
		return info.getDefault(OpInfo.DEFAULT_LOG_FILE);
	}

	/**
	 * Returns whether or not oprofile is in timer mode.
	 * @return true if oprofile is in timer mode, false otherwise
	 */
	public static boolean getTimerMode() {
		if (! isKernelModuleLoaded()){
			return true;
		}
		return info.getTimerMode();
	}

	/**
	 * Checks the requested counter, event, and unit mask for validity.
	 * @param ctr	the counter
	 * @param event	the event name
	 * @param um	the unit mask
	 * @return whether the requested event is valid
	 */
	public static Boolean checkEvent(int ctr, String event, int um) {
		int[] validResult = new int[1];
		try {
			IRunnableWithProgress opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().checkEvents(ctr, event, um, validResult);
			opxml.run(null);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}

		return (validResult[0] == CheckEventsProcessor.EVENT_OK);
	}

	/**
	 * Returns a list of all the events collected on the system, as well as
	 * the sessions under each of them.
	 * @returns a list of all collected events
	 */
	public static OpModelEvent[] getEvents() {
		OpModelEvent[] events = null;

		ArrayList<OpModelEvent> sessionList = new ArrayList<OpModelEvent>();
		try {
			IRunnableWithProgress opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().sessions(sessionList);
			opxml.run(null);
			events = new OpModelEvent[sessionList.size()];
			sessionList.toArray(events);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
		return events;
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
		}

		return image;
	}

	/**
	 * Check if oprofile kernel module is loaded and update Oprofile's information.
	 * @since 1.1
	 */
	public static void updateInfo(){
		if (!isKernelModuleLoaded()){
			initializeOprofile();
		}
		if(isKernelModuleLoaded()){
			info = OpInfo.getInfo();
		}
	}

	// Oprofile class has a static initializer and the code inside it needs to know which project
	// is being profiled in order to get the path for the Linux Tools' binaries set for that project.
	// For this reason the project property has to be set outside the Oprofile class
	/**
	 * OProfileProject class to set/get which project is being profiled
	 * @since 1.1
	 */
	public static class OprofileProject {
		private static IProject project;
		public final static String OPERF_BINARY = "operf"; //$NON-NLS-1$
		public final static String OPCONTROL_BINARY = "opcontrol"; //$NON-NLS-1$
		private static String binary = OPCONTROL_BINARY;


		/**
		 * Set the project to be profiled
		 * @param project
		 */
		public static void setProject(IProject project) {
			OprofileProject.project = project;

		}
		/**
		 * Get the project to be profiled
		 * @return project
		 */
		public static IProject getProject() {
			return project;
		}

		/**
		 * Set the profiling binary to be used (operf or opcontrol)
		 * @param binary
		 * @since 2.1
		 */
		public static void setProfilingBinary(String binary) {
			OprofileProject.binary = binary;

		}
		/**
		 * Get the profiling binary (operf or opcontrol)
		 * @return binary
		 * @since 2.1
		 */
		public static String getProfilingBinary() {
			return binary;
		}

	}

}
