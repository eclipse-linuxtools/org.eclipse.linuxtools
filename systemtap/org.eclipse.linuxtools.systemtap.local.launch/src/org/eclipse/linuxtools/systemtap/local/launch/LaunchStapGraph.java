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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.systemtap.local.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.systemtap.local.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.local.core.SystemTapUIErrorMessages;
import org.eclipse.ui.IEditorPart;

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

	
	public void launch(IEditorPart ed, String mode) {
		resourceToSearchFor = ed.getTitle();
		searchForResource = true;
		
		//Note: This launch will eventually end up calling 
		//launch(IBinary bin, String mode) below 
		super.launch(ed, mode);
	}
	
	public void launch(IBinary bin, String mode) {
		super.Init();
		this.bin = bin;
		name = "SystemTapGraph";  //$NON-NLS-1$
		binName = getName(bin);
		partialScriptPath = PluginConstants.getPluginLocation()
				+ "parse_function_partial.stp";  //$NON-NLS-1$

		scriptPath = PluginConstants.DEFAULT_OUTPUT 
				+ "callgraphGen.stp";  //$NON-NLS-1$

		
		try {
			String scriptContents = generateScript(null);
			if (scriptContents == null || scriptContents.length() < 0)
				return;
			 
			config = createConfiguration(bin, name);
			binaryPath = bin.getResource().getLocation().toString();
			arguments = binaryPath;
			outputPath = PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH;
			
			
			ILaunchConfigurationWorkingCopy wc;
			
			wc = config.getWorkingCopy();
			wc.setAttribute(LaunchConfigurationConstants.GRAPHICS_MODE, true);
//			wc.setAttribute(LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,
//					"-DMAXACTION=1000 -DSTP_NO_OVERLOAD -DMAXMAPENTRIES=10000"); 
			wc.setAttribute(LaunchConfigurationConstants.GENERATED_SCRIPT, scriptContents);
			wc.doSave();
			

			finishLaunch(name, mode);

		} catch (IOException e) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					"LaunchShortcutScriptGen",  //$NON-NLS-1$
					Messages.getString("LaunchStapGraph.0"),   //$NON-NLS-1$
					Messages.getString("LaunchStapGraph.6"));  //$NON-NLS-1$
			mess.schedule();
			e.printStackTrace();
		} catch (CoreException e1) {
			e1.printStackTrace();
		} finally {
			resourceToSearchFor = ""; //$NON-NLS-1$
			searchForResource = false;
		}
		
		
	}
	
	/**
	 * Generates the call and return function probes for the specified function
	 * @param function
	 * @return
	 */
	private String generateProbe(String function) {
		String output = "probe process(@1).function(\"" +  //$NON-NLS-1$
				        function + "\").call{\n" +  //$NON-NLS-1$
				        "\tcallFunction(probefunc())\n" +  //$NON-NLS-1$
						"}\n" +  //$NON-NLS-1$
						"probe process(@1).function(\"" +  //$NON-NLS-1$
				        function + "\").return{\n" +  //$NON-NLS-1$
				        "\treturnFunction(probefunc())\n" +  //$NON-NLS-1$
						"}\n\n";  //$NON-NLS-1$
		return output;
	}
	
	/**
	 * Prompts the user for a list of functions to probe
	 * 
	 * @param bw
	 * @return
	 * @throws IOException
	 */
	private String writeFunctionListToScript(String resourceToSearchFor, ArrayList<String> exclusions) throws IOException {
		String toWrite = getFunctionsFromBinary(bin, resourceToSearchFor);
		
		if (toWrite == null || toWrite.length() < 1) {
			return null;
		}
		
		StringBuffer output = new StringBuffer();
		
		for (String func : toWrite.split(" ")) { //$NON-NLS-1$
			if (func.length() > 0) {
				if (exclusions != null) {
					if (!exclusions.contains(func))
						output.append(generateProbe(func));
				}
				else
					output.append(generateProbe(func));
						
			}
		}

		return output.toString();
	}

	/**
	 * Copies the contents of the specified partial script. You should call writeStapMarkers first
	 * if you want StapMarkers to function properly. 
	 * 
	 * @param bw
	 * @return
	 * @throws IOException
	 */
	private String writeFromPartialScript(String projectName) throws IOException {
		String toWrite = "";  //$NON-NLS-1$
		String temp = ""; //$NON-NLS-1$
		toWrite += "probe begin{\n" + //$NON-NLS-1$
					"printf(\"\\nPROBE_BEGIN\\n\")\n" +  //$NON-NLS-1$
					"serial=1\n" +  //$NON-NLS-1$
					"startTime = 0;\n" + //$NON-NLS-1$
					"printf(\"" + projectName + "\\n\")\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"}"; //$NON-NLS-1$
 		File partialScript = new File(partialScriptPath);
		BufferedReader scriptReader = new BufferedReader(new FileReader(
				partialScript));
		while ((temp = scriptReader.readLine()) != null) {
			toWrite += temp + "\n";  //$NON-NLS-1$
		}
		scriptReader.close();

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
		String toWrite = "global serial\n" + //$NON-NLS-1$
						 "global startTime\n " + //$NON-NLS-1$
						 "global finalTime\n"; //$NON-NLS-1$
		
		return toWrite;
	}
	
	@Override
	public String generateScript(ArrayList<String> exclusions) throws IOException {
		boolean exclude = false;
		if (exclusions != null)
			if (exclusions.size() > 0)
				exclude = true;
		
		String scriptContents = "";  //$NON-NLS-1$
		File scriptFile = new File(scriptPath);
		scriptFile.delete();
		scriptFile.createNewFile();

		scriptContents += writeGlobalVariables();
//		scriptContents += writeStapMarkers();
		String funcs = writeFunctionListToScript(resourceToSearchFor, exclusions);
		if (funcs == null || funcs.length() < 0)
			return null;
		scriptContents += funcs;
		scriptContents += writeFromPartialScript(bin.getCProject().getElementName());
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(scriptFile));
//		bw.write("probe begin { printf(\"HELLO\") }");
		bw.write(scriptContents);
		bw.close();
		return scriptContents;
	}
	
//	/**
//	 * Determines whether or not the user wants StapMarkers and inserts them. To
//	 * disable StapMarkers, simply stop calling this function. This should be called
//	 * before writeFromPartialScript.
//	 * 
//	 * @param bw
//	 * @return
//	 * @throws IOException
//	 */
//	private String writeStapMarkers() throws IOException {
//		String toWrite = Messages.getString("LaunchStapGraph.26"); //$NON-NLS-1$
//		if (MessageDialog.openQuestion(new Shell(),
//				Messages.getString("LaunchStapGraph.27"), //$NON-NLS-1$
//				Messages.getString("LaunchStapGraph.28") //$NON-NLS-1$
//						+ Messages.getString("LaunchStapGraph.29"))) { //$NON-NLS-1$
//			toWrite = Messages.getString("LaunchStapGraph.30") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.31") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.32") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.33") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.34") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.35") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.36") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.37") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.38") //$NON-NLS-1$
//					+ Messages.getString("LaunchStapGraph.39") + Messages.getString("LaunchStapGraph.40"); //$NON-NLS-1$ //$NON-NLS-2$
//			partialScriptPath = PluginConstants.getPluginLocation()
//					+ Messages.getString("LaunchStapGraph.41"); //$NON-NLS-1$
//		}
//		
//		return toWrite;
//	}
}
