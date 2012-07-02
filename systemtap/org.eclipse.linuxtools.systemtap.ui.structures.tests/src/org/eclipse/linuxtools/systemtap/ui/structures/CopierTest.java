/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.linuxtools.systemtap.ui.structures.Copier;

import junit.framework.TestCase;

public class CopierTest extends TestCase {
	public CopierTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public <T> ArrayList<T>[] createArray(int size, ArrayList<T>... elements) {
		return Arrays.copyOf(elements, size);
	}
	
	public void testCopy() {
		ArrayList<String> list = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] lists = new ArrayList[3];
		int listsSize = 3;

		for(int i=0; i<listsSize; i++) {
			list.add("" + i);
			for(int j=0; j<5; j++)
				lists[i].add(new Integer(j));
		}
		
		ArrayList<String> list2 = Copier.copy(list);
		for(int i=0; i<list.size(); i++)
			assertEquals(list2.get(i), list.get(i));
		
		ArrayList<?>[] lists2 = Copier.copy(lists);
		for(int i=0; i<list.size(); i++) {
			for(int j=0; j<lists[i].size(); j++)
				assertEquals(lists2[i].get(j), lists[i].get(j));
		}
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
