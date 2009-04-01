/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.oprofile.core.IOpcontrolProvider;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.Oprofile;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.oprofile.core.daemon.OprofileDaemonOptions;

/**
 * A class which encapsulates running opcontrol.
 */
public class LinuxOpcontrolProvider implements IOpcontrolProvider {
	// Location of opcontrol security wrapper
	private static final String _OPCONTROL_REL_PATH = "natives/linux/scripts/opcontrol"; //$NON-NLS-1$
	private final String OPCONTROL_PROGRAM;

	// Initialize the Oprofile kernel module and oprofilefs
	private static final String _OPD_INIT_MODULE = "--init"; //$NON-NLS-1$
	
	// Setup daemon collection arguments
	private static final String _OPD_SETUP = "--setup"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE = "--separate="; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_SEPARATOR = ","; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_NONE = "none"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_LIBRARY = "library"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_KERNEL = "kernel"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_THREAD = "thread"; //$NON-NLS-1$
	private static final String _OPD_SETUP_SEPARATE_CPU = "cpu"; //$NON-NLS-1$

	private static final String _OPD_SETUP_EVENT = "--event="; //$NON-NLS-1$
	private static final String _OPD_SETUP_EVENT_SEPARATOR = ":"; //$NON-NLS-1$
	private static final String _OPD_SETUP_EVENT_TRUE = "1"; //$NON-NLS-1$
	private static final String _OPD_SETUP_EVENT_FALSE = "0"; //$NON-NLS-1$
	private static final String _OPD_SETUP_EVENT_DEFAULT = "default"; //$NON-NLS-1$

	private static final String _OPD_SETUP_IMAGE = "--image="; //$NON-NLS-1$

	private static final String _OPD_CALLGRAPH_DEPTH = "--callgraph="; //$NON-NLS-1$

	// Kernel image file options
	private static final String _OPD_KERNEL_NONE = "--no-vmlinux"; //$NON-NLS-1$
	private static final String _OPD_KERNEL_FILE = "--vmlinux="; //$NON-NLS-1$
	
	// Logging verbosity
//	private static final String _OPD_VERBOSE_LOGGING = "--verbose="; //$NON-NSL-1$
//	private static final String _OPD_VERBOSE_ALL = "all"; //$NON-NLS-1$
//	private static final String _OPD_VERBOSE_SFILE = "sfile"; //$NON-NLS-1$
//	private static final String _OPD_VERBOSE_ARCS = "arcs"; //$NON-NLS-1$
//	private static final String _OPD_VERBOSE_SAMPLES = "samples"; //$NON-NLS-1$
//	private static final String _OPD_VERBOSE_MODULE = "module"; //$NON-NLS-1$
//	private static final String _OPD_VERBOSE_MISC = "misc"; //$NON-NLS-1$
	
	// Start the daemon process without starting data collection
	private static final String _OPD_START_DAEMON = "--start-daemon"; //$NON-NLS-1$
	
	// Start collecting profiling data
	private static final String _OPD_START_COLLECTION = "--start"; //$NON-NLS-1$
	
	// Flush the collected profiling data to disk
	private static final String _OPD_DUMP = "--dump"; //$NON-NLS-1$
	
	// Stop data collection
	private static final String _OPD_STOP_COLLECTION = "--stop"; //$NON-NLS-1$
	
	// Stop data collection and stop daemon
	private static final String _OPD_SHUTDOWN = "--shutdown"; //$NON-NLS-1$
	
	// Clear out data from current session
	private static final String _OPD_RESET = "--reset"; //$NON-NLS-1$
	
	// Save data from the current session
	private static final String _OPD_SAVE_SESSION = "--save="; //$NON-NLS-1$
	
	// Unload the oprofile kernel module and oprofilefs
	private static final String _OPD_DEINIT_MODULE = "--deinit"; //$NON-NLS-1$
	
	// Logging verbosity. Specified with setupDaemon.
	//--verbosity=all generates WAY too much stuff in the log
	private String _verbosity = ""; //$NON-NLS-1$
	
	
	public LinuxOpcontrolProvider() throws OpcontrolException {
		OPCONTROL_PROGRAM = _findOpcontrol();
	}
	
	/**
	 * Unload the kernel module and oprofilefs
	 * @throws OpcontrolException
	 */
	public void deinitModule() throws OpcontrolException {
		_runOpcontrol(_OPD_DEINIT_MODULE);
	}
	
	/**
	 * Dump collected profiling data
	 * @throws OpcontrolException
	 */
	public void dumpSamples() throws OpcontrolException {
		_runOpcontrol(_OPD_DUMP);
	}
	
	/**
	 * Loads the kernel module and oprofilefs
	 * @throws OpcontrolException
	 */
	public void initModule() throws OpcontrolException {
		_runOpcontrol(_OPD_INIT_MODULE);
	}
	
	/**
	 * Clears out data from current session
	 * @throws OpcontrolException
	 */
	public void reset() throws OpcontrolException {
		_runOpcontrol(_OPD_RESET);
	}
	
	/**
	 * Saves the current ("default") session
	 * @param name	the name to which to save the session
	 * @throws OpcontrolException
	 */
	public void saveSession(String name) throws OpcontrolException {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(_OPD_SAVE_SESSION + name);
		_runOpcontrol(cmd);
	}
	
