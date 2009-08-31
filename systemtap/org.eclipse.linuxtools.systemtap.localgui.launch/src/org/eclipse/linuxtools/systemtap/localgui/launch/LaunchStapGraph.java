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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.systemtap.localgui.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;
import org.eclipse.swt.widgets.Shell;

public class LaunchStapGraph extends SystemTapLaunchShortcut {
	/*
	 * The following protected parameters are provided by
	 * SystemTapLaunchShortcut:
	 * 
	 * Optional customization parameters: protected String name; protected
	 * String binaryPath; protected String arguments; protected String
	 * outputPath; protected String dirPath; protected String generatedScript;
	 * protected boolean needToGenerate; protected boolean overwrite;
	 * 
	 * Mandatory: protected String scriptPath; protected ILaunchConfiguration
	 * config;
	 */

	/**
	 * Launch method for a generated script that executes on a binary
	 * 
	 * MUST specify (String) scriptPath and call config =
	 * createConfiguration(bin)!
	 * 
	 * Noteworthy defaults: name defaults to "", but please set it (for
	 * usability) overwrite defaults to true - don't change it unless you really
	 * have to.
	 * 
	 * To create new launches: -Copy shortcut code in xml, changing class name
	 * and label accordingly -Create a class that extends
	 * SystemTapLaunchShortcut with a function launch(IBinary bin, String mode)
	 * -Call super.Init() -Set name (this is shortcut-specific) -If a binary is
	 * used, call binName = getName(bin) -Call createConfiguration(bin, name)
	 * 
	 * -Specify whichever of the optional parameters you need -Set scriptPath
	 * -Set an ILaunchConfiguration -Call finishLaunch or
	 * finishLaunchWithoutBinary
	 */
	
	private String partialScriptPath;
	private static final String DELIMITER = " ";  //$NON-NLS-1$

