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

import org.eclipse.ui.console.*;

import java.util.ArrayList;
import java.util.Arrays;

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
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;

public class PerfLaunchConfigDelegate extends ProfileLaunchConfigurationDelegate {

	@Override
	protected String getPluginID() {
		return PerfPlugin.PLUGIN_ID;
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// check if Perf exists in $PATH
		if (! PerfCore.checkPerfInPath()) 
		{
			IStatus status = new Status(IStatus.ERROR, PerfPlugin.PLUGIN_ID, "Error: Perf was not found on PATH"); //$NON-NLS-1$
			throw new CoreException(status);
		}
		
		//Find the binary path
		IPath exePath = CDebugUtils.verifyProgramPath( config );
		
		//Get working directory
		File wd = getWorkingDirectory( config );
		if ( wd == null ) {
			wd = new File( System.getProperty( "user.home", "." ) ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		//Build the commandline string to run perf recording the given project
		String arguments[] = getProgramArgumentsArray( config ); //Program args from launch config.
		ArrayList<String> command = new ArrayList<String>( 4 + arguments.length );
		command.addAll(Arrays.asList(PerfCore.getRecordString(config))); //Get the base commandline string (with flags/options based on config)
		command.add( exePath.toOSString() ); // Add the path to the executable
		//Compile string
		command.addAll( Arrays.asList( arguments ) ); 
		String[] commandArray = command.toArray( new String[command.size()] );
		boolean usePty = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
		
		Process process;
		try {
			//Spawn the process
			process = execute( commandArray, getEnvironment( config ), wd, usePty ); 			
			createNewProcess( launch, process, commandArray[0] ); //Spawn IProcess using Debug plugin (CDT)
			
			/* This commented part is the basic method to run perf record without integrating into eclipse.
			String binCall = exePath.toOSString();
			for(String arg : arguments) {
				binCall.concat(" " + arg);
			}
			PerfCore.Run(binCall);*/
			
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

				//Find the console
				for(IConsole x : existing) {
					if (x.getName().contains(renderProcessLabel(commandArray[0]))) {
						binaryOutCons = (IOConsole)x;
					}
				}
				if ((binaryOutCons == null) && (existing.length != 0)) { //if can't be found get the most recent opened, this should probably never happen.
					if (existing[existing.length - 1] instanceof IOConsole)
						binaryOutCons = (IOConsole)existing[existing.length - 1];
				}
				
				//Get the printstream via the outputstream.
				//Get ouput stream
				OutputStream outputTo;
				if (binaryOutCons != null) {
					outputTo = binaryOutCons.newOutputStream();
					//Get the printstream for that console
					print = new PrintStream(outputTo);
				}				
				for (int i = 0; i < commandArray.length; i++) {
					print.print(commandArray[i] + " ");
				}
				
				//Print Message
				print.println();
				print.println("Analysing recorded perf.data, please wait...");
				//Possibly should pass this (the console reference) on to PerfCore.Report if theres anything we ever want to spit out to user.
			}
			
			//(Only for testing this line..) PerfCore.Report(config, null, null, null, "/home/thavidu/dev/eclipse-oprof2-workspace/org.eclipse.linuxtools.internal.perf.tests/resources/perf.data");
			IPath workingDir = Path.fromOSString(wd.toURI().getPath());
			PerfCore.Report(config, getEnvironment(config), workingDir, monitor, null, print);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String generateCommand(ILaunchConfiguration config) {
		// TODO Auto-generated method stub
		return null;
	}

}
