/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.localgui.launch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.systemtap.localgui.core.Helper;
import org.eclipse.linuxtools.systemtap.localgui.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.MP;
import org.eclipse.linuxtools.systemtap.localgui.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapCommandGenerator;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.systemtap.localgui.graphing.SystemTapCommandParser;
import org.eclipse.linuxtools.systemtap.localgui.graphing.SystemTapView;
import org.eclipse.ui.console.TextConsole;

/**
 * Delegate for Stap scripts. The Delegate generates part of the command string
 * and schedules a job to finish generation of the command and execute.
 * 
 */
public class SystemTapLaunchConfigurationDelegate extends
		AbstractCLaunchDelegate {

	private SystemTapCommandGenerator cmdGenerator;

	@Override
	protected String getPluginID() {
		return null;
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor m) throws CoreException {

		if (m == null) {
			m = new NullProgressMonitor();
		}
		SubMonitor monitor = SubMonitor.convert(m,
				"SystemTap runtime monitor", 10); //$NON-NLS-1$

		//System.out.println("SystemTapLaunchConfigurationDelegate: launch"); //$NON-NLS-1$

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		File temporaryScript = null;
		String command = ""; //$NON-NLS-1$
		String arguments = ""; //$NON-NLS-1$
		String scriptPath = ""; //$NON-NLS-1$
		String binaryPath = ""; //$NON-NLS-1$
		String outputPath = ""; //$NON-NLS-1$
		boolean needsBinary = false; // Set to false if we want to use SystemTap
		// binary selection
		boolean needsArguments = false;
		boolean useColour = false;

		if (config.getAttribute(LaunchConfigurationConstants.USE_COLOUR,
				LaunchConfigurationConstants.DEFAULT_USE_COLOUR))
			useColour = true;

		if (config.getAttribute(LaunchConfigurationConstants.COMMAND_VERBOSE,
				LaunchConfigurationConstants.DEFAULT_COMMAND_VERBOSE)) {
			command += "-v "; //$NON-NLS-1$
		}

		if (config.getAttribute(LaunchConfigurationConstants.COMMAND_PASS,
				LaunchConfigurationConstants.DEFAULT_COMMAND_PASS) > 0) {
			command += "-p" + config.getAttribute(LaunchConfigurationConstants.COMMAND_PASS, LaunchConfigurationConstants.DEFAULT_COMMAND_PASS) + " "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_KEEP_TEMPORARY,
				LaunchConfigurationConstants.DEFAULT_COMMAND_KEEP_TEMPORARY)) {
			command += "-k "; //$NON-NLS-1$
		}

		if (config.getAttribute(LaunchConfigurationConstants.COMMAND_GURU,
				LaunchConfigurationConstants.DEFAULT_COMMAND_GURU)) {
			command += "-g "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_PROLOGUE_SEARCH,
				LaunchConfigurationConstants.DEFAULT_COMMAND_PROLOGUE_SEARCH)) {
			command += "-P "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_NO_CODE_ELISION,
				LaunchConfigurationConstants.DEFAULT_COMMAND_NO_CODE_ELISION)) {
			command += "-u "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_DISABLE_WARNINGS,
				LaunchConfigurationConstants.DEFAULT_COMMAND_DISABLE_WARNINGS)) {
			command += "-w "; //$NON-NLS-1$
		}

		if (config.getAttribute(LaunchConfigurationConstants.COMMAND_BULK_MODE,
				LaunchConfigurationConstants.DEFAULT_COMMAND_BULK_MODE)) {
			command += "-b "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_TIMING_INFO,
				LaunchConfigurationConstants.DEFAULT_COMMAND_TIMING_INFO)) {
			command += "-t "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_BUFFER_BYTES,
				LaunchConfigurationConstants.DEFAULT_COMMAND_BUFFER_BYTES) > 0) {
			command += "-s" + config.getAttribute(LaunchConfigurationConstants.COMMAND_BUFFER_BYTES, LaunchConfigurationConstants.DEFAULT_COMMAND_BUFFER_BYTES) + " "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_TARGET_PID,
				LaunchConfigurationConstants.DEFAULT_COMMAND_TARGET_PID) > 0) {
			command += "-x" + config.getAttribute(LaunchConfigurationConstants.COMMAND_TARGET_PID, LaunchConfigurationConstants.DEFAULT_COMMAND_TARGET_PID) + " "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
				LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES) != LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES) {
			command += config.getAttribute(
					LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
					LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES)
					+ " "; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_LEAVE_RUNNING,
				LaunchConfigurationConstants.DEFAULT_COMMAND_LEAVE_RUNNING)) {
			command += "-F "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_SKIP_BADVARS,
				LaunchConfigurationConstants.DEFAULT_COMMAND_SKIP_BADVARS)) {
			command += "--skip-badvars "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_IGNORE_DWARF,
				LaunchConfigurationConstants.DEFAULT_COMMAND_IGNORE_DWARF)) {
			command += "--ignore-dwarf "; //$NON-NLS-1$
		}

		if (config.getAttribute(
				LaunchConfigurationConstants.COMMAND_TAPSET_COVERAGE,
				LaunchConfigurationConstants.DEFAULT_COMMAND_TAPSET_COVERAGE)) {
			command += "-q "; //$NON-NLS-1$
		}

		if (!config.getAttribute(LaunchConfigurationConstants.ARGUMENTS,
				LaunchConfigurationConstants.DEFAULT_ARGUMENTS).equals(
				LaunchConfigurationConstants.DEFAULT_ARGUMENTS)) {
			arguments = config.getAttribute(
					LaunchConfigurationConstants.ARGUMENTS,
					LaunchConfigurationConstants.DEFAULT_ARGUMENTS);
			needsArguments = true;
		}

		// UNCOMMENT if we decide to go with a SystemTap Binary selector instead
		// of using the one in Main tab
		if (!config.getAttribute(LaunchConfigurationConstants.BINARY_PATH,
				LaunchConfigurationConstants.DEFAULT_BINARY_PATH).equals(
				LaunchConfigurationConstants.DEFAULT_BINARY_PATH)) {
			binaryPath = config.getAttribute(
					LaunchConfigurationConstants.BINARY_PATH,
					LaunchConfigurationConstants.DEFAULT_BINARY_PATH);
			needsBinary = true;
		}

		if (!config.getAttribute(LaunchConfigurationConstants.SCRIPT_PATH,
				LaunchConfigurationConstants.DEFAULT_SCRIPT_PATH).equals(
				LaunchConfigurationConstants.DEFAULT_SCRIPT_PATH)) {
			scriptPath = config.getAttribute(
					LaunchConfigurationConstants.SCRIPT_PATH,
					LaunchConfigurationConstants.DEFAULT_SCRIPT_PATH);
		}

		// Generate script if needed
		if (config.getAttribute(LaunchConfigurationConstants.NEED_TO_GENERATE,
				LaunchConfigurationConstants.DEFAULT_NEED_TO_GENERATE)) {
			temporaryScript = new File(scriptPath);
			temporaryScript.delete();

			try {
				temporaryScript.createNewFile();
				FileWriter fstream = new FileWriter(temporaryScript);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(config.getAttribute(
						LaunchConfigurationConstants.GENERATED_SCRIPT,
						LaunchConfigurationConstants.DEFAULT_GENERATED_SCRIPT));
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		outputPath = config.getAttribute(
				LaunchConfigurationConstants.OUTPUT_PATH,
				LaunchConfigurationConstants.DEFAULT_OUTPUT_PATH
						+ System.currentTimeMillis());
		command += "-o " + outputPath; //$NON-NLS-1$
		try {
			File tempFile = new File(outputPath);
			tempFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		try {
			File workDir = getWorkingDirectory(config);
			if (workDir == null) {
				workDir = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Not sure if this line is necessary
			// set the default source locator if required
			setDefaultSourceLocator(launch, config);

			// Generate the command
			cmdGenerator = new SystemTapCommandGenerator();
			String cmd = cmdGenerator.generateCommand(scriptPath, binaryPath,
					command, needsBinary, needsArguments, arguments, useColour);


			// Prepare cmd for execution - we need a command array of strings,
			// no string can contain a space character. (One of the process'
			// requirements)
			String tmp[] = cmd.split(" "); //$NON-NLS-1$
			ArrayList<String> cmdLine = new ArrayList<String>();
			for (String str : tmp) {
				cmdLine.add(str);
			}
			String[] commandArray = (String[]) cmdLine
					.toArray(new String[cmdLine.size()]);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			
			MP.println(cmd);
			boolean graphMode = config.getAttribute(
					LaunchConfigurationConstants.GRAPHICS_MODE,
					LaunchConfigurationConstants.DEFAULT_GRAPHICS_MODE);
			// Prepare a parser object - parser will read and update from the
			// output file continuously
			SystemTapCommandParser stapCmdPar = null;
			if (!graphMode) {
				stapCmdPar = new SystemTapCommandParser(Messages
						.getString("RunSystemTapAction.0"), outputPath, //$NON-NLS-1$
						new SystemTapView(), useColour, graphMode, config
								.getName());
				stapCmdPar.schedule();
				
			}

			monitor.worked(1);
			Process subProcess = execute(commandArray, getEnvironment(config),
					workDir, true);
			IProcess process = createNewProcess(launch, subProcess,
					commandArray[0]);

			// set the command line used
			process.setAttribute(IProcess.ATTR_CMDLINE,
					cmd);
			
			while (!process.isTerminated()) {
				Thread.sleep(100);
				if (monitor.isCanceled()) {
					Runtime run = Runtime.getRuntime();
					run.exec("kill stap"); //$NON-NLS-1$
					process.terminate();
					return;
				}
				
			}
			
			Thread.sleep(100);
			if (process.getExitValue() != 0) {
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages
						(Messages.getString("SystemTapLaunchConfigurationDelegate.1"), //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.2"),  //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.3") + //$NON-NLS-1$
						PluginConstants.NEW_LINE + 
						PluginConstants.NEW_LINE +
						Messages.getString("SystemTapLaunchConfigurationDelegate.4") + //$NON-NLS-1$
						PluginConstants.NEW_LINE +
						Messages.getString("SystemTapLaunchConfigurationDelegate.5") + //$NON-NLS-1$
						PluginConstants.NEW_LINE +
						Messages.getString("SystemTapLaunchConfigurationDelegate.6") +  //$NON-NLS-1$
						PluginConstants.NEW_LINE +
						PluginConstants.NEW_LINE + 
						Messages.getString("SystemTapLaunchConfigurationDelegate.0") + //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.7")); //$NON-NLS-1$
				mess.schedule();
				IDocument doc = Helper.getConsoleDocumentByName(config.getName());
//				getConsoleByName(config.getName()).clearConsole();
				File errorLog = new File(PluginConstants.DEFAULT_OUTPUT + "Error.log"); //$NON-NLS-1$
				
				if (!errorLog.exists() || errorLog.length() > PluginConstants.MAX_LOG_SIZE){
					errorLog.delete();
					errorLog.createNewFile();
				}
				
				Calendar cal = Calendar.getInstance(TimeZone.getDefault());
				int year = cal.get(Calendar.YEAR);
				int month =  cal.get(Calendar.MONTH);
				int day = cal.get(Calendar.DAY_OF_MONTH);
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int minute = cal.get(Calendar.MINUTE);
				int second = cal.get(Calendar.SECOND);
				
				Helper.appendToFile(errorLog.getAbsolutePath(), 
						PluginConstants.NEW_LINE+day+"/"+month+"/"+year
						+" - "+hour+":"+minute+":"+second+PluginConstants.NEW_LINE
						+doc.get());
				
				return;
			}
					

			if (stapCmdPar != null)
				stapCmdPar.setProcessFinished(true);

			if (graphMode) {
				stapCmdPar = new SystemTapCommandParser(
						Messages.getString("RunSystemTapAction.0"), //$NON-NLS-1$
						outputPath, new SystemTapView(), useColour, graphMode,
						config.getName());
				stapCmdPar.schedule();
			}

		} catch (IOException e) {
			abort("Could not start process", e, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			m.done();
		}

	}
	

	public String getCommand() {
		return cmdGenerator.getExecuteCommand();
	}

	public Process execute(String[] commandArray, String[] env, File wd,
			boolean usePty) throws IOException {
		Process process = null;
		try {
			if (wd == null) {
				process = ProcessFactory.getFactory().exec(commandArray, env);
			} else {
				if (PTY.isSupported() && usePty) {
					process = ProcessFactory.getFactory().exec(commandArray,
							env, wd, new PTY());
				} else {
					process = ProcessFactory.getFactory().exec(commandArray,
							env, wd);
				}
			}
		} catch (IOException e) {
			if (process != null) {
				process.destroy();
			}
			throw e;
		}

		return process;
	}
	
	

	protected IProcess createNewProcess(ILaunch launch, Process systemProcess,
			String programName) {
		return DebugPlugin.newProcess(launch, systemProcess,
				renderProcessLabel(programName));
	}

	public String getCommandLine(String[] args) {
		StringBuffer ret = new StringBuffer();
		for (String arg : args) {
			ret.append(arg + " "); //$NON-NLS-1$
		}
		return ret.toString().trim();
	}

}
