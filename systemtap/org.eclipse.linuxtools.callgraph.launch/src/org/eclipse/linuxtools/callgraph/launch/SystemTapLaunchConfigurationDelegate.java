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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
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
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;
import org.eclipse.ui.console.TextConsole;


/**
 * Delegate for Stap scripts. The Delegate generates part of the command string
 * and schedules a job to finish generation of the command and execute.
 * 
 */
public class SystemTapLaunchConfigurationDelegate extends
		ProfileLaunchConfigurationDelegate {

	private static final String TEMP_ERROR_OUTPUT =
		PluginConstants.getDefaultOutput() + "stapTempError.error"; //$NON-NLS-1$
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
	private String partialCommand = ""; //$NON-NLS-1$
	

	@Override
	protected String getPluginID() {
		return null;
	}

	
	/**
	 * Sets strings to blank, booleans to false and everything else to null
	 */
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
		partialCommand = ConfigurationOptionsSetter.setOptions(config);  

		outputPath = config.getAttribute(
				LaunchConfigurationConstants.OUTPUT_PATH,
				PluginConstants.getDefaultOutput());
		partialCommand += "-o " + outputPath; //$NON-NLS-1$

		// check for cancellation
		if ( !testOutput(outputPath) || monitor.isCanceled() ) {
			return;
		}

		finishLaunch(launch, config, partialCommand, m, true);
	}

	
	/**
	 * Returns the current SystemTap command, or returns an error message.
	 * @return
	 */
	public String getCommand() {
		if (cmd.length() > 0)
			return cmd;
		else
			return Messages.getString("SystemTapLaunchConfigurationDelegate.0"); //$NON-NLS-1$
	}
	

	private void finishLaunch(ILaunch launch, ILaunchConfiguration config, String command,
			IProgressMonitor monitor, boolean retry) {
		String errorMessage = ""; //$NON-NLS-1$

		try {

			// Generate the command
			cmd = SystemTapCommandGenerator.generateCommand(scriptPath, binaryPath,
					command, needsBinary, needsArguments, arguments, binaryArguments);
			
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
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser1"),  //$NON-NLS-1$
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
			
			IProcess process = createProcess(config, launch);
			
			monitor.worked(1);
			
			StreamListener s = new StreamListener();
			process.getStreamsProxy().getErrorStreamMonitor().addListener(s);

			
			while (!process.isTerminated()) {
				Thread.sleep(100);
				if ((monitor != null && monitor.isCanceled()) || parser.isJobCancelled()) {
					parser.cancelJob();
					process.terminate();
					return;
				}
			}
			Thread.sleep(100);
			s.close();
			parser.setKillButtonEnabled(false);
			

			if (process.getExitValue() != 0) {
				parser.cancelJob();
				//SystemTap terminated with errors, parse console to figure out which error 
				IDocument doc = Helper.getConsoleDocumentByName(config.getName());
				//Sometimes the console has not been printed to yet, wait for a little while longer
				if (doc.get().length() < 1)
					Thread.sleep(300);
				SystemTapErrorHandler errorHandler = new SystemTapErrorHandler();
				
				
				//Prepare stap information
				errorHandler.appendToLog(config.getName() + Messages.getString("SystemTapLaunchConfigurationDelegate.stap_command") + cmd+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);//$NON-NLS-1$
				
				//Handle error from TEMP_ERROR_OUTPUT
				errorMessage = errorHandler.handle(monitor, new FileReader(TEMP_ERROR_OUTPUT)); //$NON-NLS-1$
				if ((monitor != null && monitor.isCanceled()))
					return;
				
				
				//If we are meant to retry, and the conditions for retry are met
				//Currently conditions only met if there are mismatched probe points present
				if (errorHandler.hasMismatchedProbePoints() && retry) {
					
					SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch1"), //$NON-NLS-1$
							Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch2"),  //$NON-NLS-1$
							Messages.getString("SystemTapLaunchConfigurationDelegate.Relaunch3")); //$NON-NLS-1$
					mess.schedule();
					
					//If finishHandling determines that errors are not fixable, return
					if (!errorHandler.finishHandling(monitor, scriptPath))
						return;
					
					
					//Abort job
					if ((monitor != null && monitor.isCanceled()) || parser.isJobCancelled()) {
						monitor.setCanceled(true);
						parser.cancelJob();
						return;
					}
					finishLaunch(launch, config, command, monitor, false);
					return;
				}
				
				errorHandler.finishHandling(monitor, scriptPath);
				return;
			}
			
			if (! element.getAttribute(PluginConstants.ATTR_REALTIME).equals(PluginConstants.VAL_TRUE)) { //$NON-NLS-1$ //$NON-NLS-2$
				parser.schedule();
			} else {
				//Parser already scheduled, but double-check
				if (parser != null)
					parser.cancelJob();
			}
						
			
			monitor.worked(1);
			
			errorMessage = generateErrorMessage(config.getName(), binaryArguments) + errorMessage;
			
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
			h.setBufferedWriter(TEMP_ERROR_OUTPUT); //$NON-NLS-1$
			counter = 0;
		}
		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			try {
				if (text.length() < 1) return;
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

	}


	@Override
	protected String generateCommand(ILaunchConfiguration config) {
		if (cmd != null && cmd.length() > 0)
			return cmd;

		// Generate the command
		cmd = SystemTapCommandGenerator.generateCommand(scriptPath, binaryPath,
				partialCommand, needsBinary, needsArguments, arguments, binaryArguments);
		return cmd;
	}
}
