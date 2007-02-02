/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.cdt.oprofile.core.Sample.DebugInfo;
import org.eclipse.cdt.oprofile.core.opxml.CheckEventsProcessor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;


/**
 * Common class wrapper for all things Oprofile.
 * @author Keith Seitz <keiths@redhat.com>
 */
public class Oprofile
{
	// Ugh. Need to know whether the module is loaded without running oprofile commands...
	private static final String[] _OPROFILE_CPU_TYPE_FILES = 
	{
		"/proc/sys/dev/oprofile/cpu_type",  //$NON-NLS-1$
		"/dev/oprofile/cpu_type" //$NON-NLS-1$
	};
	
	// Name of the "default" session (disk name)
	private static final String _DEFAULT_SESSION_NAME = "current"; //$NON-NLS-1$
	
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
	static public void initializeOprofileModule() {
		// Check and load the kernel module (could fail)
		_checkKernelModuleLoaded();
		
		// If the module is loaded, we can now initialize _eventList
		if (isKernelModuleLoaded()) {
			_initializeOprofileCore();
		}
	}
	
	// Initializes static data for oprofile.	
	private static void _initializeOprofileCore () {
		_info = OpInfo.getInfo();
	}
	
	/**
	 * Queries oprofile for the number of counters on the current CPU.
	 * @return the number of counters
	 */
	// ONLY FOR LAUNCH
	public static int getNumberOfCounters() {
		if (isKernelModuleLoaded())
			return _info.getNrCounters();
		return 0;
	}
	
	/**
	 * Returns the CPU speed of the current configuration.
	 * @return the cpu speed in MHz
	 */
	public static double getCpuFrequency() {
		if (isKernelModuleLoaded())
			return _info.getCPUSpeed();
		return 0.00;
	}
	
	/**
	 * Get all the events that may be collected on the given counter.
	 * (-1 for all counters)
	 * @param num the counter number
	 * @return an array of all valid events -- NEVER RETURNS NULL!
	 */
	public static OpEvent[] getEvents(int num) {
		if (isKernelModuleLoaded())
			return _info.getEvents(num);
		
		return new OpEvent[0];
	}

	/**
	 * Guess what the kernel image file in use might be. This is used by
	 * the launcher interface to present some sort of reasonable default. It's
	 * usually right. :-)
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
		} catch (Exception e) {
		}
		
		return OprofileProperties.getString("unknown-kernel"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the default location of the oprofile samples directory.
	 * @return the default samples directory
	 */
	public static String getDefaultSamplesDirectory() {
		if (isKernelModuleLoaded())
			return _info.getDefault(OpInfo.DEFAULT_SAMPLE_DIR);
		return "";
	}
	
