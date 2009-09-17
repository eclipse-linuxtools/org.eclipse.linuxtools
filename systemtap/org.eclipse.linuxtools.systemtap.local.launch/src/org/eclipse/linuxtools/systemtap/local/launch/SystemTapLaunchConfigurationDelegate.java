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

package org.eclipse.linuxtools.systemtap.local.launch;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.systemtap.local.callgraph.CallgraphView;
import org.eclipse.linuxtools.systemtap.local.callgraph.SystemTapCommandParser;
import org.eclipse.linuxtools.systemtap.local.core.Helper;
import org.eclipse.linuxtools.systemtap.local.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.systemtap.local.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.local.core.SystemTapCommandGenerator;
import org.eclipse.linuxtools.systemtap.local.core.SystemTapErrorHandler;
import org.eclipse.linuxtools.systemtap.local.core.SystemTapUIErrorMessages;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.progress.UIJob;


/**
 * Delegate for Stap scripts. The Delegate generates part of the command string
 * and schedules a job to finish generation of the command and execute.
 * 
 */
public class SystemTapLaunchConfigurationDelegate extends
		AbstractCLaunchDelegate {

	private String cmd;
	private File temporaryScript = null;
	private String arguments = ""; //$NON-NLS-1$
	private String scriptPath = ""; //$NON-NLS-1$
	private String binaryPath = ""; //$NON-NLS-1$
	private String outputPath = ""; //$NON-NLS-1$
	private boolean needsBinary = false; // Set to false if we want to use SystemTap
	private boolean needsArguments = false;
	private boolean useColour = false;
	private String binaryArguments = ""; //$NON-NLS-1$
	

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
				"SystemTap runtime monitor", 5); //$NON-NLS-1$

		//System.out.println("SystemTapLaunchConfigurationDelegate: launch"); //$NON-NLS-1$

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}


			
		String command = ConfigurationOptionsSetter.setOptions(config);  
		
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

		outputPath = config.getAttribute(
				LaunchConfigurationConstants.OUTPUT_PATH,
				PluginConstants.DEFAULT_OUTPUT);
		command += "-o " + outputPath; //$NON-NLS-1$
		try {
			File tempFile = new File(outputPath);
			tempFile.createNewFile();
			//Make sure the output file exists
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		finishLaunch(launch, config, command, m, true);
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
		StringBuffer ret = new StringBuffer();
		for (String arg : args) {
			ret.append(arg + " "); //$NON-NLS-1$
		}
		return ret.toString().trim();
	}

	
	private class DocWriter extends UIJob {
		private TextConsole console;
		private String configName;
		private String binaryCommand;

		public DocWriter(String name, TextConsole console, String cName,
				String binaryCommand) {
			super(name);
			this.console = console;
			this.configName = cName;
			this.binaryCommand = binaryCommand;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IDocument doc = console.getDocument();
			
			if (binaryCommand.length() > 0)
				try {
					doc.replace(doc.getLength(), 0, 
						PluginConstants.NEW_LINE 
						+ PluginConstants.NEW_LINE +"-------------" //$NON-NLS-1$
						+ PluginConstants.NEW_LINE 
						+ Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterMessage1")//$NON-NLS-1$ 
						+ configName + PluginConstants.NEW_LINE +
						Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterMessage2")//$NON-NLS-1$ 
						+ binaryCommand + PluginConstants.NEW_LINE +
						Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterMessage3") + //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterMessage4") + //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterMessage5")//$NON-NLS-1$
						);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			else
				try {
					doc.replace(doc.getLength(), 0,
						PluginConstants.NEW_LINE +
						PluginConstants.NEW_LINE + "-------------" + //$NON-NLS-1$
						PluginConstants.NEW_LINE + 
						Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterMessage1")//$NON-NLS-1$ 
						+ configName + PluginConstants.NEW_LINE +
						Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterNoBinarySpecified") + //$NON-NLS-1$
						PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			
			return Status.OK_STATUS;
		}
		
	}
	
	private void finishLaunch(ILaunch launch, ILaunchConfiguration config, String command,
			IProgressMonitor monitor, boolean retry) {
		try {
			File workDir = getWorkingDirectory(config);
			if (workDir == null) {
				workDir = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}



			// Generate the command
			SystemTapCommandGenerator cmdGenerator = new SystemTapCommandGenerator();
			cmd = cmdGenerator.generateCommand(scriptPath, binaryPath,
					command, needsBinary, needsArguments, arguments, binaryArguments);


//			MP.println(cmd);
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
			
			boolean graphMode = config.getAttribute(
					LaunchConfigurationConstants.GRAPHICS_MODE,
					LaunchConfigurationConstants.DEFAULT_GRAPHICS_MODE);
			// Prepare a parser object - parser will read and update from the
			// output file continuously
			SystemTapCommandParser stapCmdPar = null;
			if (!graphMode) {
				stapCmdPar = new SystemTapCommandParser(Messages
						.getString("RunSystemTapAction.0"), outputPath, //$NON-NLS-1$
						new CallgraphView(), useColour, graphMode, config
								.getName());
				stapCmdPar.schedule();
			}

			monitor.worked(1);

			
			Process subProcess = execute(commandArray, getEnvironment(config),
					workDir, true);
			
			if (subProcess == null){
				//TODO: FIgure out what the console error message is so we can catch it in errorlog
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchConfigurationDelegate.NullProcessErrorName"), Messages.getString("SystemTapLaunchConfigurationDelegate.NullProcessErrorTitle"),  //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("SystemTapLaunchConfigurationDelegate.NullProcessErrorMessage1") + //$NON-NLS-1$
				Messages.getString("SystemTapLaunchConfigurationDelegate.NullProcessErrorMessage2")); //$NON-NLS-1$
				mess.schedule();
				return;
			}
			
			IProcess process = createNewProcess(launch, subProcess,
					commandArray[0]);
			// set the command line used
			process.setAttribute(IProcess.ATTR_CMDLINE,
					cmd);
			monitor.worked(1);
			((TextConsole)Helper.getConsoleByName(config.getName())).activate();
			
			StreamListener s = new StreamListener();
			process.getStreamsProxy().getErrorStreamMonitor().addListener(s);
			


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
			s.close();
			//SIGNAL THE PROCESS TO FINISH
			if (stapCmdPar != null)
				stapCmdPar.setProcessFinished(true);

			if (process.getExitValue() != 0) {
				//SystemTap terminated with errors, parse console to figure out which error 
				IDocument doc = Helper.getConsoleDocumentByName(config.getName());
				//Sometimes the console has not been printed to yet, wait for a little while longer
				if (doc.get().length() < 1)
					Thread.sleep(300);
				SystemTapErrorHandler errorHandler = new SystemTapErrorHandler();
				errorHandler.handle(monitor, config.getName() + Messages.getString("SystemTapLaunchConfigurationDelegate.stap_command")  //$NON-NLS-1$
						+ PluginConstants.NEW_LINE + cmd
						+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);
				errorHandler.handle(monitor, new FileReader(outputPath + "ERROR"));
				if (monitor != null && monitor.isCanceled())
					return;
				
				
				if (errorHandler.hasMismatchedProbePoints() && retry) {
					errorHandler.finishHandling(monitor, s.getNumberOfErrors());
					if (monitor != null && monitor.isCanceled())
						return;
					finishLaunch(launch, config, command, monitor, false);
					return;
				}
				errorHandler.finishHandling(monitor, s.getNumberOfErrors());
					
				return;
			}
					

			if (graphMode) {
				stapCmdPar = new SystemTapCommandParser(
						Messages.getString("RunSystemTapAction.0"), //$NON-NLS-1$
						outputPath, new CallgraphView(), useColour, graphMode,
						config.getName());
					stapCmdPar.schedule();
			}
			
			monitor.worked(1);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			DocWriter dw = new DocWriter(Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterName"),  //$NON-NLS-1$
					((TextConsole)Helper.getConsoleByName(config.getName())), config.getName(),
					binaryArguments);
			dw.schedule();
			monitor.done();
		}
	}
		
	private class StreamListener implements IStreamListener{
		private Helper h;
		private int counter;
		public StreamListener() throws IOException {
			h = new Helper();
			counter = 0;
			h.setBufferedWriter(outputPath + "ERROR");
		}
		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			try {
				counter++;
				if (counter < 300)
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
