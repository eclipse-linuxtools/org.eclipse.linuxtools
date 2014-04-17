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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.SourceDisassemblyData;
import org.eclipse.linuxtools.internal.perf.StatData;
import org.eclipse.linuxtools.internal.perf.launch.Messages;
import org.eclipse.linuxtools.internal.perf.ui.SourceDisassemblyView;
import org.eclipse.linuxtools.internal.perf.ui.StatView;
import org.eclipse.linuxtools.profiling.launch.ConfigUtils;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.launch.RemoteConnection;
import org.eclipse.linuxtools.profiling.launch.RemoteConnectionException;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.framework.Version;

public class PerfLaunchConfigDelegate extends ProfileLaunchConfigurationDelegate {

	private ConfigUtils configUtils;
	private static String OUTPUT_STR = "--output="; //$NON-NLS-1$
	private IPath binPath;
	private IPath workingDirPath;
	private IProject project;

	@Override
	protected String getPluginID() {
		return PerfPlugin.PLUGIN_ID;
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			this.configUtils = new ConfigUtils(config);
			project = configUtils.getProject();
			// check if Perf exists in $PATH
			if (! PerfCore.checkPerfInPath(project))
			{
				IStatus status = new Status(IStatus.ERROR, PerfPlugin.PLUGIN_ID, "Error: Perf was not found on PATH"); //$NON-NLS-1$
				throw new CoreException(status);
			}
			URI binURI = new URI(configUtils.getExecutablePath());
			binPath=Path.fromPortableString(binURI.toString());
			workingDirPath=Path.fromPortableString(Path.fromPortableString(binURI.toString()).removeLastSegments(2).toPortableString());
			PerfPlugin.getDefault().setWorkingDir(workingDirPath);
			if (config.getAttribute(PerfPlugin.ATTR_ShowStat,
					PerfPlugin.ATTR_ShowStat_default)) {
				showStat(config, launch);
			} else {
				URI exeURI = new URI(configUtils.getExecutablePath());
				String configWorkingDir = configUtils.getWorkingDirectory() + IPath.SEPARATOR;
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
				URI workingDirURI = new URI(RemoteProxyManager.getInstance().getRemoteProjectLocation(project));
				RemoteConnection workingDirRC = new RemoteConnection(workingDirURI);
				IRemoteFileProxy workingDirRFP = workingDirRC.getRmtFileProxy();
				workingDir = workingDirRFP.getResource(workingDirURI.getPath());
				//Build the commandline string to run perf recording the given project
				String arguments[] = getProgramArgumentsArray( config ); //Program args from launch config.
				ArrayList<String> command = new ArrayList<>( 4 + arguments.length );
				Version perfVersion = PerfCore.getPerfVersion(config);
				command.addAll(Arrays.asList(PerfCore.getRecordString(config, perfVersion))); //Get the base commandline string (with flags/options based on config)
				command.add( remoteBinFile.toOSString() ); // Add the path to the executable
				command.set(0, perfPathString);
				command.add(2,OUTPUT_STR + configWorkingDir + PerfPlugin.PERF_DEFAULT_DATA);
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
					try (BufferedReader error = new BufferedReader(
							new InputStreamReader(pProxy.getErrorStream()))) {
						String err = error.readLine();
						while (err != null) {
							stream.println(err);
							err = error.readLine();
						}
					}
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
						print.print(command.get(i) + " "); //$NON-NLS-1$
					}

					//Print Message
					print.println();
					print.println("Analysing recorded perf.data, please wait..."); //$NON-NLS-1$
					//Possibly should pass this (the console reference) on to PerfCore.Report if theres anything we ever want to spit out to user.
				}
				PerfCore.Report(config, getEnvironment(config), Path.fromOSString(configWorkingDir), monitor, null, print);

				URI perfDataURI = null;
				IRemoteFileProxy proxy = null;
				perfDataURI = new URI(RemoteProxyManager.getInstance().getRemoteProjectLocation(project) + PerfPlugin.PERF_DEFAULT_DATA);
				proxy = RemoteProxyManager.getInstance().getFileProxy(perfDataURI);
				IFileStore perfDataFileStore = proxy.getResource(perfDataURI.getPath());
				IFileInfo info = perfDataFileStore.fetchInfo();
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
				perfDataFileStore.putInfo(info, EFS.SET_ATTRIBUTES, null);

				PerfCore.refreshView(renderProcessLabel(exeURI.getPath()));
				if (config.getAttribute(PerfPlugin.ATTR_ShowSourceDisassembly,
						PerfPlugin.ATTR_ShowSourceDisassembly_default)) {
					showSourceDisassembly(Path.fromPortableString(workingDirURI.toString() + IPath.SEPARATOR));
				}

			}
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
	}

	/**
	 * Show source disassembly view.
	 * @param workingDir working directory.
	 */
	private void showSourceDisassembly(IPath workingDir) {
		String title = renderProcessLabel(workingDir.toPortableString() + PerfPlugin.PERF_DEFAULT_DATA);
		SourceDisassemblyData sdData = new SourceDisassemblyData(title, workingDir, project);
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
		// Build the command line string
		String arguments[] = getProgramArgumentsArray(config);

		// Get working directory
		int runCount = config.getAttribute(PerfPlugin.ATTR_StatRunCount,
				PerfPlugin.ATTR_StatRunCount_default);
		StringBuffer args = new StringBuffer();
		for (String arg : arguments) {
			args.append(arg);
			args.append(" "); //$NON-NLS-1$
		}
		URI binURI = null;
		try {
			binURI = new URI(binPath.toPortableString());
		} catch (URISyntaxException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
		}
		Object[] titleArgs = new Object[]{binURI.getPath(), args.toString(), String.valueOf(runCount)};
		String title = renderProcessLabel(MessageFormat.format(Messages.PerfLaunchConfigDelegate_stat_title, titleArgs));

		List<String> configEvents = config.getAttribute(PerfPlugin.ATTR_SelectedEvents,
				PerfPlugin.ATTR_SelectedEvents_default);

		String[] statEvents  = new String [] {};

		if(!config.getAttribute(PerfPlugin.ATTR_DefaultEvent, PerfPlugin.ATTR_DefaultEvent_default)){
			// gather selected events
			statEvents = (configEvents == null) ? statEvents : configEvents.toArray(new String[]{});
		}

		StatData sd = new StatData(title, workingDirPath, binURI.getPath(), arguments, runCount, statEvents, project);
		sd.setLaunch(launch);
		sd.parse();
		PerfPlugin.getDefault().setStatData(sd);
		sd.updateStatData();
		StatView.refreshView();
	}

}