	/**
	 * Give setup aruments
	 * @param args	list of parameters for daemon
	 * @throws OpcontrolException
	 */
	public void setupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) throws OpcontrolException {
		// Convert options & events to arguments for opcontrol
		ArrayList<String> args = new ArrayList<String>();
		args.add(_OPD_SETUP);
		_optionsToArguments(args, options);
		if (!Oprofile.getTimerMode()) {
			if (events == null || events.length == 0) {
				args.add(_OPD_SETUP_EVENT + _OPD_SETUP_EVENT_DEFAULT);
			} else {
				for (int i = 0; i < events.length; ++i) {
					_eventToArguments(args, events[i]);
				}
			}
		}
		_runOpcontrol(args);
	}
	
	/**
	 * Stop data collection and remove daemon
	 * @throws OpcontrolException
	 */
	public void shutdownDaemon() throws OpcontrolException {
		_runOpcontrol(_OPD_SHUTDOWN);
	}
	
	/**
	 * Start data collection (will start daemon if necessary)
	 * @throws OpcontrolException
	 */
	public void startCollection() throws OpcontrolException {
		_runOpcontrol(_OPD_START_COLLECTION);
	}
	
	/**
	 * Start daemon without starting profiling
	 * @throws OpcontrolException
	 */
	public void startDaemon() throws OpcontrolException {
		_runOpcontrol(_OPD_START_DAEMON);
	}
	
	/**
	 * Stop data collection
	 * @throws OpcontrolException
	 */
	public void stopCollection() throws OpcontrolException {
		_runOpcontrol(_OPD_STOP_COLLECTION);
	}
	
	// Convenience function
	private void _runOpcontrol(String cmd) throws OpcontrolException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(cmd);
		_runOpcontrol(list);
	}
	
	// Will add opcontrol program to beginning of args
	// args: list of opcontrol arguments (not including opcontrol program itself)
	private void _runOpcontrol(ArrayList<String> args) throws OpcontrolException {
		args.add(0, OPCONTROL_PROGRAM);
		// Verbosity hack. If --start or --start-daemon, add verbosity, if set
		String cmd = (String) args.get(1);
		if (_verbosity.length() > 0 && (cmd.equals (_OPD_START_COLLECTION) || cmd.equals(_OPD_START_DAEMON))) {
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
			
			throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolRun", ioe)); //$NON-NLS-1$
		}
		
		if (p != null) {
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String output = "", s; //$NON-NLS-1$
			try {
				while ((s = stdout.readLine()) != null) {
					output += s;
				}
				
				int ret = p.waitFor();
				if (ret != 0) {
					System.out.println(output);
					throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolNonZeroExitCode", null)); //$NON-NLS-1$
				}
			} catch (IOException ioe) { 
				ioe.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String _findOpcontrol() throws OpcontrolException {
		URL url = FileLocator.find(Platform.getBundle(OprofileCorePlugin.getId()), new Path(_OPCONTROL_REL_PATH), null); 

		if (url != null) {
			try {
				return FileLocator.toFileURL(url).getPath();
			} catch (IOException ignore) { }
		} else {
			throw new OpcontrolException(OprofileCorePlugin.createErrorStatus("opcontrolProvider", null)); //$NON-NLS-1$
		}

		return null;
	}	

	// Convert the event into arguments for opcontrol
	private void _eventToArguments(ArrayList<String> args, OprofileDaemonEvent event) {
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
	private void _optionsToArguments(ArrayList<String> args, OprofileDaemonOptions options) {
		// Add separate flags
		int mask = options.getSeparateProfilesMask();

		String separate = new String(_OPD_SETUP_SEPARATE);
		
		if (mask == OprofileDaemonOptions.SEPARATE_NONE) {
			separate += _OPD_SETUP_SEPARATE_NONE;
		} else {
			//note that opcontrol will nicely ignore the trailing comma
			if ((mask & OprofileDaemonOptions.SEPARATE_LIBRARY) != 0)
				separate += _OPD_SETUP_SEPARATE_LIBRARY + _OPD_SETUP_SEPARATE_SEPARATOR;
			if ((mask & OprofileDaemonOptions.SEPARATE_KERNEL) != 0)
				separate += _OPD_SETUP_SEPARATE_KERNEL + _OPD_SETUP_SEPARATE_SEPARATOR;
			if ((mask & OprofileDaemonOptions.SEPARATE_THREAD) != 0)
				separate += _OPD_SETUP_SEPARATE_THREAD + _OPD_SETUP_SEPARATE_SEPARATOR;
			if ((mask & OprofileDaemonOptions.SEPARATE_CPU) != 0)
				separate += _OPD_SETUP_SEPARATE_CPU + _OPD_SETUP_SEPARATE_SEPARATOR;
		}
		args.add(separate);
		
		// Add kernel image
		if (options.getKernelImageFile() == null || options.getKernelImageFile().length() == 0) {
			args.add(_OPD_KERNEL_NONE);
		} else {
			args.add(_OPD_KERNEL_FILE + options.getKernelImageFile());
		}

		//image filter -- always non-null
		args.add(_OPD_SETUP_IMAGE + options.getBinaryImage());
		
		//callgraph depth
		args.add(_OPD_CALLGRAPH_DEPTH + options.getCallgraphDepth());
	}
}
