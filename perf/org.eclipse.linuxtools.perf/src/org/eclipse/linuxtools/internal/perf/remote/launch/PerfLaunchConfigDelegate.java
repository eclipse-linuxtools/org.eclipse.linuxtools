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
package org.eclipse.linuxtools.internal.perf.remote.launch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.ui.console.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.launch.ConfigUtils;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteConnection;
import org.eclipse.linuxtools.profiling.launch.RemoteConnectionException;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

public class PerfLaunchConfigDelegate extends ProfileLaunchConfigurationDelegate {

	private ConfigUtils configUtils;
	private static String OUTPUT_STR = "--output="; //$NON-NLS-1$

	@Override
	protected String getPluginID() {
		return PerfPlugin.PLUGIN_ID;
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			this.configUtils = new ConfigUtils(config);
			IProject project = ConfigUtils.getProject(configUtils.getProjectName());
			// check if Perf exists in $PATH
			if (! PerfCore.checkRemotePerfInPath(project)) 
			{
				IStatus status = new Status(IStatus.ERROR, PerfPlugin.PLUGIN_ID, "Error: Perf was not found on PATH"); //$NON-NLS-1$
				throw new CoreException(status);
			}

			URI exeURI = new URI(configUtils.getExecutablePath());
			String configWorkingDir = configUtils.getWorkingDirectory();
			RemoteConnection exeRC = new RemoteConnection(exeURI);
			String perfPathString = RuntimeProcessFactory.getFactory().whichCommand(PerfPlugin.PERF_COMMAND, project);
			boolean copyExecutable = configUtils.getCopyExecutable();
			if (copyExecutable) {
				URI copyExeURI = new URI(configUtils.getCopyFromExecutablePath());
				RemoteConnection copyExeRC = new RemoteConnection(copyExeURI);
				IRemoteFileProxy copyExeRFP = copyExeRC.getRmtFileProxy();
				IFileStore copyExeFS = copyExeRFP.getResource(copyExeURI.getPath());
				IRemoteFileProxy exeRFP = exeRC.getRmtFileProxy();
				IFileStore exeFS = exeRFP.getResource(exeURI.getPath());
				IFileInfo exeFI = exeFS.fetchInfo();
				if (exeFI.isDirectory()) {
					// Assume the user wants to copy the file to the given directory, using
					// the same filename as the "copy from" executable.
					IPath copyExePath = Path.fromOSString(copyExeURI.getPath());
					IPath newExePath = Path.fromOSString(exeURI.getPath()).append(copyExePath.lastSegment());
					// update the exeURI with the new path.
					exeURI = new URI(exeURI.getScheme(), exeURI.getAuthority(), newExePath.toString(), exeURI.getQuery(), exeURI.getFragment());
					exeFS = exeRFP.getResource(exeURI.getPath());
				}
				copyExeFS.copy(exeFS, EFS.OVERWRITE | EFS.SHALLOW, new SubProgressMonitor(monitor, 1));
				// Note: assume that we don't need to create a new exeRC since the
				// scheme and authority remain the same between the original exeURI and the new one.
			}
			IPath remoteBinFile = Path.fromOSString(exeURI.getPath());
			IFileStore workingDir;
			if(configWorkingDir == null){
				// If no working directory was provided, use the directory containing the
				// the executable as the working directory.
				IPath workingDirPath = (Path.fromPortableString(remoteBinFile.removeLastSegments(1).toOSString()));
				IRemoteFileProxy workingDirRFP = exeRC.getRmtFileProxy();
				workingDir = workingDirRFP.getResource(workingDirPath.toOSString());
			} else {
				URI workingDirURI = new URI(configUtils.getWorkingDirectory());
				RemoteConnection workingDirRC = new RemoteConnection(workingDirURI);
				IRemoteFileProxy workingDirRFP = workingDirRC.getRmtFileProxy();
				workingDir = workingDirRFP.getResource(workingDirURI.getPath());
			}

			//Build the commandline string to run perf recording the given project
			String arguments[] = getProgramArgumentsArray( config ); //Program args from launch config.
			ArrayList<String> command = new ArrayList<String>( 4 + arguments.length );
			command.addAll(Arrays.asList(PerfCore.getRecordString(config))); //Get the base commandline string (with flags/options based on config)
			command.add( remoteBinFile.toOSString() ); // Add the path to the executable
			command.set(0, perfPathString);
			command.add(2,OUTPUT_STR + configWorkingDir + IPath.SEPARATOR + PerfPlugin.PERF_DEFAULT_DATA);
			//Compile string
			command.addAll( Arrays.asList( arguments ) );


			//Spawn the process
			String[] commandArray = command.toArray(new String[command.size()]);
			Process pProxy = RuntimeProcessFactory.getFactory().exec(commandArray, getEnvironment(config), workingDir, project);
			MessageConsole console = new MessageConsole("Perf Console", null); //$NON-NLS-1$
			console.activate();
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
			MessageConsoleStream stream = console.newMessageStream();

			if (pProxy != null) {
				BufferedReader error = new BufferedReader(
						new InputStreamReader(pProxy.getErrorStream()));
				String err;
				err = error.readLine();
				while (err != null) {
					stream.println(err);
					err = error.readLine();
				}
				error.close();
			}



			/* This commented part is the basic method to run perf record without integrating into eclipse.
						String binCall = exePath.toOSString();
						for(String arg : arguments) {
							binCall.concat(" " + arg);
						}
						PerfCore.Run(binCall);*/

			pProxy.destroy();
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

				for (int i = 0; i < command.size(); i++) {
					print.print(command.get(i) + " ");
				}

				//Print Message
				print.println();
				print.println("Analysing recorded perf.data, please wait...");
				//Possibly should pass this (the console reference) on to PerfCore.Report if theres anything we ever want to spit out to user.
			}
			PerfCore.Report(config, getEnvironment(config), Path.fromOSString(configWorkingDir + IPath.SEPARATOR), monitor, null, print);

		} catch (IOException e) {
			e.printStackTrace();
			abort(e.getLocalizedMessage(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (RemoteConnectionException e) {
			e.printStackTrace();
			abort(e.getLocalizedMessage(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			abort(e.getLocalizedMessage(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} 
	}

}
