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

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraphParser;

public class StapGraphParserTest extends TestCase {
	
	//RENDER THE GRAPH
	public  static StapGraphParser initializeGraph(String filePath){
		StapGraphParser grph = new StapGraphParser("basic test", filePath);
		grph.testRun(new NullProgressMonitor());
		return grph;
	}
	
	public static void assertSanity(StapGraphParser grph){
		assertEquals(grph.serialMap.get(0),null);
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

}
