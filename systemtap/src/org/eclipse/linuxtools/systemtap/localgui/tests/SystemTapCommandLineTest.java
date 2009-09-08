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
package org.eclipse.linuxtools.systemtap.localgui.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.systemtap.localgui.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraphParser;
import org.eclipse.linuxtools.systemtap.localgui.launch.SystemTapLaunchShortcut;

public class SystemTapCommandLineTest extends TestCase {
	File tmpfile = new File("");
	public final String currentPath = tmpfile.getAbsolutePath();
	
	public String stapCommand;
	public final String scriptPath = currentPath+"/stapscript";
	public String binaryPath = "";
	public final String graphDataPath = currentPath+"/graph_data_output.graph";
	public final String parseFunctionPath = currentPath+"/parse_function_nomark.stp";
	
	
	//FOR TESTING THE GRAPH PARSING
	public void executeGraphTests(){
		initializeFiles();
		Runtime rt = Runtime.getRuntime();
		try {
			//EXECUTE THE COMMAND
			Process pr = null;
			pr = rt.exec("stap -c '"+binaryPath+ "' "+"-o "+graphDataPath+" "+ parseFunctionPath + " " + binaryPath);
			pr.waitFor();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		StapGraphParser grph = StapGraphParserTest.initializeGraph(graphDataPath);
		StapGraphParserTest.assertSanity(grph);
		StapGraphParserTest.assertTimes(grph);
		StapGraphParserTest.assertConnectedness(grph);
	}
	
	
	//FOR TESTING RAW STAP SCRIPT OUTPUT
	public String getCommandOutput(String command, boolean needsBinary){
		Runtime rt = Runtime.getRuntime();
		try {
			//CREATE/ACCESS A TEMPORARY FILE TO HOLD THE SCRIPT
			File file = new File(scriptPath);
			file.createNewFile();
			
			//WRITE THE COMMAND TO THE FILE
			BufferedWriter wbuff = new BufferedWriter(new FileWriter(file));
			wbuff.write(command);
			wbuff.close();
			
			//EXECUTE THE COMMAND
			Process pr = null;
			rt.exec("kill stap");
			if (needsBinary){
				pr = rt.exec("stap -c '"+binaryPath+ "' "+ scriptPath + " " + binaryPath);
			}else{
				pr = rt.exec("stap "+scriptPath);				
			}
			pr.waitFor();
			
			InputStream inpstr = pr.getInputStream();
			BufferedReader rbuff = new BufferedReader (new InputStreamReader(inpstr));
			String line = "";
			String text = "";
			
			//READ THE STANDARD OUTPUT OF COMMAND
			while ((line = rbuff.readLine()) != null){
				text += line;
			}
			
			rbuff.close();
			return text;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void testBasicStapScript() {
		final String expected = "probe_beginprobe_end";
		
		String command = "probe begin { " +
							"printf(\"probe_begin\")" +
							"exit()" +
						"}" +
						"probe end {" + 
							"printf(\"probe_end\")" +
						"}";
		String actual = getCommandOutput(command, false);
		
		assertEquals(expected, actual);
	}
	
	
	
	public void testFunctionProbes(){
		binaryPath = currentPath+"/basic";
		final String expected = "mainfoo";
		
		String command = "probe process(@1).function(\"*\"){ printf(\"%s\",probefunc()) }";
		String actual = getCommandOutput(command, true);
		
		assertEquals(expected, actual);
		
	}

	public void testBasicOperations(){
		final String expected = "01234";
		
		String command = "global map\n" +
		"global num\n" +
		"probe begin {" +
		"for (num=0; num<5; num++){"+
		"map[num]=num"+
		"}"+
		"exit()"+
		"}"+
		"probe end {" +
		"foreach (tmp in map){"+
		"printf(\"%d\",map[tmp])"+
		"}"+
		"}";
		String actual = getCommandOutput(command, false);
		
		assertEquals(expected, actual);
		
	}
	
	public void testCallGraphRunBasic(){
		binaryPath = currentPath+"/basic";
		executeGraphTests();
	}
	
	public void testCallGraphRunRecursive(){
		binaryPath = currentPath+"/catlan";
		executeGraphTests();
	}
	
	public void testManyFuncs(){
		binaryPath = currentPath+"/eag";
		executeGraphTests();
	}
	
	public void testFailure(){
		try {
			SystemTapLaunchShortcut shortcut = new SystemTapLaunchShortcut();
			ILaunchConfiguration config = shortcut.outsideGetLaunchConfigType().newInstance(null, "Temp Name");
			ILaunchConfigurationWorkingCopy wc = config.copy("Temp Name");
			
			wc.setAttribute(LaunchConfigurationConstants.BINARY_PATH,currentPath + "/basic");
			wc.setAttribute(LaunchConfigurationConstants.SCRIPT_PATH,scriptPath);
			wc.setAttribute(LaunchConfigurationConstants.ARGUMENTS,"-e'()'");
			wc.setAttribute(LaunchConfigurationConstants.OUTPUT_PATH,graphDataPath);
			config = wc.doSave();
			config.launch("profile", null);
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	
	public void initializeFiles(){
		File scriptFile = new File(scriptPath);
		File graphDataFile = new File(graphDataPath);
		
		try {
			scriptFile.createNewFile();
			graphDataFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
