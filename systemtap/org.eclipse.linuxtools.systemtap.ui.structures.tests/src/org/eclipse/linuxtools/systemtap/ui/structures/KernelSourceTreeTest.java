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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class KernelSourceTreeTest {

	@Before
	protected void setUp() {
		kst = new KernelSourceTree();
	}

	@Test
	public void testGetTree() {
		assertNull("Inital tree is null", kst.getTree());
	}
	
	@Test
	public void testBuildKernelTree() {
		TreeNode t;

		String direct = null;	//Null
		String[] excluded = null;
		kst.buildKernelTree(direct, excluded);
		assertNull("Null directory", kst.getTree());
		
		direct = "";	//Empty string for directory
		kst.buildKernelTree(direct, excluded);
		assertNull("Empty string directory", kst.getTree());
		
		direct = "/noSuchDirectory/";	//Missing folder
		kst.buildKernelTree(direct, excluded);
		assertNull("Missing directory", kst.getTree());
		
		direct = "/root/";	//Inaccessable
		kst.buildKernelTree(direct, excluded);
		assertNull("Inaccessable directory", kst.getTree());
		
		direct = "/bin/";	//No .c or .h files
		kst.buildKernelTree(direct, excluded);
		t = kst.getTree();
		assertEquals("Bin folder item count", 0, t.getChildCount());
		assertTrue("Bin folder name", "bin".equals(t.toString()));
		assertTrue("Bin has file", t.getData() instanceof File);

		excluded = new String[] {".git"};
		direct = "/tmp/";	//No .c or .h files
		kst.buildKernelTree(direct, excluded);
	}
	
	public void testDispose() {
		kst.dispose();
	}
	
	KernelSourceTree kst;
}
