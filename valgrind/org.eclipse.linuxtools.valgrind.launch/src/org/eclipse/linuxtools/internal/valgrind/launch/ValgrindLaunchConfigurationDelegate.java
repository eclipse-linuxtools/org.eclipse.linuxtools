/*******************************************************************************
 * Copyright (c) 2008, 2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 * Martin Oberhuber (Wind River) - [360085] Fix valgrind problem marker lifecycle
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCoreParser;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindError;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindInfo;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.valgrind.core.CommandLineConstants;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Version;

public class ValgrindLaunchConfigurationDelegate extends ProfileLaunchConfigurationDelegate {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected static final String NO = "no"; //$NON-NLS-1$
	protected static final String YES = "yes"; //$NON-NLS-1$
	protected static final String EQUALS = "="; //$NON-NLS-1$

	protected static final String LOG_FILE = CommandLineConstants.LOG_PREFIX + "%p.txt"; //$NON-NLS-1$
	protected static final FileFilter LOG_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(CommandLineConstants.LOG_PREFIX);
		}
	};

	protected String toolID;
	protected ValgrindCommand command;
	protected IPath outputPath;
	protected IValgrindLaunchDelegate dynamicDelegate;
	protected ILaunchConfiguration config;
	protected ILaunch launch;
	protected IProcess process;
	protected String launchStr;
	protected Version valgrindVersion; // null if not used

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor m) throws CoreException {
		if (m == null) {
			m = new NullProgressMonitor();
		}

		SubMonitor monitor = SubMonitor.convert(m, Messages.getString("ValgrindLaunchConfigurationDelegate.Profiling_Local_CCPP_Application"), 10); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		this.config = config;
		this.launch	= launch;
		try {
			IProject project = CDebugUtils.verifyCProject(config).getProject();
			command = getValgrindCommand();

			// remove any output from previous run
			ValgrindUIPlugin.getDefault().resetView();
			// reset stored launch data
			getPlugin().setCurrentLaunchConfiguration(null);
			getPlugin().setCurrentLaunch(null);

			String valgrindCommand= getValgrindCommand().getValgrindCommand();
			// also ensure Valgrind version is usable
			valgrindVersion = getPlugin().getValgrindVersion(project);

			monitor.worked(1);
			IPath exePath = CDebugUtils.verifyProgramPath(config);
			String[] arguments = getProgramArgumentsArray(config);
			File workDir = getWorkingDirectory(config);
			if (workDir == null) {
				workDir = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// set output directory in config
			IValgrindOutputDirectoryProvider provider = getPlugin().getOutputDirectoryProvider();
			setOutputPath(config, provider.getOutputPath());
			outputPath = verifyOutputPath(config);
			// create/empty output directory
			createDirectory(outputPath);

			// tool that was launched
			toolID = getTool(config);
			// ask tool extension for arguments
			dynamicDelegate = getDynamicDelegate(toolID);
			String[] opts = getValgrindArgumentsArray(config);

			// set the default source locator if required
			setDefaultSourceLocator(launch, config);

			ArrayList<String> cmdLine = new ArrayList<String>(1 + arguments.length);
			cmdLine.add(valgrindCommand);
			cmdLine.addAll(Arrays.asList(opts));
			cmdLine.add(exePath.toOSString());
			cmdLine.addAll(Arrays.asList(arguments));
			String[] commandArray = cmdLine.toArray(new String[cmdLine.size()]);
			boolean usePty = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			monitor.worked(1);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}
			// call Valgrind

			command.execute(commandArray, getEnvironment(config), workDir, valgrindCommand, usePty, project);
			monitor.worked(3);
			process = createNewProcess(launch, command.getProcess(), commandArray[0]);
			// set the command line used
			process.setAttribute(IProcess.ATTR_CMDLINE, command.getCommandLine());
			while (!process.isTerminated()) {
				Thread.sleep(100);
			}

			// store these for use by other classes
			getPlugin().setCurrentLaunchConfiguration(config);
			getPlugin().setCurrentLaunch(launch);

			// parse Valgrind logs
			IValgrindMessage[] messages = parseLogs(outputPath);

			// create launch summary string to distinguish this launch
			launchStr = createLaunchStr();

			// create view
			ValgrindUIPlugin.getDefault().createView(launchStr, toolID);
			// set log messages
			ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
			view.setMessages(messages);
			monitor.worked(1);

			// pass off control to extender
			dynamicDelegate.handleLaunch(config, launch, outputPath, monitor.newChild(2));

			// initialize tool-specific part of view
			dynamicDelegate.initializeView(view.getDynamicView(), launchStr, monitor.newChild(1));

			// refresh view
			ValgrindUIPlugin.getDefault().refreshView();

			// show view
			ValgrindUIPlugin.getDefault().showView();

			// set up resource listener for post-build events.
			ResourcesPlugin.getWorkspace().addResourceChangeListener(
					new ProjectBuildListener(project), IResourceChangeEvent.POST_BUILD);

			monitor.worked(1);
		} catch (IOException e) {
			abort(Messages.getString("ValgrindLaunchConfigurationDelegate.Error_starting_process"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			m.done();
		}
	}

	protected IValgrindMessage[] parseLogs(IPath outputPath) throws IOException, CoreException {
		List<IValgrindMessage> messages = new ArrayList<IValgrindMessage>();

		for (File log : outputPath.toFile().listFiles(LOG_FILTER)) {
			ValgrindCoreParser parser = new ValgrindCoreParser(log, launch);
			IValgrindMessage[] results = parser.getMessages();

			if (results.length == 0){
				results = new IValgrindMessage[1];
				results[0] = new ValgrindInfo(null, Messages.getString("ValgrindOutputView.No_output"), launch);
			}
			messages.addAll(Arrays.asList(results));
			createMarkers(results);
		}

		return messages.toArray(new IValgrindMessage[messages.size()]);
	}

	protected void createMarkers(IValgrindMessage[] messages) throws CoreException {
		// find the topmost stack frame within the workspace to annotate with marker
		// traverse nested errors as well
		Stack<IValgrindMessage> messageStack = new Stack<IValgrindMessage>();
		messageStack.addAll(Arrays.asList(messages));
		while (!messageStack.isEmpty()) {
			IValgrindMessage message = messageStack.pop();
			IMarker marker = null;
			IValgrindMessage[] children = message.getChildren();
			for (int i = 0; i < children.length; i++) {
				// if we've found our marker we don't care about any further frames in this stack
				if (children[i] instanceof ValgrindStackFrame && marker == null) {
					ValgrindStackFrame frame = (ValgrindStackFrame) children[i];
					if (frame.getLine() > 0) {
						ISourceLocator locator = frame.getLaunch().getSourceLocator();
						ISourceLookupResult result = DebugUITools.lookupSource(frame.getFile(), locator);
						Object sourceElement = result.getSourceElement();

						if (sourceElement != null) {
							// Resolve IResource in case we get a LocalFileStorage object
							if (sourceElement instanceof LocalFileStorage) {
								IPath filePath = ((LocalFileStorage) sourceElement).getFullPath();
								URI fileURI = URIUtil.toURI(filePath);
								IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
								IFile[] files = root.findFilesForLocationURI(fileURI);
								if (files.length > 0) {
									// Take the first match
									sourceElement = files[0];
								}
							}

							if (sourceElement instanceof IResource) {
								IResource resource = (IResource) sourceElement;
								marker = resource.createMarker(ValgrindLaunchPlugin.MARKER_TYPE);
								marker.setAttribute(IMarker.MESSAGE, message.getText());
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
								marker.setAttribute(IMarker.LINE_NUMBER, frame.getLine());
							}
						}
					}
				}
				else if (children[i] instanceof ValgrindError) {
					// nested error
					messageStack.push(children[i]);
				}
			}
		}
	}

	@Override
	protected IProcess createNewProcess(ILaunch launch, Process systemProcess, String programName) {
		return DebugPlugin.newProcess(launch, systemProcess, renderProcessLabel(programName));
	}

	protected ValgrindCommand getValgrindCommand() {
		return getPlugin().getValgrindCommand();
	}

	protected ValgrindLaunchPlugin getPlugin() {
		return ValgrindLaunchPlugin.getDefault();
	}

	protected IValgrindLaunchDelegate getDynamicDelegate(String toolID) throws CoreException {
		return getPlugin().getToolDelegate(toolID);
	}

	protected IPath verifyOutputPath(ILaunchConfiguration config) throws CoreException {
		IPath result = null;
		String strPath = config.getAttribute(LaunchConfigurationConstants.ATTR_INTERNAL_OUTPUT_DIR, (String) null);
		if (strPath != null) {
			result = Path.fromPortableString(strPath);
		}
		if (result == null) {
			abort(Messages.getString("ValgrindLaunchConfigurationDelegate.Retrieving_location_failed"), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}
		return result;
	}

	protected void setOutputPath(ILaunchConfiguration config, IPath outputPath) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(LaunchConfigurationConstants.ATTR_INTERNAL_OUTPUT_DIR, outputPath.toPortableString());
		wc.doSave();
	}

	protected void createDirectory(IPath path) throws IOException {
		File outputDir = path.toFile();

		if (outputDir.exists()) {
			// delete any preexisting files
			for (File outputFile : outputDir.listFiles()) {
				if (outputFile.isFile() && !outputFile.delete()) {
					throw new IOException(NLS.bind(Messages.getString("ValgrindOutputDirectory.Couldnt_delete"), outputFile.getAbsolutePath())); //$NON-NLS-1$
				}
			}
		}
		else if (!outputDir.mkdir()) {
			throw new IOException(NLS.bind(Messages.getString("ValgrindOutputDirectory.Couldnt_create"), outputDir.getAbsolutePath())); //$NON-NLS-1$
		}
	}

	protected String createLaunchStr() {
		return config.getName() + " [" + getPlugin().getToolName(toolID) + "] " + process.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String[] getValgrindArgumentsArray(ILaunchConfiguration config) throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();
		opts.add(CommandLineConstants.OPT_TOOL + EQUALS + getPlugin().getToolName(toolID));
		opts.add(CommandLineConstants.OPT_QUIET); // suppress uninteresting output
		opts.add(CommandLineConstants.OPT_LOGFILE + EQUALS + outputPath.append(LOG_FILE).toOSString());

		opts.add(CommandLineConstants.OPT_TRACECHILD + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, LaunchConfigurationConstants.DEFAULT_GENERAL_TRACECHILD) ? YES : NO));
		opts.add(CommandLineConstants.OPT_CHILDSILENT + EQUALS + YES); // necessary for parsing
		opts.add(CommandLineConstants.OPT_FREERES + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, LaunchConfigurationConstants.DEFAULT_GENERAL_FREERES) ? YES : NO));

		opts.add(CommandLineConstants.OPT_DEMANGLE + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, LaunchConfigurationConstants.DEFAULT_GENERAL_DEMANGLE) ? YES : NO));
		opts.add(CommandLineConstants.OPT_NUMCALLERS + EQUALS + config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, LaunchConfigurationConstants.DEFAULT_GENERAL_NUMCALLERS));
		opts.add(CommandLineConstants.OPT_ERRLIMIT + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, LaunchConfigurationConstants.DEFAULT_GENERAL_ERRLIMIT) ? YES : NO));
		opts.add(CommandLineConstants.OPT_BELOWMAIN + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, LaunchConfigurationConstants.DEFAULT_GENERAL_BELOWMAIN) ? YES : NO));
		opts.add(CommandLineConstants.OPT_MAXFRAME + EQUALS + config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, LaunchConfigurationConstants.DEFAULT_GENERAL_MAXFRAME));

		// 3.4.0 specific
		if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
			boolean useMainStack = config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK_BOOL, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK_BOOL);
			if (useMainStack) {
				opts.add(CommandLineConstants.OPT_MAINSTACK + EQUALS + config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAINSTACK, LaunchConfigurationConstants.DEFAULT_GENERAL_MAINSTACK));
			}
		}

		// 3.6.0 specific
		if (valgrindVersion == null || valgrindVersion.compareTo(ValgrindLaunchPlugin.VER_3_6_0) >= 0) {
			if (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DSYMUTIL, LaunchConfigurationConstants.DEFAULT_GENERAL_DSYMUTIL) != LaunchConfigurationConstants.DEFAULT_GENERAL_DSYMUTIL)
				opts.add(CommandLineConstants.OPT_DSYMUTIL + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DSYMUTIL, LaunchConfigurationConstants.DEFAULT_GENERAL_DSYMUTIL) ? YES : NO));
		}

		List<?> suppFiles = config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILES, LaunchConfigurationConstants.DEFAULT_GENERAL_SUPPFILES);
		for (Object strpath : suppFiles) {
			IPath suppfile = getPlugin().parseWSPath((String) strpath);
			if (suppfile != null) {
				opts.add(CommandLineConstants.OPT_SUPPFILE + EQUALS + suppfile.toOSString());
			}
		}
		opts.addAll(Arrays.asList(dynamicDelegate.getCommandArray(config, valgrindVersion, outputPath)));

		String[] ret = new String[opts.size()];
		return opts.toArray(ret);
	}

	protected String getTool(ILaunchConfiguration config) throws CoreException {
		return config.getAttribute(LaunchConfigurationConstants.ATTR_TOOL, LaunchConfigurationConstants.DEFAULT_TOOL);
	}

	@Override
	protected String getPluginID() {
		return ValgrindLaunchPlugin.PLUGIN_ID;
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		//Delete our own problem markers
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		root.deleteMarkers(ValgrindLaunchPlugin.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
		return super.finalLaunchCheck(configuration, mode, monitor);
	}
}
