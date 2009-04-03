/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.linuxtools.valgrind.core.CommandLineConstants;
import org.eclipse.linuxtools.valgrind.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class ValgrindLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected static final String NO = "no"; //$NON-NLS-1$
	protected static final String YES = "yes"; //$NON-NLS-1$
	protected static final String EQUALS = "="; //$NON-NLS-1$

	protected static final String LOG_PREFIX = "valgrind_"; //$NON-NLS-1$
	protected static final String LOG_FILE = LOG_PREFIX + "%p.xml"; //$NON-NLS-1$
	protected static final FileFilter LOG_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(LOG_PREFIX);
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
			command = getValgrindCommand();

			// remove any output from previous run
			ValgrindUIPlugin.getDefault().resetView();
			// reset stored launch data
			getPlugin().setCurrentLaunchConfiguration(null);
			getPlugin().setCurrentLaunch(null);
			
			// find Valgrind binary if not already done
			IPath valgrindLocation = getPlugin().findValgrindLocation();

			monitor.worked(1);
			IPath exePath = verifyProgramPath(config);
			String[] arguments = getProgramArgumentsArray(config);
			File workDir = getWorkingDirectory(config);
			if (workDir == null) {
				workDir = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// set output directory in config
			setOutputPath(config);
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
			cmdLine.add(valgrindLocation.toOSString());
			cmdLine.addAll(Arrays.asList(opts));
			cmdLine.add(exePath.toOSString());
			cmdLine.addAll(Arrays.asList(arguments));
			String[] commandArray = (String[]) cmdLine.toArray(new String[cmdLine.size()]);
			boolean usePty = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			monitor.worked(1);
			
			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}
			// call Valgrind
			command.execute(commandArray, getEnvironment(config), workDir, usePty);
			monitor.worked(3);
			process = createNewProcess(launch, command.getProcess() ,commandArray[0]);
			// set the command line used
			process.setAttribute(IProcess.ATTR_CMDLINE, command.getCommandLine());
			while (!process.isTerminated()) {
				Thread.sleep(100);
			}

			if (process.getExitValue() == 0) {
				// create launch summary string to distinguish this launch
				launchStr = createLaunchStr();

				// create view
				ValgrindUIPlugin.getDefault().createView(launchStr, toolID);
				monitor.worked(1);

				// pass off control to extender
				dynamicDelegate.handleLaunch(config, launch, monitor.newChild(3));

				// refresh view
				ValgrindUIPlugin.getDefault().refreshView();

				// show view
				ValgrindUIPlugin.getDefault().showView();
				monitor.worked(1);
				
				// store these for use by other classes
				getPlugin().setCurrentLaunchConfiguration(config);
				getPlugin().setCurrentLaunch(launch);
			}
			else {
				handleValgrindError();
			}
		} catch (IOException e) {
			abort(Messages.getString("ValgrindLaunchConfigurationDelegate.Error_starting_process"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			m.done();
		}
	}

	protected IProcess createNewProcess(ILaunch launch, Process systemProcess, String programName) {
		return DebugPlugin.newProcess(launch, systemProcess, renderProcessLabel(programName));
	}

	protected ValgrindCommand getValgrindCommand() {
		return getPlugin().getValgrindCommand();
	}

	protected ValgrindLaunchPlugin getPlugin() {
		return ValgrindLaunchPlugin.getDefault();
	}

	protected void handleValgrindError() throws IOException {
		final String errorLog = readLogs();

		// find this process' console and write any error messages stored in the log to it
		IOConsole console = (IOConsole) DebugUITools.getConsole(process);

		if (console != null) {
			writeErrorsToConsole(errorLog, console);
		}
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

	protected void setOutputPath(ILaunchConfiguration config) throws CoreException, IOException {
		IValgrindOutputDirectoryProvider provider = getPlugin().getOutputDirectoryProvider();
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(LaunchConfigurationConstants.ATTR_INTERNAL_OUTPUT_DIR, provider.getOutputPath().toPortableString());
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

	protected void writeErrorsToConsole(String errors, IOConsole console)
	throws IOException {
		IOConsoleOutputStream os = null;
		try {
			os = console.newOutputStream();
			changeConsoleOutputStreamColor(os, new ConsoleColorProvider().getColor(IDebugUIConstants.ID_STANDARD_ERROR_STREAM));
			os.setActivateOnWrite(true);
			os.write(errors.getBytes());
			os.flush();
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	protected String createLaunchStr() {
		return config.getName() + " [" + getPlugin().getToolName(toolID) + "] " + process.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String[] getValgrindArgumentsArray(ILaunchConfiguration config) throws CoreException, IOException {
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

		String strpath = config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, LaunchConfigurationConstants.DEFAULT_GENERAL_SUPPFILE);
		if (!strpath.equals(EMPTY_STRING)) {
			IPath suppfile = getPlugin().parseWSPath(strpath);
			if (suppfile != null) {
				opts.add(CommandLineConstants.OPT_SUPPFILE + EQUALS + suppfile.toOSString());
			}
		}

		opts.addAll(Arrays.asList(dynamicDelegate.getCommandArray(config)));		

		String[] ret = new String[opts.size()];
		return opts.toArray(ret);
	}

	//	protected void saveState(IProgressMonitor monitor) throws CoreException, IOException {
	//		monitor.beginTask(Messages.getString("ValgrindLaunchConfigurationDelegate.Saving_Valgrind_output"), 2); //$NON-NLS-1$
	//		try {
	//			XMLMemento memento = XMLMemento.createWriteRoot(MementoConstants.ELEMENT_ROOT);
	//			//		memento.putString(MementoConstants.ELEMENT_LABEL, process.getLabel());
	//			//		memento.putString(MementoConstants.ELEMENT_DATADIR, command.getDatadir().getCanonicalPath());
	//	
	//			// write launch history to persistent storage
	//			HistoryFile.getInstance().save(launchStr, datadir, memento, config);
	//		} finally {
	//			monitor.done();
	//		}
	//	}

	protected String getTool(ILaunchConfiguration config) throws CoreException {
		return config.getAttribute(LaunchConfigurationConstants.ATTR_TOOL, LaunchConfigurationConstants.DEFAULT_TOOL);
	}

	@Override
	protected String getPluginID() {
		return ValgrindLaunchPlugin.PLUGIN_ID;
	}

	protected String readLogs() throws IOException {
		StringBuffer buf = new StringBuffer();
		BufferedReader br = null;
		try {
			File[] logs = outputPath.toFile().listFiles(LOG_FILTER);
			for (int i = 0; i < logs.length; i++) {
				br = new BufferedReader(new FileReader(logs[i]));
				String line;
				while ((line = br.readLine()) != null) {
					buf.append(line);
					buf.append('\n');
				}
				br.close();
			}
		} catch (IOException e) {
			if (br != null) {
				br.close();
			}
			throw e;
		}
		return buf.toString();
	}

	private void changeConsoleOutputStreamColor(final IOConsoleOutputStream os, final Color color) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				os.setColor(color);
			}							
		});
	}
}
