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
package org.eclipse.linuxtools.systemtap.local.callgraph.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.systemtap.local.callgraph.StapGraphParser;

public class StapGraphParserTest extends TestCase {
	
	//RENDER THE GRAPH
	public  static StapGraphParser initializeGraph(String filePath){
		StapGraphParser grph = new StapGraphParser("basic test", filePath);
		grph.testRun(new NullProgressMonitor());
		return grph;
	}
	
	public static void assertSanity(StapGraphParser grph){
		//SAME NUMBER OF NODES ENTRIES
		assertEquals(grph.serialMap.size(),grph.timeMap.size());
		assertEquals(grph.serialMap.size(),grph.outNeighbours.size());
		//ALL UNIQUE FUNCTIONS HAVE A TIME
		//ALL FUNCTIONS HAVE A CUMULATIVE TIME
		for (int val : grph.serialMap.keySet()){
			String fname = grph.serialMap.get(val);
			assertTrue(grph.timeMap.get(val) != null);
			assertTrue(grph.cumulativeTimeMap.get(fname) != null);
		}
	}
	
	
	public static void assertTimes(StapGraphParser grph){
		//NO FUNCTION HAS TIME/CUMULATIVE TIME LARGER THAN TOTAL
		for (int val : grph.serialMap.keySet()){
			String fname = grph.serialMap.get(val);
			assertTrue(grph.totalTime >= grph.timeMap.get(val));
			assertTrue(grph.totalTime >= grph.cumulativeTimeMap.get(fname));
		}
	}
	
	
	public static void assertConnectedness (StapGraphParser grph){
		boolean hasParent;
		//ALL NODES MUST HAVE A PARENT EXCEPT THE ROOT
		for (int key : grph.serialMap.keySet()){
			hasParent = false;
			for (ArrayList<Integer> list : grph.outNeighbours.values()){
				if (list.contains(key)){
					hasParent = true;
					break;
				}
			}
			
			if (!hasParent){
				for (int other : grph.serialMap.keySet()){
					if (key > other){
						fail(key + " " + grph.serialMap.get(key) + " had no parent");						
					}
				}
			}
		}
		
	}
	
	
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
