/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc. and others
 * (C) Copyright IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Keith Seitz <keiths@redhat.com> - setup code in launch the method, initially
 *        written in the now-defunct OprofileSession class
 *    QNX Software Systems and others - the section of code marked in the launch
 *        method, and the exec method
 *    Thavidu Ranatunga (IBM) - This code is based on the AbstractOprofileLauncher
 *        class code for the OProfile plugin.  Part of that was originally adapted
 *        from code written by QNX Software Systems and others for the CDT
 *        LocalCDILaunchDelegate class.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.launch;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.SourceDisassemblyData;
import org.eclipse.linuxtools.internal.perf.StatData;
import org.eclipse.linuxtools.internal.perf.ui.SourceDisassemblyView;
import org.eclipse.linuxtools.internal.perf.ui.StatView;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.osgi.framework.Version;

public class PerfLaunchConfigDelegate extends ProfileLaunchConfigurationDelegate {

	@Override
	protected String getPluginID() {
		return PerfPlugin.PLUGIN_ID;
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// check if Perf exists in $PATH
		if (! PerfCore.checkPerfInPath(null)) {
			IStatus status = new Status(IStatus.ERROR, PerfPlugin.PLUGIN_ID,
					Messages.PerfLaunchConfigDelegate_perf_not_found);
			throw new CoreException(status);
		}

		// Get working directory
		File wd = getWorkingDirectory(config);
		if (wd == null) {
			wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		IPath workingDir = Path.fromOSString(wd.toURI().getPath());
		PerfPlugin.getDefault().setWorkingDir(workingDir);

		if (config.getAttribute(PerfPlugin.ATTR_ShowStat,
				PerfPlugin.ATTR_ShowStat_default)) {
			showStat(config, launch);
		} else {

			//Find the binary path
			IPath exePath = CDebugUtils.verifyProgramPath( config );

			// Build the commandline string to run perf recording the given project
			// Program args from launch config.
			String arguments[] = getProgramArgumentsArray(config);

			ArrayList<String> command = new ArrayList<>();
			// Get the base commandline string (with flags/options based on config)
			Version perfVersion = PerfCore.getPerfVersion(config);
			command.addAll(Arrays.asList(PerfCore.getRecordString(config, perfVersion)));
			// Add the path to the executable
			command.add(exePath.toOSString());
			command.addAll(Arrays.asList( arguments));
			String[] commandArray = command.toArray(new String[] {});
			boolean usePty = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL,
					ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);

			Process process;
			try {
				//Spawn the process
				process = execute( commandArray, getEnvironment( config ), wd, usePty );
				createNewProcess( launch, process, commandArray[0] ); //Spawn IProcess using Debug plugin (CDT)

				//Wait for recording to complete.
				process.waitFor();
				PrintStream print = null;
				if (config.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true)) {
					//Get the console to output to.
					//This may not be the best way to accomplish this but it shall do for now.
					ConsolePlugin plugin = ConsolePlugin.getDefault();
					IConsoleManager conMan = plugin.getConsoleManager();
					IConsole[] existing = conMan.getConsoles();
					IOConsole binaryOutCons = null;

					// Find the console
					for(IConsole x : existing) {
						if (x.getName().contains(renderProcessLabel(commandArray[0]))
								&& x instanceof IOConsole) {
							binaryOutCons = (IOConsole)x;
						}
					}
					// If can't be found get the most recent opened, this should probably never happen.
					if ((binaryOutCons == null) && (existing.length != 0)) {
						if (existing[existing.length - 1] instanceof IOConsole)
							binaryOutCons = (IOConsole)existing[existing.length - 1];
					}

					//Get the printstream via the outputstream.
					//Get ouput stream
					OutputStream outputTo;
					if (binaryOutCons != null) {
						outputTo = binaryOutCons.newOutputStream();
						print = new PrintStream(outputTo);

						for (int i = 0; i < commandArray.length; i++) {
							print.print(commandArray[i] + " "); //$NON-NLS-1$
						}

						// Print Message
						print.println();
						print.println(Messages.PerfLaunchConfigDelegate_analyzing);
						// Possibly should pass this (the console reference) on to
						// PerfCore.Report if theres anything we ever want to spit
						// out to user.
					}
				}

				PerfCore.Report(config, getEnvironment(config), workingDir, monitor, null, print);
				PerfPlugin.getDefault().getPerfProfileData().toFile().setReadOnly();
				PerfCore.refreshView(renderProcessLabel(exePath.toOSString()));

				if (config.getAttribute(PerfPlugin.ATTR_ShowSourceDisassembly,
						PerfPlugin.ATTR_ShowSourceDisassembly_default)) {
					showSourceDisassembly(workingDir);
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Show source disassembly view.
	 * @param workingDir working directory.
	 */
	private void showSourceDisassembly(IPath workingDir) {
		String title = renderProcessLabel(workingDir + "perf.data"); //$NON-NLS-1$
		SourceDisassemblyData sdData = new SourceDisassemblyData(title, workingDir);
		sdData.parse();
		PerfPlugin.getDefault().setSourceDisassemblyData(sdData);
		SourceDisassemblyView.refreshView();
	}

	/**
	 * Show statistics view.
	 * @param config launch configuration
	 * @param launch launch
	 * @throws CoreException
	 */
	private void showStat(ILaunchConfiguration config, ILaunch launch)
			throws CoreException {
		//Find the binary path
		IPath exePath = CDebugUtils.verifyProgramPath( config );

		// Build the command line string
		String arguments[] = getProgramArgumentsArray(config);

		// Get working directory
		IPath workingDir = PerfPlugin.getDefault().getWorkingDir();

		int runCount = config.getAttribute(PerfPlugin.ATTR_StatRunCount,
				PerfPlugin.ATTR_StatRunCount_default);
		StringBuffer args = new StringBuffer();
		for (String arg : arguments) {
			args.append(arg);
			args.append(" "); //$NON-NLS-1$
		}

		Object[] titleArgs = new Object[]{exePath.toOSString(), args.toString(), String.valueOf(runCount)};
		String title = renderProcessLabel(MessageFormat.format(Messages.PerfLaunchConfigDelegate_stat_title, titleArgs));

		List<String> configEvents = config.getAttribute(PerfPlugin.ATTR_SelectedEvents,
				PerfPlugin.ATTR_SelectedEvents_default);

		String[] statEvents  = new String [] {};

		if(!config.getAttribute(PerfPlugin.ATTR_DefaultEvent, PerfPlugin.ATTR_DefaultEvent_default)){
			// gather selected events
			statEvents = (configEvents == null) ? statEvents : configEvents.toArray(new String[]{});
		}

		StatData sd = new StatData(title, workingDir, exePath.toOSString(), arguments, runCount, statEvents);
		sd.setLaunch(launch);
		sd.parse();
		PerfPlugin.getDefault().setStatData(sd);

		sd.updateStatData();

		StatView.refreshView();
	}

}
