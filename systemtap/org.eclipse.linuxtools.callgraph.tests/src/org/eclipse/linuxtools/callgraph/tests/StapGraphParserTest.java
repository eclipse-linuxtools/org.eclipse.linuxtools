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
package org.eclipse.linuxtools.callgraph.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.linuxtools.internal.callgraph.StapGraphParser;
import org.junit.Test;

public class StapGraphParserTest {

	//RENDER THE GRAPH
	public  static StapGraphParser initializeGraph(String filePath){
		StapGraphParser grph = new StapGraphParser();
		grph.setSourcePath(filePath);
		grph.nonRealTimeParsing();
		return grph;
	}

	public static void assertSanity(StapGraphParser grph){
		/*if (grph.serialMap.size() == 0 || grph.timeMap.size() == 0
				|| grph.outNeighbours.size() == 0 || grph.countMap.size() == 0
				|| grph.aggregateTimeMap.size() == 0){
			fail("Parsing Error : One or more data structures were empty.");
		}*/

		//SAME NUMBER OF NODES ENTRIES
		assertEquals(grph.serialMap.size(),grph.timeMap.size());
		int nsize = 0;
		for (int key : grph.neighbourMaps.keySet())
			if (grph.neighbourMaps.get(key)!= null)
				nsize+=grph.neighbourMaps.get(key).size();
		assertEquals(grph.serialMap.size(),nsize);
		//ALL UNIQUE FUNCTIONS HAVE A TIME
		//ALL FUNCTIONS HAVE A CUMULATIVE TIME
		for (int val : grph.serialMap.keySet()){
			String fname = grph.serialMap.get(val);
			assertNotNull(grph.timeMap.get(val));
			assertNotNull(grph.aggregateTimeMap.get(fname));
		}
	}


	public static void assertTimes(StapGraphParser grph){
		//NO FUNCTION HAS TIME/CUMULATIVE TIME LARGER THAN TOTAL
		for (int val : grph.serialMap.keySet()){
			String fname = grph.serialMap.get(val);
			assertTrue(grph.totalTime >= grph.timeMap.get(val));
			assertTrue(grph.totalTime >= grph.aggregateTimeMap.get(fname));
		}
	}


	public static void assertConnectedness (StapGraphParser grph){
		boolean hasParent;
		//ALL NODES MUST HAVE A PARENT EXCEPT THE ROOT
		for (int key : grph.serialMap.keySet()){
			hasParent = false;
			for (int k:grph.neighbourMaps.keySet()) {
				HashMap<Integer, ArrayList<Integer>> outNeighbours = grph.neighbourMaps.get(k);
				if (outNeighbours != null && outNeighbours.size() > 0)
				for (ArrayList<Integer> list : outNeighbours.values()){
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

	}


	public final String currentPath = Activator.getPluginLocation();
	public String graphDataPath= "";

	//FOR TESTING THE GRAPH PARSING
	public void executeGraphTests(){
		StapGraphParser grph = StapGraphParserTest.initializeGraph(graphDataPath);
		StapGraphParserTest.assertSanity(grph);
		StapGraphParserTest.assertTimes(grph);
		StapGraphParserTest.assertConnectedness(grph);
	}

    @Test
	public void testJustMain(){
		graphDataPath = currentPath+"main.graph";
		executeGraphTests();
	}

    @Test
	public void testCallGraphRunBasic(){
		graphDataPath = currentPath+"basic.graph";
		executeGraphTests();
	}
    @Test
	public void testCallGraphRunRecursive(){
		graphDataPath = currentPath+"catlan.graph";
		executeGraphTests();
	}
    @Test
	public void testManyFuncs(){
		graphDataPath = currentPath+"eag.graph";
		executeGraphTests();
	}
    @Test
	public void testComprehensive(){
		graphDataPath = currentPath+"comprehensive.graph";
		executeGraphTests();
	}
    @Test
	public void testHeavy(){
		graphDataPath = currentPath+"heavy.graph";
		executeGraphTests();
	}

}