	/**
	 * Returns the oprofile daemon log file.
	 * @return the log file (absolute pathname)
	 */
	public static String getLogFile() {
		if (isKernelModuleLoaded())
			return _info.getDefault(OpInfo.DEFAULT_LOG_FILE);
		return "";
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
	 * Returns a list of all the events collected on the system.
	 * @returns a list of all collected events
	 */
	public static SessionEvent[] getSessionEvents()
	{
		SessionEvent[] sessions = new SessionEvent[0];
		
		ArrayList sessionList = new ArrayList();
		try {
			IRunnableWithProgress opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().sessions(_info, sessionList);
			opxml.run(null);
			sessions = new SessionEvent[sessionList.size()];
			sessionList.toArray(sessions);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		} catch (OpxmlException e) {
			_showErrorDialog("opxmlProvider", e); //$NON-NLS-1$
		}
		return sessions;
	}
	
	/**
	 * Return a list of all the Samples in the given session.
	 * @param session the session for which to get samples
	 * @param shell the composite shell to use for the progress dialog
	 */
	public static void getSamples(final SampleSession session, Shell shell) {
		/* As much as I would like to get all this UI stuff back into the UI code, it really confuses
		   things. It would be a real PITA for the UI to check whether we need samples to be read.
		   Reading samples should just magically happen (as far as the UI is concerned). */
		final IRunnableWithProgress opxml;
		try {
			opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().samples(session);
		} catch (OpxmlException e) {
			_showErrorDialog("opxmlProvider", e); //$NON-NLS-1$
			return;
		}
		
		if (shell != null) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					Object[] fmtArgs = null;
					String key = null;
					if (session.isDefaultSession()) {
						key = "getSamples.caption.default-session"; //$NON-NLS-1$
						fmtArgs = new Object[0];
					} else {
						key = "getSamples.caption"; //$NON-NLS-1$
						fmtArgs = new Object[] {session.getExecutableName()};
					}
					
					String caption = MessageFormat.format(OprofileProperties.getString(key), fmtArgs);
					monitor.beginTask(caption, session.getSampleCount());				
					opxml.run(monitor);
					monitor.done();
				}
			};

			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			try {
				dialog.run(true, false, runnable);
			} catch (InterruptedException ie) {
			} catch (InvocationTargetException e) {
			}
		} else {
			// No shell -- just run opxml without a ProgressMonitor
			try {
				opxml.run(null);
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * Collects the debug information for the given sample file.
	 * There's a lot of searching going on, and it probably isn't even really needed,
	 * since all the samples and debug-info from opxml are ordered. Nonetheless,
	 * speed seems very good even with a binary search, so what the heck.
	 * 
	 * This function will set the debuginfo objects for every Sample in the ProfileImage.
	 * @param image the sample file
	 */
	public static void getDebugInfo(ProfileImage image) {
		Sample[] samples = image.getSamples(null);
		
		// Sort samples
		Arrays.sort(samples, new Comparator() {
			public int compare(Object o1, Object o2) {
				Sample a = (Sample) o1;
				Sample b = (Sample) o2;
				return a.getAddress().compareTo(b.getAddress());
			}			
		});
		
		// Run opxml and get the list of all the debug info
		ArrayList infoList = new ArrayList();
		try {
			IRunnableWithProgress opxml = OprofileCorePlugin.getDefault().getOpxmlProvider().debugInfo(image, infoList);
			opxml.run(null);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		} catch (OpxmlException e) {
			_showErrorDialog("opxmlProvider", e); //$NON-NLS-1$
		}
				
		// Loop through all the debug infos, setting the debug info for each
		// corresponding Sample.
		for (Iterator i = infoList.listIterator(); i.hasNext();) {
			DebugInfo info = (DebugInfo) i.next();
			int index =
				Arrays.binarySearch(samples, info.address, new Comparator() {
				public int compare(Object o1, Object o2) {
					String addr1 = null;
					String addr2 = null;
					if (o1 instanceof Sample) {
						addr1 = ((Sample) o1).getAddress();
						addr2 = (String) o2;
					} else {
						addr1 = (String) o1;
						addr2 = ((Sample) o2).getAddress();
					}
					return addr1.compareTo(addr2);
				}
			});

			if (index >= 0) {
				samples[index].setDebugInfo(info);
			}
		}
	}
	
	/**
	 * Finds the event with the given name
	 * @param name the event's name (i.e., CPU_CLK_UNHALTED)
	 * @return the event or <code>null</code> if not found
	 */
	public static OpEvent findEvent(String name) {
		if (isKernelModuleLoaded())
			return _info.findEvent(name);
		return null;
	}
	
	// Make sure the kernel module is loaded
	private static void _checkKernelModuleLoaded() {
		if (!isKernelModuleLoaded())
			_initializeOprofile();
	}
	
	// This requires more inside knowledge about Oprofile than one would like,
	// but it is the only way of knowing whether the module is loaded (and we can
	// succesfully call into the oprofile wrapper library without causing it to print out
	// a lot of warnings).
	public static boolean isKernelModuleLoaded() {
		for (int i = 0; i < _OPROFILE_CPU_TYPE_FILES.length; ++i) {
			File f = new File(_OPROFILE_CPU_TYPE_FILES[i]);
			if (f.exists())
				return true;
		}
		
		return false;
	}
	
	// initialize oprofile module
	private static void _initializeOprofile() {
		Throwable except = null;
		try {
			OprofileCorePlugin.getDefault().getOpcontrolProvider().initModule();
		} catch (OpcontrolException e) {
			except = e.getStatus().getException();
		} catch (Exception e) {
			except = e;
		}
		
		if (!isKernelModuleLoaded()) {
			// Could not get the kernel module loaded. Warn user.
			String smsg = OprofileProperties.getString("oprofile.init.error.status.message"); //$NON-NLS-1$
			Status status = new Status(IStatus.ERROR, OprofileCorePlugin.getId(), IStatus.OK, smsg, except);
			String title = OprofileProperties.getString("oprofile.init.error.dialog.title"); //$NON-NLS-1$
			String msg = OprofileProperties.getString("oprofile.init.error.dialog.message"); //$NON-NLS-1$
			ErrorDialog.openError(null /* parent shell */, title, msg, status);
		}
	}
	
	// Little helper function
	private static void _showErrorDialog(String key, CoreException except) {
		String title = OprofileProperties.getString(key + ".error.dialog.title"); //$NON-NLS-1$
		String msg = OprofileProperties.getString(key + ".error.dialog.message"); //$NON-NLS-1$
		ErrorDialog.openError(null /* parent shell */, title, msg, except.getStatus());
	}

	/**
	 * Is the file the "default" session?
	 * @param file the file
	 * @return boolean indicating whether the file is the default session
	 */
	public static boolean isDefaultSession(File file) {
		return (file.getName().compareTo(_DEFAULT_SESSION_NAME) == 0);
	}
}
