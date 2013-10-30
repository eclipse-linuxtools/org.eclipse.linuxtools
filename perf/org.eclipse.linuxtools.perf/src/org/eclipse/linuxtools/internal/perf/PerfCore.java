/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.perf.model.PMCommand;
import org.eclipse.linuxtools.internal.perf.model.PMDso;
import org.eclipse.linuxtools.internal.perf.model.PMEvent;
import org.eclipse.linuxtools.internal.perf.model.PMFile;
import org.eclipse.linuxtools.internal.perf.model.PMSymbol;
import org.eclipse.linuxtools.internal.perf.model.TreeParent;
import org.eclipse.linuxtools.internal.perf.ui.PerfProfileView;
import org.eclipse.linuxtools.profiling.launch.ConfigUtils;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Version;

public class PerfCore {

	public static String spitStream(BufferedReader br, String blockTitle, PrintStream print) {

		StringBuffer strBuf = new StringBuffer();
		String line = null;
		try {
			while (( line = br.readLine()) != null){
				strBuf.append(line);
				strBuf.append("\n"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			logException(e);
		}
		String str = strBuf.toString();
		if (!str.trim().isEmpty() && print != null) {
				print.println(blockTitle + ": \n" +str + "\n END OF " + blockTitle); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return str;
	}
	// Maps event lists to host names for caching
	private static HashMap<String,HashMap<String, ArrayList<String>>> eventsHostMap = null;
	private static HashMap<String,ArrayList<String>> eventList = null;

	/**
	 * Gets the list of events for a given launch configuration. Uses a cache for each host
	 * @param config
	 * @return
	 */
	public static HashMap<String,ArrayList<String>> getEventList(ILaunchConfiguration config) {
		String projectHost = getHostName(config);

		if(eventsHostMap == null){
			eventsHostMap = new HashMap<String, HashMap<String,ArrayList<String>>>();
		}

		// local projects have null hosts
		if(projectHost == null){
			projectHost = "local"; //$NON-NLS-1$
		}

		eventList = eventsHostMap.get(projectHost);

		if(eventList == null){
			eventList = loadEventList(config);
			eventsHostMap.put(projectHost, eventList);
		}
		return eventList;
	}


	/**
	 *
	 * @param config
	 * @return the name of the host in which the config's project is stored
	 */
	private static String getHostName(ILaunchConfiguration config){
		String projectName = null;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			return null;
		}
		if (projectName.isEmpty()) {
			return null;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if(project == null){
			return null;
		}
		return project.getLocationURI().getHost();

	}

	private static IProject getProject(ILaunchConfiguration config){
		if(config == null){
			return null;
		} else {
			ConfigUtils configUtils = new ConfigUtils(config);
			try {
				String projectName = configUtils.getProjectName();
				// an empty string is not a legal path to file argument for ConfigUtils.getProject
				if (projectName != null && !projectName.isEmpty()) {
					return ConfigUtils.getProject(projectName);
				}
			} catch (CoreException e1) {
				logException(e1);
			}
		}

		return null;
	}

	private static HashMap<String,ArrayList<String>> loadEventList(ILaunchConfiguration config){
		HashMap<String,ArrayList<String>> events = new HashMap<String,ArrayList<String>>();
		IProject project = getProject(config);

		if (project == null) {
			if (!PerfCore.checkPerfInPath()) {
				return events;
			}
		} else if (!PerfCore.checkRemotePerfInPath(project)) {
			return events;
		}

		Process p = null;
		BufferedReader input = null;
		try {
			// Execute "perf list" to get list of all symbolic event types.
			// Alternatively can try with -i flag.
			p = RuntimeProcessFactory.getFactory().exec(new String[] {PerfPlugin.PERF_COMMAND, "list"}, project); //(char 1 as -t is a custom field seperator //$NON-NLS-1$

			/*
			 * Old versions of Perf will send events list to stderr instead of stdout
			 * Checking if stdout is empty then read from stderr
			 */
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));

		} catch( IOException e ) {
			logException(e);
		}
		return parseEventList(input);
	}

	public static HashMap<String,ArrayList<String>> parseEventList (BufferedReader input){
		HashMap<String,ArrayList<String>> events = new HashMap<String,ArrayList<String>>();
		String line;
		try {
			// Process list of events. Each line is of the form <event>\s+<category>.
			while (( line = input.readLine()) != null){
				if (line.contains("[")) { //$NON-NLS-1$
					String event;
					String category;
					if (line.contains(PerfPlugin.STRINGS_HWBREAKPOINTS)) {
						category = PerfPlugin.STRINGS_HWBREAKPOINTS;
						event = line.substring(1,line.indexOf('[', 0)).trim();
					} else if (line.contains(PerfPlugin.STRINGS_RAWHWEvents)) {
						category = PerfPlugin.STRINGS_RAWHWEvents;
						event = line.substring(1,line.indexOf('[', 0)).trim();
					} else {
						event = line.substring(1,line.indexOf('[', 0)).trim();
						if (event.contains("OR")) { //$NON-NLS-1$
							event = event.split("OR")[0]; //filter out the abbreviations. //$NON-NLS-1$
						}
						category = line.replaceFirst(".*\\[(.+)\\]", "$1").trim(); //$NON-NLS-1$ //$NON-NLS-2$
					}
					ArrayList<String> categoryEvents = events.get(category);
					if (categoryEvents == null) {
						categoryEvents = new ArrayList<String>();
						events.put(category, categoryEvents);
					}
					categoryEvents.add(event.trim());
				}
			}
		} catch (IOException e) {
			logException(e);
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return events;
	}

	//Gets the current version of perf
	public static Version getPerfVersion(ILaunchConfiguration config, String[] environ, IPath workingDir) {
		IProject project = getProject(config);
		Process p = null;
		IRemoteFileProxy proxy = null;
		IFileStore workingDirFileStore = null;

		if (workingDir == null) {
			try {
				p = RuntimeProcessFactory.getFactory().exec(new String [] {PerfPlugin.PERF_COMMAND, "--version"}, project); //$NON-NLS-1$
			} catch (IOException e) {
				logException(e);
			}
		} else {
			try {
				proxy = RemoteProxyManager.getInstance().getFileProxy(new URI(workingDir.toOSString()));
				workingDirFileStore = proxy.getResource(workingDir.toOSString());
				p = RuntimeProcessFactory.getFactory().exec(new String [] {PerfPlugin.PERF_COMMAND, "--version"}, environ, workingDirFileStore, project); //$NON-NLS-1$
			} catch (IOException e) {
				logException(e);
			} catch (CoreException e) {
				logException(e);
			} catch (URISyntaxException e) {
				logException(e);
			}
		}

		if (p == null) {
			return null;
		}

		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String perfVersion = spitStream(input, "Perf --version", null);
		int index = perfVersion.indexOf('-');
		if (index > 0) {
			perfVersion = perfVersion.substring(0, index);
		}
		perfVersion = perfVersion.replace("perf version", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
		return new Version(perfVersion);
	}

	public static boolean checkPerfInPath()
	{
		try
		{
			Process p = Runtime.getRuntime().exec(new String [] {PerfPlugin.PERF_COMMAND, "--version"}); //$NON-NLS-1$
			return (p != null);
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public static boolean checkRemotePerfInPath(IProject project) {
		try
		{
			Process p = RuntimeProcessFactory.getFactory().exec(new String [] {PerfPlugin.PERF_COMMAND, "--version"}, project); //$NON-NLS-1$
			return (p != null);
		}
		catch (IOException e)
		{
			logException(e);
			return false;
		}
	}

	//Generates a perf record command string with the options set in the given config. (If null uses default).
	public static String [] getRecordString(ILaunchConfiguration config, Version perfVersion) {
		String [] base = new String [] {PerfPlugin.PERF_COMMAND, "record"}; //$NON-NLS-1$
		if (config == null) {
			return base;
		} else {
			ArrayList<String> newCommand = new ArrayList<String>();
			newCommand.addAll(Arrays.asList(base));
			try {
				if (new Version(3, 11, 0).compareTo(perfVersion) > 0) {
					// Removed as of 4a4d371a4dfbd3b84a7eab8d535d4c7c3647b09e from perf upstream (kernel)
					newCommand.add("-f"); //$NON-NLS-1$
				}
				if (config.getAttribute(PerfPlugin.ATTR_Record_Realtime, PerfPlugin.ATTR_Record_Realtime_default))
					newCommand.add("-r"); //$NON-NLS-1$
				if (config.getAttribute(PerfPlugin.ATTR_Record_Verbose, PerfPlugin.ATTR_Record_Verbose_default))
					newCommand.add("-v"); //$NON-NLS-1$
				if (config.getAttribute(PerfPlugin.ATTR_Multiplex, PerfPlugin.ATTR_Multiplex_default))
					newCommand.add("-M"); //$NON-NLS-1$
				List<?> selE = config.getAttribute(PerfPlugin.ATTR_SelectedEvents, PerfPlugin.ATTR_SelectedEvents_default);
				if (!config.getAttribute(PerfPlugin.ATTR_DefaultEvent, PerfPlugin.ATTR_DefaultEvent_default)
						&& selE != null) {
					for(Object e : selE) {
						newCommand.add("-e"); //$NON-NLS-1$
						newCommand.add((String)e);
					}
				}
			} catch (CoreException e) { }
			return newCommand.toArray(new String[] {});
		}
	}

	public static String[] getReportString(ILaunchConfiguration config, String perfDataLoc) {
		ArrayList<String> base = new ArrayList<String>();
		base.addAll(Arrays.asList(new String [] {PerfPlugin.PERF_COMMAND, "report", "--sort", "comm,dso,sym", "-n", "-t", "" + (char)1 }));//(char 1 as -t is a custom field seperator) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		if (config != null) {
			try {
				String kernelLoc = config.getAttribute(PerfPlugin.ATTR_Kernel_Location, PerfPlugin.ATTR_Kernel_Location_default);
				if (kernelLoc != PerfPlugin.ATTR_Kernel_Location_default) {
					base.add("--vmlinux"); //$NON-NLS-1$
					base.add(kernelLoc);
				}
				if (config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, PerfPlugin.ATTR_ModuleSymbols_default))
					base.add("-m"); //$NON-NLS-1$

				/*
				 * danielhb, 12/14/2011 - some systems, like ubuntu and sles, does not have
				 * the -U option. The binary fails to execute in those systems when this
				 * option is enabled.
				 * I'm disabling it to make the plug-in runnable for them. This
				 * will probably need to be revisited in the future, probably when this
				 * flag is implemented by the Perf binary of those systems.
				 */
				/*
				if (config.getAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, PerfPlugin.ATTR_HideUnresolvedSymbols_default))
					base.add("-U");
				 */
				if (perfDataLoc != null) {
					base.add("-i"); //$NON-NLS-1$
					base.add(perfDataLoc);
				}
			} catch (CoreException e) { }
		}
		return base.toArray( new String[base.size()] );
	}

	public static String[] getAnnotateString(ILaunchConfiguration config, String dso, String symbol, String perfDataLoc, boolean OldPerfVersion) {
		ArrayList<String> base = new ArrayList<String>();
		if (OldPerfVersion) {
			base.addAll( Arrays.asList( new String[]{PerfPlugin.PERF_COMMAND, "annotate", "-s", symbol, "-l", "-P"} ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		} else {
			base.addAll( Arrays.asList( new String[]{PerfPlugin.PERF_COMMAND, "annotate", "-d", dso, "-s", symbol, "-l", "-P"} ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		if (config != null) {
			try {
				String kernelLoc = config.getAttribute(PerfPlugin.ATTR_Kernel_Location, PerfPlugin.ATTR_Kernel_Location_default);
				if (kernelLoc != PerfPlugin.ATTR_Kernel_Location_default) {
					base.add("--vmlinux"); //$NON-NLS-1$
					base.add(kernelLoc);
				}
				if (config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, PerfPlugin.ATTR_ModuleSymbols_default))
					base.add("-m"); //$NON-NLS-1$
				if (perfDataLoc != null) {
					base.add("-i"); //$NON-NLS-1$
					base.add(perfDataLoc);
				}
			} catch (CoreException e) { }
		}

		//(Annotate string per symbol)
		return base.toArray( new String[base.size()] );
	}

	// Runs assuming perf.data has already been recorded, environ and workingDir can be set to null to use default
	//perfDataLoc is optional - it is used to provide a pre-existing data file instead of something recorded from
	//whatever project is being profiled. It is only used for junit tests atm.
	public static void Report(ILaunchConfiguration config, String[] environ, IPath workingDir, IProgressMonitor monitor, String perfDataLoc, PrintStream print) {
		IProject project = getProject(config);
		TreeParent invisibleRoot = PerfPlugin.getDefault().clearModelRoot();

		Version perfVersion = getPerfVersion(config, environ, workingDir);
		boolean OldPerfVersion = false;
		if (new Version(0, 0, 2).compareTo(perfVersion) > 0) {
			OldPerfVersion = true;
			if (print != null) { print.println("WARNING: You are running an older version of Perf, please update if you can. The plugin may produce unpredictable results."); }
		}


		BufferedReader input = null;
		BufferedReader error = null;
		Process p = null;

		if (monitor != null && monitor.isCanceled()) {
			return;
		}

		try {
			if (workingDir==null) {
				p = RuntimeProcessFactory.getFactory().exec(getReportString(config, perfDataLoc), project);
				PerfPlugin.getDefault().setPerfProfileData(new Path(perfDataLoc));
				PerfPlugin.getDefault().setWorkingDir(project.getLocation());
			} else {
				String defaultPerfDataLoc = workingDir.toOSString() + PerfPlugin.PERF_DEFAULT_DATA;
				p = RuntimeProcessFactory.getFactory().exec(getReportString(config, defaultPerfDataLoc), project);
				PerfPlugin.getDefault().setPerfProfileData(new Path(defaultPerfDataLoc));
				PerfPlugin.getDefault().setWorkingDir(workingDir);
			}

			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			//spitting error stream moved to end of while loop, due to commenting of p.waitFor()
		} catch( IOException e ) {
			logException(e);
		}


		PerfCore.parseRemoteReport(config, workingDir, monitor, perfDataLoc, print,
				invisibleRoot, OldPerfVersion, input, error, project);

	}

	/**
	 * Parse and build a tree model from the report of a perf data file
	 * @param config launch configuration
	 * @param workingDir working directory configuration
	 * @param monitor  monitor
	 * @param perfDataLoc location of perf data file
	 * @param print print stream
	 * @param invisibleRoot  root of the model
	 * @param OldPerfVersion boolean old perf version flag
	 * @param input input stream from perf data file report
	 * @param error output stream to where all standard error is written to
	 */
	public static void parseReport(ILaunchConfiguration config,
			IPath workingDir, IProgressMonitor monitor, String perfDataLoc,
			PrintStream print, TreeParent invisibleRoot,
			boolean OldPerfVersion, BufferedReader input, BufferedReader error) {
		PerfCore.parseRemoteReport(config, workingDir, monitor, perfDataLoc, print,
				invisibleRoot, OldPerfVersion, input, error, null);
	}

	public static void parseRemoteReport(ILaunchConfiguration config,
			IPath workingDir, IProgressMonitor monitor, String perfDataLoc,
			PrintStream print, TreeParent invisibleRoot,
			boolean OldPerfVersion, BufferedReader input, BufferedReader error, IProject project) {

		if (monitor != null && monitor.isCanceled()) {
			return;
		}
		String line = null;
		String items[];
		float percent;

		Process p = null;
		double samples;
		String comm,dso,symbol;
		boolean kernelFlag;
		PMEvent currentEvent = null;
		PMCommand currentCommand = null;
		PMDso currentDso = null;
		PMFile currentFile = null;
		PMSymbol currentSym = null;
		try {
			while (( line = input.readLine()) != null){
				if (monitor != null && monitor.isCanceled()) {
					return;
				}
				// line containing report information
				if ((line.startsWith("#"))) { //$NON-NLS-1$
					if (line.contains("Events:") || line.contains("Samples:")) { //$NON-NLS-1$ //$NON-NLS-2$
						String[] tmp = line.trim().split(" "); //$NON-NLS-1$
						String event = tmp[tmp.length - 1];
						// In this case, the event name is single quoted
						if (line.contains("Samples:")){ //$NON-NLS-1$
							event = event.substring(1, event.length() -1);
						}
						currentEvent = new PMEvent(event);
						invisibleRoot.addChild(currentEvent);
						currentCommand = null;
						currentDso = null;
					} else if (line.contains("Samples:")) { //"samples" was used instead of events in an older version, some incompatibilities may arise. //$NON-NLS-1$
						if (print != null) { print.println("WARNING: You are running an older version of Perf, please update if you can. The plugin may produce unpredictable results."); }
						invisibleRoot.addChild(new PMEvent("WARNING: You are running an older version of Perf, the plugin may produce unpredictable results."));
					}
					// contains profiled information
				} else {
					items = line.trim().split(""+(char)1); // using custom field separator. for default whitespace use " +" //$NON-NLS-1$
					if (items.length != 5) {
						continue;
					}
					percent = Float.parseFloat(items[0]); //percent column
					samples = Double.parseDouble(items[1].trim()); //samples column
					comm = items[2].trim(); //command column
					dso = items[3].trim(); //dso column
					symbol = items[4].trim(); //symbol column
					kernelFlag = (""+symbol.charAt(1)).equals("k"); //$NON-NLS-1$ //$NON-NLS-2$

					// initialize current command if it doesn't exist
					if ((currentCommand == null) || (!currentCommand.getName().equals(comm))) {
						currentCommand = (PMCommand) currentEvent.getChild(comm);
						if(currentCommand == null) {
							currentCommand = new PMCommand(comm);
							currentEvent.addChild(currentCommand);
						}
					}

					// initialize current dso if it doesn't exist
					if ((currentDso == null) || (!currentDso.getName().equals(dso))) {
						currentDso = (PMDso) currentCommand.getChild(dso);
						if (currentDso == null) {
							currentDso = new PMDso(dso,kernelFlag);
							currentCommand.addChild(currentDso);
						}
					}

					/*
					 *  Initialize the current file, and symbol
					 *
					 *  We won't know the name of the file containing the symbol
					 *  until we run 'perf annotate' to resolve it, so for now we
					 *  attach all symbols as children of 'Unfiled Symbols'.
					 */
					currentFile = currentDso.getFile(PerfPlugin.STRINGS_UnfiledSymbols);
					currentSym = new PMSymbol(symbol, percent, samples);
					currentFile.addChild(currentSym);
				}
			}
		} catch (IOException e) {
			logException(e);
		}
		spitStream(error,"Perf Report", print);

		boolean SourceLineNumbers = PerfPlugin.ATTR_SourceLineNumbers_default;
		boolean Kernel_SourceLineNumbers = PerfPlugin.ATTR_Kernel_SourceLineNumbers_default;
		try {
			// Check if resolving source file/line numbers is selected
			SourceLineNumbers = config.getAttribute(PerfPlugin.ATTR_SourceLineNumbers, PerfPlugin.ATTR_SourceLineNumbers_default);
			Kernel_SourceLineNumbers = config.getAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, PerfPlugin.ATTR_Kernel_SourceLineNumbers_default);
		} catch (CoreException e2) {
			SourceLineNumbers = false;
		}

		if (monitor != null && monitor.isCanceled()) {
			return;
		}

		boolean hasProfileData = invisibleRoot.getChildren().length != 0;

		if (SourceLineNumbers) {
			for (TreeParent ev : invisibleRoot.getChildren()) {
				if (!(ev instanceof PMEvent)) continue;
				for (TreeParent cmd : ev.getChildren()) {
					if (!(cmd instanceof PMCommand)) continue;
					for (TreeParent d : cmd.getChildren()) {
						if (!(d instanceof PMDso)) continue;
						currentDso = (PMDso)d;
						if ((!Kernel_SourceLineNumbers) && currentDso.isKernelDso()) continue;
						for (TreeParent s : currentDso.getFile(PerfPlugin.STRINGS_UnfiledSymbols).getChildren()) {
							if (!(s instanceof PMSymbol)) continue;

							if (monitor != null && monitor.isCanceled()) {
								return;
							}


							currentSym = (PMSymbol)s;
							String[] annotateCmd;
							if (workingDir == null) {
								annotateCmd = getAnnotateString(config, currentDso.getName(), currentSym.getName().substring(4), perfDataLoc, OldPerfVersion);
							} else {
								String perfDefaultDataLoc = workingDir + "/" + PerfPlugin.PERF_DEFAULT_DATA;
								annotateCmd = getAnnotateString(config, currentDso.getName(), currentSym.getName().substring(4), perfDefaultDataLoc, OldPerfVersion);
							}

							try {
								if(project==null) p = Runtime.getRuntime().exec(annotateCmd);
								else p = RuntimeProcessFactory.getFactory().exec(annotateCmd, project);
								input = new BufferedReader(new InputStreamReader(p.getInputStream()));
								error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
							} catch (IOException e) {
								logException(e);
							}

							PerfCore.parseAnnotation(monitor, input,
									workingDir, currentDso, currentSym);
						}

						if (currentDso.getFile(PerfPlugin.STRINGS_UnfiledSymbols).getChildren().length == 0) {
							currentDso.removeChild(currentDso.getFile(PerfPlugin.STRINGS_UnfiledSymbols));
						}
						spitStream(error,"Perf Annotate", print);
					}
				}
			}
		}

		if (print != null) {
			if (hasProfileData) {
				print.println("Profile data loaded into Perf Profile View.");
			} else {
				print.println("No profile data generated to be displayed.");
			}
		}
	}

	/**
	 * Parse annotation file for a dso given a symbol
	 * @param monitor monitor
	 * @param input annotation file input stream
	 * @param workingDir working directory configuration
	 * @param currentDso dso
	 * @param currentSym symbol
	 */
	public static void parseAnnotation(IProgressMonitor monitor,
			BufferedReader input, IPath workingDir, PMDso currentDso,
			PMSymbol currentSym) {

		if (monitor != null && monitor.isCanceled()) {
			return;
		}

		boolean grabBlock = false;
		boolean blockStarted = false;
		String dsoName,lineRef;
		String line = null;
		String items[];
		float percent;

		try {
			while (( line = input.readLine()) != null){
				if (line.startsWith("Sorted summary for file")) { //$NON-NLS-1$
					grabBlock = true;
					dsoName = line.replace("Sorted summary for file ",""); //$NON-NLS-1$ //$NON-NLS-2$
					blockStarted = false;
					if ((workingDir != null) && (dsoName.startsWith("./"))) { //$NON-NLS-1$
						if (workingDir.toOSString().endsWith("/")) { //$NON-NLS-1$
							dsoName = workingDir.toOSString() + dsoName.substring(2); // path already ends with '/', so trim './'
						} else {
							dsoName = workingDir.toOSString() + dsoName.substring(1); // path doesn't have '/', so trim just the '.'
						}
					}
					currentDso.setPath(dsoName);
				} else if (line.startsWith("---")) { //$NON-NLS-1$
					if (blockStarted) {
						blockStarted = false;
						grabBlock = false;
					} else {
						blockStarted = true;
					}
				} else if (grabBlock && blockStarted) {
					//process the line.
					items = line.trim().split(" +"); //$NON-NLS-1$
					if (items.length != 2) {
						continue;
					}
					percent = Float.parseFloat(items[0]);
					lineRef = items[1];
					items = lineRef.split(":"); //$NON-NLS-1$
					if (currentDso == null) {
						//if (PerfPlugin.DEBUG_ON) System.err.println("Parsed line ref without being in valid block, shouldn't happen.");
						break;
					} else {
						int lineNum = -1;
						try {
							lineNum = Integer.parseInt(items[1]);
						} catch (NumberFormatException e) {
							// leave line number as -1
						}
						currentSym.addPercent(lineNum, percent);
						// Symbol currently in 'Unfiled Symbols' but we now know the actual parent
						if (currentSym.getParent().getName().equals(PerfPlugin.STRINGS_UnfiledSymbols)) {
							currentSym.getParent().removeChild(currentSym);
							currentDso.getFile(items[0]).addChild(currentSym);
							// Symbol has 2 (or more) parents
						} else if (!((PMFile)currentSym.getParent()).getPath().equals(items[0])) {
							currentSym.markConflict();
							currentSym.getParent().removeChild(currentSym);
							currentDso.getFile(PerfPlugin.STRINGS_MultipleFilesForSymbol).addChild(currentSym);
						}
					}
				}
			}
		} catch (IOException e) {
			logException(e);
		}
	}

	public static void RefreshView (final String title) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					PerfProfileView view = (PerfProfileView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(PerfPlugin.VIEW_ID);
					view.setContentDescription(title);
					view.refreshModel();
				} catch (PartInitException e) {
					logException(e);
				}
			}
		});
	}

	/**
	 * Log specified exception.
	 * @param e Exception to log.
	 */
	public static void logException(Exception e) {
		Status status = new Status(IStatus.ERROR, PerfPlugin.PLUGIN_ID,
				e.getMessage());
		PerfPlugin.getDefault().getLog().log(status);
	}
}