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

package org.eclipse.linuxtools.callgraph.launch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.callgraph.core.DocWriter;
import org.eclipse.linuxtools.callgraph.core.Helper;
import org.eclipse.linuxtools.callgraph.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.callgraph.core.SystemTapCommandGenerator;
import org.eclipse.linuxtools.callgraph.core.SystemTapErrorHandler;
import org.eclipse.linuxtools.callgraph.core.SystemTapParser;
import org.eclipse.linuxtools.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.ui.console.TextConsole;


/**
 * Delegate for Stap scripts. The Delegate generates part of the command string
 * and schedules a job to finish generation of the command and execute.
 * 
 */
public class SystemTapLaunchConfigurationDelegate extends
		AbstractCLaunchDelegate {

	private static final String TEMP_ERROR_OUTPUT =
		PluginConstants.DEFAULT_OUTPUT + "stapTempError.error"; //$NON-NLS-1$
	private String cmd;
	private File temporaryScript = null;
	private String arguments = ""; //$NON-NLS-1$
	private String scriptPath = ""; //$NON-NLS-1$
	private String binaryPath = ""; //$NON-NLS-1$
	private String outputPath = ""; //$NON-NLS-1$
	private boolean needsBinary = false; // Set to false if we want to use SystemTap
	private boolean needsArguments = false;
	@SuppressWarnings("unused")
	private boolean useColour = false;
	private String binaryArguments = ""; //$NON-NLS-1$
	

	@Override
	protected String getPluginID() {
		return null;
	}

	private void initialize() {
		 temporaryScript = null;
		 arguments = ""; //$NON-NLS-1$
		 scriptPath = ""; //$NON-NLS-1$
		 binaryPath = ""; //$NON-NLS-1$
		 outputPath = ""; //$NON-NLS-1$
		 needsBinary = false; // Set to false if we want to use SystemTap
		 needsArguments = false;
		 useColour = false;
		 binaryArguments = ""; //$NON-NLS-1$
	}
	
	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor m) throws CoreException {
		
		if (m == null) {
			m = new NullProgressMonitor();
		}
		SubMonitor monitor = SubMonitor.convert(m,
				"SystemTap runtime monitor", 5); //$NON-NLS-1$
		initialize();

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
			
		
		
		/*
		 * Set variables
		 */
		if (config.getAttribute(LaunchConfigurationConstants.USE_COLOUR,
				LaunchConfigurationConstants.DEFAULT_USE_COLOUR))
			useColour = true; 
		if (!config.getAttribute(LaunchConfigurationConstants.ARGUMENTS,
				LaunchConfigurationConstants.DEFAULT_ARGUMENTS).equals(
				LaunchConfigurationConstants.DEFAULT_ARGUMENTS)) {
			arguments = config.getAttribute(
					LaunchConfigurationConstants.ARGUMENTS,
					LaunchConfigurationConstants.DEFAULT_ARGUMENTS);
			needsArguments = true;
		}
		if (!config.getAttribute(LaunchConfigurationConstants.BINARY_PATH,
				LaunchConfigurationConstants.DEFAULT_BINARY_PATH).equals(
				LaunchConfigurationConstants.DEFAULT_BINARY_PATH)) {
			binaryPath = config.getAttribute(
					LaunchConfigurationConstants.BINARY_PATH,
					LaunchConfigurationConstants.DEFAULT_BINARY_PATH);
			needsBinary = true;
		}
		if (!config.getAttribute(LaunchConfigurationConstants.BINARY_ARGUMENTS,
				LaunchConfigurationConstants.DEFAULT_BINARY_ARGUMENTS).equals(
				LaunchConfigurationConstants.DEFAULT_BINARY_ARGUMENTS)) {
			binaryArguments = config.getAttribute(
					LaunchConfigurationConstants.BINARY_ARGUMENTS,
					LaunchConfigurationConstants.DEFAULT_BINARY_ARGUMENTS);
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

		/**
		 * Generate partial command
		 */
		String partialCommand = ConfigurationOptionsSetter.setOptions(config);  

		outputPath = config.getAttribute(
				LaunchConfigurationConstants.OUTPUT_PATH,
				PluginConstants.DEFAULT_OUTPUT);
		partialCommand += "-o " + outputPath; //$NON-NLS-1$
		
		try {
			//Make sure the output file exists
			File tempFile = new File(outputPath);
			tempFile.delete();
			tempFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		finishLaunch(launch, config, partialCommand, m, true);
	}

	public String getCommand() {
		if (cmd.length() > 0)
			return cmd;
		else
			return Messages.getString("SystemTapLaunchConfigurationDelegate.0"); //$NON-NLS-1$
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
			return null;
		}
		return process;
	}
	
	

	protected IProcess createNewProcess(ILaunch launch, Process systemProcess,
			String programName) {
		return DebugPlugin.newProcess(launch, systemProcess,
				renderProcessLabel(programName));
	}

	public String getCommandLine(String[] args) {
		StringBuilder ret = new StringBuilder();
		for (String arg : args) {
			ret.append(arg + " "); //$NON-NLS-1$
		}
		return ret.toString().trim();
	}

	

	private void finishLaunch(ILaunch launch, ILaunchConfiguration config, String command,
			IProgressMonitor monitor, boolean retry) {
		String errorMessage = ""; //$NON-NLS-1$

		try {
			File workDir = getWorkingDirectory(config);
			if (workDir == null) {
				workDir = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Generate the command
			SystemTapCommandGenerator cmdGenerator = new SystemTapCommandGenerator();
			cmd = cmdGenerator.generateCommand(scriptPath, binaryPath,
					command, needsBinary, needsArguments, arguments, binaryArguments);


			// Prepare cmd for execution - we need a command array of strings,
			// no string can contain a space character. (One of the process'
			// requirements)
			String tmp[] = cmd.split(" "); //$NON-NLS-1$
			ArrayList<String> cmdLine = new ArrayList<String>();
			for (String str : tmp) {
				cmdLine.add(str);
			}
			String[] commandArray = (String[]) cmdLine.toArray(new String[cmdLine.size()]);
			
			// Check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			monitor.worked(1);
			
			if (launch == null) {
				return;
			}
			// Not sure if this line is necessary
			// set the default source locator if required
			setDefaultSourceLocator(launch, config);
			
			String parserClass = config.getAttribute(LaunchConfigurationConstants.PARSER_CLASS, 
					LaunchConfigurationConstants.DEFAULT_PARSER_CLASS);
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IConfigurationElement[] extensions = reg
					.getConfigurationElementsFor(PluginConstants.PARSER_RESOURCE, 
							PluginConstants.PARSER_NAME, 
							parserClass);
			
			
			if (extensions == null || extensions.length < 1) {
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser1"), 
						Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser2"), //$NON-NLS-1$ //$NON-NLS-2$
						Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser3") + //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser4") + parserClass); //$NON-NLS-1$
				mess.schedule();
				return;
			}
			
			IConfigurationElement element = extensions[0];

			SystemTapParser parser = 
				(SystemTapParser) element.createExecutableExtension(PluginConstants.ATTR_CLASS);
			parser.setViewID(config.getAttribute(LaunchConfigurationConstants.VIEW_CLASS,
					LaunchConfigurationConstants.VIEW_CLASS));
			parser.setSourcePath(outputPath);
			parser.setMonitor(SubMonitor.convert(monitor));
			parser.setDone(false);

			parser.setKillButtonEnabled(true);
						
			if (element.getAttribute(PluginConstants.ATTR_REALTIME).equals(PluginConstants.VAL_TRUE)) {
				parser.setRealTime(true);
				parser.schedule();
			}

			monitor.worked(1);

			
			Process subProcess = execute(commandArray, getEnvironment(config),
					workDir, true);
			System.out.println(cmd);
			
			if (subProcess == null){
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchConfigurationDelegate.NullProcessErrorName"),
				Messages.getString("SystemTapLaunchConfigurationDelegate.NullProcessErrorTitle"),  //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("SystemTapLaunchConfigurationDelegate.NullProcessErrorMessage1")); //$NON-NLS-1$
				mess.schedule();
				return;
			}
			
			IProcess process = createNewProcess(launch, subProcess,commandArray[0]);
			// set the command line used
			process.setAttribute(IProcess.ATTR_CMDLINE,cmd);
			monitor.worked(1);
			((TextConsole)Helper.getConsoleByName(config.getName())).activate();
			
			StreamListener s = new StreamListener();
			process.getStreamsProxy().getErrorStreamMonitor().addListener(s);

			
			while (!process.isTerminated()) {
				Thread.sleep(100);
				if ((monitor != null && monitor.isCanceled()) || parser.isJobCancelled()) {
					if (!parser.isJobCancelled()) {
						parser.cancelJob();
						Runtime run = Runtime.getRuntime();
						run.exec("kill stap"); //$NON-NLS-1$
					}
					process.terminate();
					return;
				}
			}
			Thread.sleep(100);
			s.close();
			parser.setKillButtonEnabled(false);
			

			if (process.getExitValue() != 0) {
				//SystemTap terminated with errors, parse console to figure out which error 
				IDocument doc = Helper.getConsoleDocumentByName(config.getName());
				//Sometimes the console has not been printed to yet, wait for a little while longer
				if (doc.get().length() < 1)
					Thread.sleep(300);
				SystemTapErrorHandler errorHandler = new SystemTapErrorHandler();
				errorHandler.handle(monitor, config.getName() 
						+ Messages.getString("SystemTapLaunchConfigurationDelegate.stap_command")  //$NON-NLS-1$
						+ cmd
						+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);
				errorMessage = errorHandler.handle(monitor, new FileReader(TEMP_ERROR_OUTPUT)); //$NON-NLS-1$
				if ((monitor != null && monitor.isCanceled()) || parser.isJobCancelled()) {
					monitor.setCanceled(true);
					parser.cancelJob();
					return;
				}
				
				
				if (errorHandler.hasMismatchedProbePoints() && retry) {
					
					SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch1"), //$NON-NLS-1$
							Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch2"),  //$NON-NLS-1$
							Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch3")); //$NON-NLS-1$
					mess.schedule();
					
					if (!errorHandler.finishHandling(monitor, s.getNumberOfErrors())) {
						//Check if we should attempt a relaunch or not
						return;
					}
					
					if ((monitor != null && monitor.isCanceled()) || parser.isJobCancelled()) {
						monitor.setCanceled(true);
						parser.cancelJob();
						return;
					}
					finishLaunch(launch, config, command, monitor, false);
					return;
				}
				
				errorHandler.finishHandling(monitor, s.getNumberOfErrors());
				return;
			}
			
			if (! element.getAttribute(PluginConstants.ATTR_REALTIME).equals(PluginConstants.VAL_TRUE)) { //$NON-NLS-1$ //$NON-NLS-2$
				parser.schedule();
			} else {
				//Parser already scheduled, but double-check
				if (parser != null)
					parser.setDone(true);
			}
						
			
			monitor.worked(1);
			
			errorMessage = generateErrorMessage(config.getName(), command) + errorMessage;
			
			DocWriter dw = new DocWriter(Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterName"),  //$NON-NLS-1$
					((TextConsole)Helper.getConsoleByName(config.getName())), errorMessage);
			dw.schedule();

			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			monitor.done();
			
		}
	}
	
	
	private String generateErrorMessage(String configName, String binaryCommand) {
		String output = ""; //$NON-NLS-1$
		
		if (binaryCommand == null || binaryCommand.length() < 0) {
			output = PluginConstants.NEW_LINE +
						PluginConstants.NEW_LINE + "-------------" + //$NON-NLS-1$
						PluginConstants.NEW_LINE + 
						Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch10") //$NON-NLS-1$
						+ configName + PluginConstants.NEW_LINE +
						Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch8") + //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch9") + //$NON-NLS-1$
						"configuration in Profile As --> Profile Configurations." + //$NON-NLS-1$
						PluginConstants.NEW_LINE + PluginConstants.NEW_LINE;
		}
		else {
			output = PluginConstants.NEW_LINE 
					+ PluginConstants.NEW_LINE +"-------------" //$NON-NLS-1$
					+ PluginConstants.NEW_LINE 
					+ Messages.getString("SystemTapLaunchConfigurationDelegate.EndMessage1")  //$NON-NLS-1$
					+ configName + PluginConstants.NEW_LINE +
					Messages.getString("SystemTapLaunchConfigurationDelegate.EndMessage2") //$NON-NLS-1$
					+ binaryCommand + PluginConstants.NEW_LINE +
					Messages.getString("SystemTapLaunchConfigurationDelegate.EndMessage3") +  //$NON-NLS-1$
					Messages.getString("SystemTapLaunchConfigurationDelegate.EndMessage4") +  //$NON-NLS-1$
					Messages.getString("SystemTapLaunchConfigurationDelegate.EndMessage5") +  //$NON-NLS-1$
					PluginConstants.NEW_LINE + PluginConstants.NEW_LINE;
		}
			
		return output;
	}
	
		
	private class StreamListener implements IStreamListener{
		private Helper h;
		private int counter;
		public StreamListener() throws IOException {
			File file = new File(TEMP_ERROR_OUTPUT);
			file.delete();
			file.createNewFile();
			h = new Helper();
			counter = 0;
			h.setBufferedWriter(TEMP_ERROR_OUTPUT); //$NON-NLS-1$
		}
		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			try {
				counter++;
				if (counter < PluginConstants.MAX_ERRORS)
					h.appendToExistingFile(text);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		public void close() throws IOException {
			h.closeBufferedWriter();
		}
		
		public int getNumberOfErrors() {
			return counter;
		}
	}
}