	public void launch(IBinary bin, String mode) {
		super.Init();		
		name = Messages.getString("LaunchStapGraph.0"); //$NON-NLS-1$
		binName = getName(bin);

		
		partialScriptPath = PluginConstants.getPluginLocation()
				+ "parse_function_partial.stp"; //$NON-NLS-1$

		scriptPath = PluginConstants.DEFAULT_OUTPUT 
				+ "callgraphGen.stp"; //$NON-NLS-1$

		try {
			String scriptContents = ""; //$NON-NLS-1$
			File scriptFile = new File(scriptPath);
			scriptFile.delete();
			scriptFile.createNewFile();


			scriptContents += writeGlobalVariables();
			scriptContents += writeStapMarkers();
			String funcs = writeFunctionListToScript();
			if (funcs == null)
				return;
			scriptContents += funcs;
			scriptContents += writeFromPartialScript();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(scriptFile));
			bw.write(scriptContents);
			bw.close();

			 
			config = createConfiguration(bin, name);
			binaryPath = bin.getResource().getLocation().toString();
			arguments = binaryPath;
			outputPath = PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH;
			
			
			ILaunchConfigurationWorkingCopy wc;
			
			wc = config.getWorkingCopy();
			wc.setAttribute(LaunchConfigurationConstants.GRAPHICS_MODE, true);
			wc.setAttribute(LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
					"-DMAXACTION=1000 -DSTP_NO_OVERLOAD -DMAXMAPENTRIES=10000"); //$NON-NLS-1$
			wc.setAttribute(LaunchConfigurationConstants.GENERATED_SCRIPT, scriptContents);
			wc.doSave();
			

			finishLaunch(name, mode);

		} catch (IOException e) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					Messages.getString("LaunchStapGraph.GenerationError"), //$NON-NLS-1$
					Messages.getString("LaunchStapGraph.GenerationErrorTitle"), //$NON-NLS-1$ 
					Messages.getString("LaunchStapGraph.GenerationErrorMessage")); //$NON-NLS-1$
			mess.schedule();
			e.printStackTrace();
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		

	}
	
	/**
	 * Generates the call and return function probes for the specified function
	 * @param function
	 * @return
	 */
	private String generateProbe(String function) {
		String output = "probe process(@1).function(\"" + //$NON-NLS-1$
				        function + "\").call{\n" + //$NON-NLS-1$
				        "\tcallFunction(probefunc())\n" + //$NON-NLS-1$
						"}\n" + //$NON-NLS-1$
						"probe process(@1).function(\"" + //$NON-NLS-1$
				        function + "\").return{\n" + //$NON-NLS-1$
				        "\treturnFunction(probefunc())\n" + //$NON-NLS-1$
						"}\n\n"; //$NON-NLS-1$
		return output;
	}
	
	/**
	 * Prompts the user for a list of functions to probe
	 * 
	 * @param bw
	 * @return
	 * @throws IOException
	 */
	private String writeFunctionListToScript() throws IOException {
		String toWrite = ""; //$NON-NLS-1$
		
		InputDialog id = new InputDialog(
				new Shell(),
				Messages.getString("LaunchStapGraph.FunctionsToProbeTitle"), //$NON-NLS-1$
				Messages.getString("LaunchStapGraph.FunctionsToProbeMessage") //$NON-NLS-1$
						+ Messages.getString("LaunchStapGraph.FunctionsToProbeMessage2"), //$NON-NLS-1$
				"*", null); //$NON-NLS-1$
		id.open();
		
		if (id.getReturnCode() == InputDialog.CANCEL)
			return null;
		String functions = id.getValue();

		if (functions == null) {
			failedToLaunch(Messages.getString("LaunchStapGraph.InvalidFunctionsReason")); //$NON-NLS-1$
			throw new IOException();
		} else if (functions.length() < 1) {
			failedToLaunch(Messages.getString("LaunchStapGraph.InvalidFunctionsReason")); //$NON-NLS-1$
			throw new IOException();
		}
		String[] funcs = functions.split(DELIMITER);
		for (String func : funcs) {
			toWrite += generateProbe(func);
		}

		return toWrite;
	}

	/**
	 * Copies the contents of the specified partial script. You should call writeStapMarkers first
	 * if you want StapMarkers to function properly. 
	 * 
	 * @param bw
	 * @return
	 * @throws IOException
	 */
	private String writeFromPartialScript() throws IOException {
		String toWrite = Messages.getString("LaunchStapGraph.23"); //$NON-NLS-1$
		String temp = Messages.getString("LaunchStapGraph.24"); //$NON-NLS-1$
		File partialScript = new File(partialScriptPath);
		BufferedReader scriptReader = new BufferedReader(new FileReader(
				partialScript));
		while ((temp = scriptReader.readLine()) != null) {
			toWrite += temp + Messages.getString("LaunchStapGraph.25"); //$NON-NLS-1$
		}
		scriptReader.close();

		return toWrite;
	}
	
	
	/**
	 * Determines whether or not the user wants StapMarkers and inserts them. To
	 * disable StapMarkers, simply stop calling this function. This should be called
	 * before writeFromPartialScript.
	 * 
	 * @param bw
	 * @return
	 * @throws IOException
	 */
	private String writeStapMarkers() throws IOException {
		String toWrite = Messages.getString("LaunchStapGraph.26"); //$NON-NLS-1$
		if (MessageDialog.openQuestion(new Shell(),
				Messages.getString("LaunchStapGraph.27"), //$NON-NLS-1$
				Messages.getString("LaunchStapGraph.28") //$NON-NLS-1$
						+ Messages.getString("LaunchStapGraph.29"))) { //$NON-NLS-1$
			toWrite = Messages.getString("LaunchStapGraph.30") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.31") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.32") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.33") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.34") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.35") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.36") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.37") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.38") //$NON-NLS-1$
					+ Messages.getString("LaunchStapGraph.39") + Messages.getString("LaunchStapGraph.40"); //$NON-NLS-1$ //$NON-NLS-2$
			partialScriptPath = PluginConstants.getPluginLocation()
					+ Messages.getString("LaunchStapGraph.41"); //$NON-NLS-1$
		}
		
		return toWrite;
	}
	
	/**
	 * Writes global variables for the StapGraph script to the BufferedWriter.
	 * Should be called first.
	 * 
	 * @param bw
	 * @return
	 * @throws IOException
	 */
	private String writeGlobalVariables() throws IOException {
		String toWrite = Messages.getString("LaunchStapGraph.42") //$NON-NLS-1$
			+ Messages.getString("LaunchStapGraph.43") //$NON-NLS-1$
			+ Messages.getString("LaunchStapGraph.44") //$NON-NLS-1$
			+ Messages.getString("LaunchStapGraph.45") //$NON-NLS-1$
			+ Messages.getString("LaunchStapGraph.46") //$NON-NLS-1$
			+ Messages.getString("LaunchStapGraph.47") //$NON-NLS-1$
			+ Messages.getString("LaunchStapGraph.48") + Messages.getString("LaunchStapGraph.49") //$NON-NLS-1$ //$NON-NLS-2$
			+ Messages.getString("LaunchStapGraph.50"); //$NON-NLS-1$
		
		return toWrite;
	}
	
	
}
