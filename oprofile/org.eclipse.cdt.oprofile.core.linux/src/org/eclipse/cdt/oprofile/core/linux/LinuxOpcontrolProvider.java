/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.cdt.oprofile.core.IOpcontrolProvider;
import org.eclipse.cdt.oprofile.core.OpcontrolException;
import org.eclipse.cdt.oprofile.core.OprofileCorePlugin;
import org.eclipse.cdt.oprofile.core.OprofileDaemonEvent;
import org.eclipse.cdt.oprofile.core.OprofileDaemonOptions;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * A class which encapsulates running opcontrol.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class LinuxOpcontrolProvider implements IOpcontrolProvider {
	// Location of opcontrol security wrapper
	private static final String _OPCONTROL_PROGRAM = _findOpcontrol();

	// Initialize the Oprofile kernel module and oprofilefs
	private static final String _OPD_INIT_MODULE = "--init"; //$NON-NLS-1$
	
	// Setup daemon collection arguments
	private static final String _OPD_SETUP = "--setup"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE = "--separate="; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_SEPARATOR = ","; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_LIBRARY = "library"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_KERNEL = "kernel"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_THREAD = "thread"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_CPU = "cpu"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_ALL = "all"; //$NON-NLS-1$
	private static final String _OPD_SETUP_EVENT = "--event="; //$NON-NLS-1$
	private static final String _OPD_SETUP_EVENT_SEPARATOR = ":"; //$NON-NLS-1$
	private static final String _OPD_SETUP_EVENT_TRUE = "1"; //$NON-NLS-1$
	private static final String _OPD_SETUP_EVENT_FALSE = "0"; //$NON-NLS-1$
	
	// Kernel image file options
	private static final String _OPD_KERNEL_NONE = "--no-vmlinux"; //$NON-NLS-1$
	private static final String _OPD_KERNEL_FILE = "--vmlinux="; //$NON-NLS-1$
	
	// Logging verbosity
	private static final String _OPD_VERBOSE_LOGGING = "--verbose="; //$NON-NSL-1$
	private static final String _OPD_VERBOSE_ALL = "all"; //$NON-NLS-1$
	private static final String _OPD_VERBOSE_SFILE = "sfile"; //$NON-NLS-1$
	private static final String _OPD_VERBOSE_ARCS = "arcs"; //$NON-NLS-1$
	private static final String _OPD_VERBOSE_SAMPLES = "samples"; //$NON-NLS-1$
	private static final String _OPD_VERBOSE_MODULE = "module"; //$NON-NLS-1$
	private static final String _OPD_VERBOSE_MISC = "misc"; //$NON-NLS-1$
	
	// Start the daemon process without starting data collection
	private static final String _OPD_START_DAEMON = "--start-daemon"; //$NON-NLS-1$
	
	// Start collecting profiling data
	private static final String _OPD_START_COLLECTION = "--start"; //$NON-NLS-1$
	
	// Flush the collected profiling data to disk
	private static final String _OPD_DUMP = "--dump"; //$NON-NLS-1$
	
	// Stop data collection
	private static final String _OPD_STOP_COLLECTION = "--stop"; //$NON-NLS-1$
	
	// Stop data collection and stop daemon
	private static final String _OPD_SHUTDOWN = "--shutdown"; //$NON-NLS_1$
	
	// Clear out data from current session
	private static final String _OPD_RESET = "--reset"; //$NON-NLS-1$
	
	// Save data from the current session
	private static final String _OPD_SAVE_SESSION = "--save="; //$NON-NLS-1$
	
	// Unload the oprofile kernel module and oprofilefs
	private static final String _OPD_DEINIT_MODULE = "--deinit";
	
	// Logging verbosity. Specified with setupDaemon.
	private String _verbosity = null;
	
	/**
	 * Unload the kernel module and oprofilefs
	 * @throws OpcontrolException
	 */
	public void deinitModule() throws OpcontrolException {
		_runOpcontrol(_OPD_DEINIT_MODULE, true);
	}
	
	/**
	 * Dump collected profiling data
	 * @throws OpcontrolException
	 */
	public void dumpSamples() throws OpcontrolException {
		_runOpcontrol(_OPD_DUMP, true);
	}
	
	/**
	 * Loads the kernel module and oprofilefs
	 * @throws OpcontrolException
	 */
	public void initModule() throws OpcontrolException {
		_runOpcontrol(_OPD_INIT_MODULE, true);
	}
	
	/**
	 * Clears out data from current session
	 * @throws OpcontrolException
	 */
	public void reset() throws OpcontrolException {
		_runOpcontrol(_OPD_RESET, true);
	}
	
	/**
	 * Saves the current ("default") session
	 * @param name	the name to which to save the session
	 * @throws OpcontrolException
	 */
	public void saveSession(String name) throws OpcontrolException {
		ArrayList cmd = new ArrayList();
		cmd.add(_OPD_SAVE_SESSION + name);
		_runOpcontrol(cmd, true);
	}
	
	/**
	 * Give setup aruments
	 * @param args	list of parameters for daemon
	 * @throws OpcontrolException
	 */
	public void setupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) throws OpcontrolException {
		// Convert options & events to arguments for opcontrol
		ArrayList args = new ArrayList();
		args.add(_OPD_SETUP);
		_optionsToArguments(args, options);
		for (int i = 0; i < events.length; ++i) {
			_eventToArguments(args, events[i]);
		}
		_runOpcontrol(args, true);
	}
	
	/**
	 * Stop data collection and remove daemon
	 * @throws OpcontrolException
	 */
	public void shutdownDaemon() throws OpcontrolException {
		_runOpcontrol(_OPD_SHUTDOWN, true);
	}
	
	/**
	 * Start data collection (will start daemon if necessary)
	 * @throws OpcontrolException
	 */
	public void startCollection() throws OpcontrolException {
		_runOpcontrol(_OPD_START_COLLECTION, true);
	}
	
	/**
	 * Start daemon without starting profiling
	 * @throws OpcontrolException
	 */
	public void startDaemon() throws OpcontrolException {
		_runOpcontrol(_OPD_START_DAEMON, true);
	}
	
	/**
	 * Stop data collection
	 * @throws OpcontrolException
	 */
	public void stopCollection() throws OpcontrolException {
		_runOpcontrol(_OPD_STOP_COLLECTION, true);
	}
	
	// Convenience function
	private void _runOpcontrol(String cmd, boolean drainOutput) throws OpcontrolException {
		ArrayList list = new ArrayList();
		list.add(cmd);
		_runOpcontrol(list, drainOutput);
	}
	
	// Will add opcontrol program to beginning of args
	// args: list of opcontrol arguments (not including opcontrol program itself)
	private void _runOpcontrol(ArrayList args, boolean drainOutput) throws OpcontrolException {
		args.add(0, _OPCONTROL_PROGRAM);
		// Verbosity hack. If --start or --start-daemon, add verbosity, if set
		String cmd = (String) args.get(1);
		if ((cmd.equals (_OPD_START_COLLECTION) || cmd.equals(_OPD_START_DAEMON))
			&& _verbosity != null)
		{
			args.add(_verbosity);
		}
		
		String[] cmdArray = new String[args.size()];
		args.toArray(cmdArray);
		
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmdArray);
		} catch (IOException ioe) {
			if (p != null) {
				p.destroy();
				p = null;
			}
			
			// Throw an exception
			Status status = new Status(Status.ERROR,
									   OprofileCorePlugin.getId(),
									   0 /* code */,
									   "error message",
									   ioe);
			throw new OpcontrolException(status);
		}
		
		// TODO: I REALLY NEED TO CHECK FOR AN ERROR HERE! 
		// The problem is that userhelper doesn't return any error codes!!
		if (p != null && drainOutput) {
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			try {
				String s;
				while ((s = stdout.readLine()) != null) {
					// drain
				}
			} catch (IOException ioe) {
				// We don't care if there were errors draining the output
			}
		}
	}
	
	private static String _findOpcontrol() {
		URL url = OprofileCorePlugin.getDefault().find(new Path("opcontrol")); //$NON-NLS-1$
		if (url != null) {
			try {
				return Platform.asLocalURL(url).getPath();
			} catch (IOException e) {
			}
		}
		
		// TODO: display error in unlikely event opcontrol not found
		// (which could only happen in case of corrupt installation)
		return null;
	}	

	// Convert the event into arguments for opcontrol
	private void _eventToArguments(ArrayList args, OprofileDaemonEvent event) {
		// Event spec: "EVENT:count:mask:profileKernel:profileUser"
		String spec = new String(_OPD_SETUP_EVENT);
		spec += event.getEvent().getText();
		spec += _OPD_SETUP_EVENT_SEPARATOR;
		spec += event.getResetCount();
		spec += _OPD_SETUP_EVENT_SEPARATOR;
		spec += event.getEvent().getUnitMask().getMaskValue();
		spec += _OPD_SETUP_EVENT_SEPARATOR;
		spec += (event.getProfileKernel() ? _OPD_SETUP_EVENT_TRUE : _OPD_SETUP_EVENT_FALSE);
		spec += _OPD_SETUP_EVENT_SEPARATOR;
		spec += (event.getProfileUser() ? _OPD_SETUP_EVENT_TRUE : _OPD_SETUP_EVENT_FALSE);
		args.add(spec);
	}
	
	// Convert the options into arguments for opcontrol
	private void _optionsToArguments(ArrayList args, OprofileDaemonOptions options) {
		// Add separate flags
		int mask = options.getSeparateProfilesMask();
		if (mask != OprofileDaemonOptions.SEPARATE_NONE) {
			String separate = new String(_OPD_SETUP_SEPARATE);
			if (mask == OprofileDaemonOptions.SEPARATE_ALL) {
				separate += _OPD_SETUP_SEPARATE_ALL;
			} else {
				if ((mask & OprofileDaemonOptions.SEPARATE_LIBRARY) != 0)
					separate += _OPD_SETUP_SEPARATE_LIBRARY
							+ _OPD_SETUP_SEPARATE_SEPARATOR;
				if ((mask & OprofileDaemonOptions.SEPARATE_KERNEL) != 0)
					separate += _OPD_SETUP_SEPARATE_KERNEL
							+ _OPD_SETUP_SEPARATE_SEPARATOR;
				if ((mask & OprofileDaemonOptions.SEPARATE_THREAD) != 0)
					separate += _OPD_SETUP_SEPARATE_THREAD
							+ _OPD_SETUP_SEPARATE_SEPARATOR;
				if ((mask & OprofileDaemonOptions.SEPARATE_CPU) != 0)
					separate += _OPD_SETUP_SEPARATE_CPU
							+ _OPD_SETUP_SEPARATE_SEPARATOR;
			}
			args.add(separate);
		}
		
		// Add kernel image
		if (options.getKernelImageFile() == null || options.getKernelImageFile().equals("")) {
			args.add(_OPD_KERNEL_NONE);
		} else {
			args.add(_OPD_KERNEL_FILE + options.getKernelImageFile());
		}
		
		// Note verbosity (only support "all" now)
		if (options.getVerboseLogging()) {
			_verbosity = _OPD_VERBOSE_LOGGING + _OPD_VERBOSE_ALL;
		}
	}

}
