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

package org.eclipse.linuxtools.internal.callgraph.launch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
import org.eclipse.linuxtools.internal.callgraph.core.DocWriter;
import org.eclipse.linuxtools.internal.callgraph.core.Helper;
import org.eclipse.linuxtools.internal.callgraph.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapCommandGenerator;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapErrorHandler;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapParser;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationDelegate;

/**
 * Delegate for Stap scripts. The Delegate generates part of the command string
 * and schedules a job to finish generation of the command and execute.
 *
 */
public class SystemTapLaunchConfigurationDelegate extends
		ProfileLaunchConfigurationDelegate {

	private static final String [] escapableChars = new String []  {"(", ")", " "}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
	private String stap = ""; //$NON-NLS-1$

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
				LaunchConfigurationConstants.DEFAULT_USE_COLOUR)) {
			useColour = true;
		}
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
				try (FileWriter fstream = new FileWriter(temporaryScript);
						BufferedWriter out = new BufferedWriter(fstream)) {
					out.write(config
							.getAttribute(
									LaunchConfigurationConstants.GENERATED_SCRIPT,
									LaunchConfigurationConstants.DEFAULT_GENERATED_SCRIPT));
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		stap = config.getAttribute(LaunchConfigurationConstants.COMMAND,
				PluginConstants.STAP_PATH);

		/**
		 * Generate partial command
		 */
		partialCommand = ConfigurationOptionsSetter.setOptions(config);

		outputPath = config.getAttribute(
				LaunchConfigurationConstants.OUTPUT_PATH,
				PluginConstants.getDefaultOutput());

		boolean fileExists = true;
		try {
			//Make sure the output file exists
			File tempFile = new File(outputPath);
			tempFile.delete();
			fileExists = tempFile.createNewFile();
		} catch (IOException e1) {
			fileExists = false;
		}

		// check for cancellation
		if ( !fileExists || monitor.isCanceled() ) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchConfigurationDelegate.0"),  //$NON-NLS-1$
					Messages.getString("SystemTapLaunchConfigurationDelegate.1"), Messages.getString("SystemTapLaunchConfigurationDelegate.2") + outputPath +  //$NON-NLS-1$ //$NON-NLS-2$
					Messages.getString("SystemTapLaunchConfigurationDelegate.3")); //$NON-NLS-1$
			mess.schedule();
			return;
		}

		finishLaunch(launch, config, m);
	}

	/**
	 * Returns the current SystemTap command, or returns an error message.
	 * @return
	 */
	public String getCommand() {
		if (cmd.length() > 0) {
			return cmd;
		}
		else {
			return Messages.getString("SystemTapLaunchConfigurationDelegate.NoCommand"); //$NON-NLS-1$
		}
	}

	private void finishLaunch(ILaunch launch, ILaunchConfiguration config,
			IProgressMonitor monitor) {

		try {
			// Check for cancellation
			if (monitor.isCanceled() || launch == null) {
				return;
			}
			monitor.worked(1);

			// set the default source locator if required
			setDefaultSourceLocator(launch, config);

			/*
			 * Fetch a parser
			 */
			String parserClass = config.getAttribute(LaunchConfigurationConstants.PARSER_CLASS,
					LaunchConfigurationConstants.DEFAULT_PARSER_CLASS);
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IConfigurationElement[] extensions = reg
					.getConfigurationElementsFor(PluginConstants.PARSER_RESOURCE,
							PluginConstants.PARSER_NAME,
							parserClass);
			if (extensions == null || extensions.length < 1) {
				SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser1"),  //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser1"), //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser2") + //$NON-NLS-1$
						Messages.getString("SystemTapLaunchConfigurationDelegate.InvalidParser3") + parserClass); //$NON-NLS-1$
				mess.schedule();
				return;
			}

			IConfigurationElement element = extensions[0];
			SystemTapParser parser =
				(SystemTapParser) element.createExecutableExtension(PluginConstants.ATTR_CLASS);

			//Set parser options
			parser.setViewID(config.getAttribute(LaunchConfigurationConstants.VIEW_CLASS,
					LaunchConfigurationConstants.VIEW_CLASS));
			parser.setSourcePath(outputPath);
			parser.setMonitor(SubMonitor.convert(monitor));
			parser.setDone(false);
			parser.setSecondaryID(config.getAttribute(LaunchConfigurationConstants.SECONDARY_VIEW_ID,
					LaunchConfigurationConstants.DEFAULT_SECONDARY_VIEW_ID));

			parser.setKillButtonEnabled(true);

			monitor.worked(1);

			/*
			 * Launch
			 */

			File workDir = getWorkingDirectory(config);
			if (workDir == null) {
				workDir = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			//Put command into a shell script
			String cmd = generateCommand();
			File script = File.createTempFile("org.eclipse.linuxtools.profiling.launch" + System.currentTimeMillis(), ".sh"); //$NON-NLS-1$ //$NON-NLS-2$
			String data = "#!/bin/sh\nexec " + cmd; //$NON-NLS-1$
			try (FileOutputStream out = new FileOutputStream(script)){
				out.write(data.getBytes());
			}

			String [] commandArray = new String [] {"sh", script.getAbsolutePath()}; //$NON-NLS-1$

			Process subProcess = execute(commandArray, getEnvironment(config),
					workDir, true);

			IProcess process = createNewProcess(launch, subProcess,commandArray[0]);
			// set the command line used
			process.setAttribute(IProcess.ATTR_CMDLINE,cmd);

			monitor.worked(1);

			StreamListener s = new StreamListener();
			process.getStreamsProxy().getErrorStreamMonitor().addListener(s);

			while (!process.isTerminated()) {
				Thread.sleep(100);
				if ((monitor != null && monitor.isCanceled()) || parser.isDone()) {
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
				//exit code for command not found
				if (process.getExitValue() == 127){
					SystemTapUIErrorMessages errorDialog = new SystemTapUIErrorMessages(
							Messages.getString("SystemTapLaunchConfigurationDelegate.CallGraphGenericError"), //$NON-NLS-1$
							Messages.getString("SystemTapLaunchConfigurationDelegate.CallGraphGenericError"), //$NON-NLS-1$
							Messages.getString("SystemTapLaunchConfigurationDelegate.stapNotFound")); //$NON-NLS-1$

					errorDialog.schedule();
				}else{
					SystemTapErrorHandler errorHandler = new SystemTapErrorHandler();

					//Prepare stap information
					errorHandler.appendToLog(config.getName() + Messages.getString("SystemTapLaunchConfigurationDelegate.stap_command") + cmd+ PluginConstants.NEW_LINE + PluginConstants.NEW_LINE);//$NON-NLS-1$

					//Handle error from TEMP_ERROR_OUTPUT
					errorHandler.handle(monitor, new FileReader(TEMP_ERROR_OUTPUT));
					if ((monitor != null && monitor.isCanceled())) {
						return;
					}

					errorHandler.finishHandling();
					if (errorHandler.isErrorRecognized()) {
						SystemTapUIErrorMessages errorDialog = new SystemTapUIErrorMessages(
								Messages.getString("SystemTapLaunchConfigurationDelegate.CallGraphGenericError"),  //$NON-NLS-1$
								Messages.getString("SystemTapLaunchConfigurationDelegate.CallGraphGenericError"),  //$NON-NLS-1$
								errorHandler.getErrorMessage());

						errorDialog.schedule();
					}
				}
				return;
			}

			if (element.getAttribute(PluginConstants.ATTR_REALTIME).equals(PluginConstants.VAL_TRUE)) {
				parser.setRealTime(true);
			}

			parser.schedule();
			monitor.worked(1);

			String message = generateErrorMessage(config.getName(), binaryArguments);

			DocWriter dw = new DocWriter(Messages.getString("SystemTapLaunchConfigurationDelegate.DocWriterName"),  //$NON-NLS-1$
					(Helper.getConsoleByName(config.getName())), message);
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
		} else {
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

	private static class StreamListener implements IStreamListener{
		private int counter;
		private BufferedWriter bw;

		public StreamListener() throws IOException {
			File file = new File(TEMP_ERROR_OUTPUT);
			file.delete();
			file.createNewFile();
			bw = Helper.setBufferedWriter(TEMP_ERROR_OUTPUT);
			counter = 0;
		}

		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			try {
				if (text.length() < 1) {
					return;
				}
				counter++;
				if (counter < PluginConstants.MAX_ERRORS) {
					bw.append(text);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void close() throws IOException {
			bw.close();
		}
	}

	public String generateCommand() {
		// Generate the command
		cmd = SystemTapCommandGenerator.generateCommand(escapeSpecialCharacters(scriptPath), escapeSpecialCharacters(binaryPath),
				partialCommand, needsBinary, needsArguments, escapeSpecialCharacters(arguments), binaryArguments, stap);
		cmd += " >& " + escapeSpecialCharacters(outputPath); //$NON-NLS-1$
		return cmd;
	}

	/**
	 * Escapes special characters in the target string
	 *
	 * @param script the script to be executed by the shell.
	 * @return the formatted string that will be executed.
	 */
	private String escapeSpecialCharacters(String str) {
		// Modify script to catch escapable characters.
		String res = str;
		for (int i = 0; i < escapableChars.length; i++) {
			res = res.replace(escapableChars[i], "\\" + escapableChars[i]); //$NON-NLS-1$
		}
		return res;
	}
}
