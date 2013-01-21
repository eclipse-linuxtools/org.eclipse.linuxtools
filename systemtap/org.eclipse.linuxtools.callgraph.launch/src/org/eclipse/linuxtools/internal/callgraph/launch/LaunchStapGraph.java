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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapUIErrorMessages;
import org.eclipse.ui.IEditorPart;

public class LaunchStapGraph extends SystemTapLaunchShortcut {


	//TODO: Do not let this class persist, or otherwise change it so persistence doesn't matter.
	private String partialScriptPath;
	private String funcs;
	private ArrayList<String> exclusions;
	private String projectName;
	protected static final String ATTR_PARSER = "org.eclipse.linuxtools.callgraph.graphparser"; //$NON-NLS-1$
	protected static final String ATTR_VIEWER = "org.eclipse.linuxtools.callgraph.callgraphview";  //$NON-NLS-1$

	public LaunchStapGraph() {
		funcs = null;
		exclusions = new ArrayList<String>();
		projectName = null;
	}

	@Override
	public void launch(IEditorPart ed, String mode) {
		resourceToSearchFor = ed.getTitle();
		searchForResource = true;

		//Note: This launch will eventually end up calling
		//launch(IBinary bin, String mode) below
		super.launch(ed, mode);
	}

	@Override
	public void launch(IBinary bin, String mode) {
		launch(bin, mode, null);
	}

	public void launch(IBinary bin, String mode, ILaunchConfigurationWorkingCopy wc) {
		super.initialize();
		this.bin = bin;
		name = "SystemTapGraph";  //$NON-NLS-1$
		binName = getName(bin);
		partialScriptPath = PluginConstants.getPluginLocation()
				+ "parse_function_partial.stp";  //$NON-NLS-1$

		viewID = "org.eclipse.linuxtools.callgraph.callgraphview"; //$NON-NLS-1$



		projectName = bin.getCProject().getElementName();

		try {
			if (wc == null)
				wc = createConfiguration(bin, name);
			binaryPath = bin.getResource().getLocation().toString();
			binaryPath = escapeSpecialCharacters(binaryPath);
			arguments = binaryPath;
			outputPath = PluginConstants.getDefaultIOPath();

			if (writeFunctionListToScript(resourceToSearchFor) == null)
				return;
			if (funcs == null || funcs.length() < 0)
				return;

			needToGenerate = true;
			finishLaunch(name, mode, wc);

		} catch (IOException e) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(
					"LaunchShortcutScriptGen",  //$NON-NLS-1$
					Messages.getString("LaunchStapGraph.ScriptGenErr"),   //$NON-NLS-1$
					Messages.getString("LaunchStapGraph.ScriptGenErrMsg"));  //$NON-NLS-1$
			mess.schedule();
			e.printStackTrace();
		} finally {
			resourceToSearchFor = ""; //$NON-NLS-1$
			searchForResource = false;
		}


	}

	public void setFuncs(String val) {
		funcs = val;
	}

	/**
	 * Generates the call and return function probes for the specified function
	 * @param function
	 * @return
	 */
	private String generateProbe(String function) {
		String output = "probe process(@1).function(\"" + function + "\").call ? {	if ( ! isinstr(probefunc(), \"___STAP_MARKER___\")) { callFunction(probefunc(),tid()) } 	}	probe process(@1).function(\"" + function + "\").return ? {		if ( ! isinstr(probefunc(), \"___STAP_MARKER___\")) returnFunction(probefunc(),tid())	else { printf(\"?%d,,%s\\n\", tid(), user_string(strtol(tokenize($$return, \"return=\"),16)))}}\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return output;
	}

	/**
	 * Prompts the user for a list of functions to probe
	 *
	 * @param bw
	 * @return
	 */
	public String writeFunctionListToScript(String resourceToSearchFor) {
		String toWrite = getFunctionsFromBinary(bin, resourceToSearchFor);

		if (toWrite == null || toWrite.length() < 1) {
			return null;
		}

		StringBuffer output = new StringBuffer();

		for (String func : toWrite.split(" ")) { //$NON-NLS-1$
			if (func.length() > 0
					&& (exclusions == null || exclusions.size() < 1 || exclusions
							.contains(func))) {
				output.append(generateProbe(func));
			}
		}

		funcs = output.toString();
		return funcs;
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
		toWrite += "\nprobe begin{\n" + //$NON-NLS-1$
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


	public void setExclusions(ArrayList<String> e) {
		exclusions = e;
	}

	@Override
	public String generateScript() throws IOException {

		String scriptContents = "";  //$NON-NLS-1$

		scriptContents += funcs;

		scriptContents += writeFromPartialScript(projectName);

		return scriptContents;
	}

	public void setPartialScriptPath(String val) {
		partialScriptPath = val;
	}

	@Override
	public String setScriptPath() {
		scriptPath = PluginConstants.getDefaultOutput()
				+ "callgraphGen.stp";  //$NON-NLS-1$
		return scriptPath;
	}

	@Override
	public String setParserID() {
		parserID = ATTR_PARSER;
		return parserID;
	}

	@Override
	public String setViewID() {
		return ATTR_VIEWER;
	}
}
