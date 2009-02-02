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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.core.utils.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
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
	protected File datadir;
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
			datadir = ValgrindLaunchPlugin.getDefault().getStateLocation().toFile();
			command = new ValgrindCommand(datadir);

			// find Valgrind
			String valgrindCmd = null;
			try {
				valgrindCmd = ValgrindCommand.whichValgrind();
			} catch (IOException e) {
				abort(Messages.getString("ValgrindLaunchConfigurationDelegate.Please_ensure_Valgrind"), e, ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST); //$NON-NLS-1$
			}

			monitor.worked(1);
			IPath exePath = verifyProgramPath(config);

			String[] arguments = getProgramArgumentsArray(config);

			// tool that was launched
			toolID = getTool(config);
			// ask tool extension for arguments
			dynamicDelegate = ValgrindLaunchPlugin.getDefault().getToolDelegate(toolID);
			String[] opts = getValgrindArgumentsArray(config);

			// set the default source locator if required
			setDefaultSourceLocator(launch, config);

			File wd = getWorkingDirectory(config);
			if (wd == null) {
				wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			ArrayList<String> cmdLine = new ArrayList<String>(1 + arguments.length);
			cmdLine.add(valgrindCmd);
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
			command.execute(commandArray, getEnvironment(config), wd, usePty);
			monitor.worked(3);
			process = DebugPlugin.newProcess(launch, command.getProcess(), renderProcessLabel(commandArray[0]));

			while (!process.isTerminated()) {
				Thread.sleep(100);
			}

			// remove any output from previous run
			ValgrindUIPlugin.getDefault().resetView();

			if (process.getExitValue() == 0) {
				// create launch summary string to distinguish this launch
				launchStr = createLaunchStr();

				// create view
				ValgrindUIPlugin.getDefault().createView(launchStr, toolID);
				monitor.worked(1);

				// pass off control to extender
				dynamicDelegate.launch(command, config, launch, monitor.newChild(3));


				// refresh view
				ValgrindUIPlugin.getDefault().refreshView();

				// show view
				ValgrindUIPlugin.getDefault().showView();
				monitor.worked(1);

				// save results of launch to persistent storage
				//			saveState(monitor.newChild(2));
			}
			else {
				final String errorLog = readLogs();

				// find this process' console and write any error messages stored in the log to it
				IOConsole console = (IOConsole) DebugUITools.getConsole(process);
//				IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
//				IOConsole console = null;
//				String procLabel = process.getLabel();
//				for (int i = 0; i < consoles.length; i++) {
//					String name = consoles[i].getName();
//					if (consoles[i] instanceof IOConsole && name != null && name.contains(procLabel)) {
//						console = (IOConsole) consoles[i];
//					}
//				}

				if (console != null) {
					writeErrorsToConsole(errorLog, console);
				}
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
		return config.getName() + " [" + ValgrindLaunchPlugin.getDefault().getToolName(toolID) + "] " + process.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String[] getValgrindArgumentsArray(ILaunchConfiguration config) throws CoreException, IOException {
		ArrayList<String> opts = new ArrayList<String>();
		opts.add(ValgrindCommand.OPT_TOOL + EQUALS + ValgrindLaunchPlugin.getDefault().getToolName(toolID));
		opts.add(ValgrindCommand.OPT_QUIET);
		opts.add(ValgrindCommand.OPT_LOGFILE + EQUALS + datadir.getCanonicalPath() + File.separator + LOG_FILE);

		opts.add(ValgrindCommand.OPT_TRACECHILD + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_TRACECHILD, false) ? YES : NO));
		opts.add(ValgrindCommand.OPT_CHILDSILENT + EQUALS + YES);//(config.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_CHILDSILENT, false) ? YES : NO));
		//		opts.add(ValgrindCommand.OPT_TRACKFDS + EQUALS + (config.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TRACKFDS, false) ? YES : NO));
		//		opts.add(ValgrindCommand.OPT_TIMESTAMP + EQUALS + (config.getAttribute(ValgrindLaunchPlugin.ATTR_GENERAL_TIMESTAMP, false) ? YES : NO));
		opts.add(ValgrindCommand.OPT_FREERES + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_FREERES, true) ? YES : NO));

		opts.add(ValgrindCommand.OPT_DEMANGLE + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_DEMANGLE, true) ? YES : NO));
		opts.add(ValgrindCommand.OPT_NUMCALLERS + EQUALS + config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_NUMCALLERS, 12));
		opts.add(ValgrindCommand.OPT_ERRLIMIT + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_ERRLIMIT, true) ? YES : NO));
		opts.add(ValgrindCommand.OPT_BELOWMAIN + EQUALS + (config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_BELOWMAIN, false) ? YES : NO));
		opts.add(ValgrindCommand.OPT_MAXFRAME + EQUALS + config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_MAXFRAME, 2000000));

		String strpath = config.getAttribute(LaunchConfigurationConstants.ATTR_GENERAL_SUPPFILE, EMPTY_STRING);
		if (!strpath.equals(EMPTY_STRING)) {
			File suppfile = ValgrindLaunchPlugin.getDefault().parseWSPath(strpath);
			if (suppfile != null) {
				String escapedPath = ValgrindLaunchPlugin.getDefault().escapeAndQuote(suppfile.getCanonicalPath());
				opts.add(ValgrindCommand.OPT_SUPPFILE + EQUALS + escapedPath);
			}
		}

		opts.addAll(Arrays.asList(dynamicDelegate.getCommandArray(command, config)));		

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
		return config.getAttribute(LaunchConfigurationConstants.ATTR_TOOL, ValgrindLaunchPlugin.TOOL_EXT_DEFAULT);
	}

	@Override
	protected String getPluginID() {
		return ValgrindLaunchPlugin.PLUGIN_ID;
	}

	protected String readLogs() throws IOException {
		StringBuffer buf = new StringBuffer();
		File[] logs = command.getDatadir().listFiles(LOG_FILTER);
		for (int i = 0; i < logs.length; i++) {
			BufferedReader br = new BufferedReader(new FileReader(logs[i]));
			String line;
			while ((line = br.readLine()) != null) {
				buf.append(line);
				buf.append('\n');
			}
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
